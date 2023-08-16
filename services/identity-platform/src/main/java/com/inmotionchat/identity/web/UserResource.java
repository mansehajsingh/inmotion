package com.inmotionchat.identity.web;

import com.inmotionchat.core.data.dto.UserDTO;
import com.inmotionchat.core.data.dto.VerifyDTO;
import com.inmotionchat.core.exceptions.DomainInvalidException;
import com.inmotionchat.core.exceptions.UnauthorizedException;
import com.inmotionchat.identity.service.contract.UserService;
import com.inmotionchat.smartpersist.exception.ConflictException;
import com.inmotionchat.smartpersist.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.inmotionchat.core.web.AbstractResource.API_V1;

@RestController
@RequestMapping(API_V1 + "/users")
public class UserResource {

    private final UserService userService;

    @Autowired
    public UserResource(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody UserDTO userDTO) throws ConflictException, DomainInvalidException, NotFoundException, UnauthorizedException {
        this.userService.createEmailPasswordUser(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/{uid}/verify")
    public ResponseEntity<?> verify(@PathVariable String uid, @RequestBody VerifyDTO verifyDTO) throws ConflictException, DomainInvalidException, NotFoundException, UnauthorizedException {
        this.userService.verifyEmailPasswordUser(uid, verifyDTO);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
