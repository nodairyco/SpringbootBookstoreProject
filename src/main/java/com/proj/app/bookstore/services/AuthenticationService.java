package com.proj.app.bookstore.services;

import com.proj.app.bookstore.domain.dto.AuthenticationRequest;
import com.proj.app.bookstore.domain.dto.AuthenticationResponse;
import com.proj.app.bookstore.domain.dto.UserDto;

public interface AuthenticationService {
    AuthenticationResponse register(UserDto request);

    AuthenticationResponse authenticate(AuthenticationRequest request);
}
