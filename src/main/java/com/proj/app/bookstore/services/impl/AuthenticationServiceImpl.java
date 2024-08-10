package com.proj.app.bookstore.services.impl;

import com.proj.app.bookstore.domain.dto.AuthenticationRequest;
import com.proj.app.bookstore.domain.dto.AuthenticationResponse;
import com.proj.app.bookstore.domain.dto.UserDto;
import com.proj.app.bookstore.domain.entities.UserEntity;
import com.proj.app.bookstore.mappers.Mapper;
import com.proj.app.bookstore.repositories.UserRepository;
import com.proj.app.bookstore.services.AuthenticationService;
import com.proj.app.bookstore.services.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final Mapper<UserEntity, UserDto> mapper;

    @Override
    public AuthenticationResponse register(UserDto request) {
        var user = mapper.mapFrom(request);
        user.addRole("USER");
        user = userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder().token(jwtToken).build();
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder().token(jwtToken).build();
    }

}
