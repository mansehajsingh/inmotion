package com.inmotionchat.smartpersist;

import com.google.gson.internal.Primitives;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SmartQuery<T> {

    protected Class<T> type;

    protected MultiValueMap<String, Object> parameters;

    public SmartQuery(Class<T> type, MultiValueMap<String, Object> parameters) {
        this.type = type;
        this.parameters = parameters;
    }

    public void add(String param, Object ...values) {
        this.parameters.addAll(param, Arrays.stream(values).toList());
    }

    private List<Predicate> handleDate(String searchKey, Root<T> root, CriteriaBuilder criteriaBuilder) throws ParseException {
        String suffix = searchKey.endsWith("After") ? "After" : "Before";
        String columnName = searchKey.substring( 0, searchKey.length() - suffix.length() );
        List<Predicate> predicates = new ArrayList<>();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        for (Object objValue : parameters.get(searchKey)) {
            if (objValue instanceof String value) {

                if (suffix.equals("After")) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(columnName), format.parse(value)));
                } else {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get(columnName), format.parse(value)));
                }
            }
        }

        return predicates;
    }

    private List<Predicate> handleGenericEquals(String searchKey, Root<T> root, CriteriaBuilder criteriaBuilder) throws NoSuchFieldException {
        Field field = getField(searchKey);
        List<Predicate> predicates = new ArrayList<>();

        Class<?> fieldType = field.getType();

        if (fieldType.isPrimitive()) {
            fieldType = Primitives.wrap(fieldType);
        }

        for (Object objValue : parameters.get(searchKey)) {
            predicates.add(criteriaBuilder.equal(root.get(searchKey), fieldType.cast(objValue)));
        }

        return predicates;
    }

    private boolean isForeignKey(String columnName) throws NoSuchFieldException {
        Field field = getField(columnName);
        return field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToOne.class);
    }

    private boolean isId(String columnName) throws NoSuchFieldException {
        Field field = getField(columnName);
        return field.isAnnotationPresent(Id.class);
    }

    private Field getField(String field) throws NoSuchFieldException {
        Class<?> clazz = type;

        do {
            try {
                return clazz.getDeclaredField(field);
            } catch (NoSuchFieldException ignored) {}

            clazz = clazz.getSuperclass();
        } while (clazz != null);

        throw new NoSuchFieldException();
    }

    private boolean hasField(String field) {
        try {
            getField(field);
            return true;
        } catch (NoSuchFieldException ignored) {
            return false;
        }
    }

    protected Specification<T> build(Specification<T> additionalSteps) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            try {

                for (String searchKey : parameters.keySet()) {

                    if (searchKey.endsWith("After") || searchKey.endsWith("Before")) {
                        Predicate[] predicates = handleDate(searchKey, root, criteriaBuilder).toArray(new Predicate[0]);
                        predicate = criteriaBuilder.and(predicate, criteriaBuilder.or(predicates));

                    } else if (hasField(searchKey)) {
                        if (isId(searchKey) || isForeignKey(searchKey)) {
                            Long fkeyId = Long.parseLong(parameters.getFirst(searchKey).toString());
                            predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get(searchKey), fkeyId));
                        } else {
                            Predicate[] predicates = handleGenericEquals(searchKey, root, criteriaBuilder).toArray(new Predicate[0]);
                            predicate = criteriaBuilder.and(predicate, criteriaBuilder.or(predicates));
                        }

                    }

                }

            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }

            predicate = criteriaBuilder.and(predicate,
                    additionalSteps.toPredicate(root, criteriaQuery, criteriaBuilder));

            return predicate;
        };
    }

    protected Specification<T> build() {
        return build((root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.conjunction());
    }

}
