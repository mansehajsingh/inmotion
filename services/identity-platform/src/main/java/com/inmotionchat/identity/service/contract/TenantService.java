package com.inmotionchat.identity.service.contract;

import com.inmotionchat.core.data.dto.TenantDTO;
import com.inmotionchat.core.data.postgres.identity.Tenant;
import com.inmotionchat.core.exceptions.DomainInvalidException;
import com.inmotionchat.core.exceptions.UnauthorizedException;
import com.inmotionchat.smartpersist.exception.ConflictException;
import com.inmotionchat.smartpersist.exception.NotFoundException;

import java.util.List;

public interface TenantService {

    Tenant retrieveById(Long id) throws NotFoundException;

    List<Tenant> searchByDomain(String domain);

    Tenant create(TenantDTO prototype) throws DomainInvalidException, ConflictException, NotFoundException, UnauthorizedException;

}
