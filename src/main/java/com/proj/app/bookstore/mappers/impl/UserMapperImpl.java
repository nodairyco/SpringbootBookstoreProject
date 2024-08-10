package com.proj.app.bookstore.mappers.impl;

import com.proj.app.bookstore.domain.dto.UserDto;
import com.proj.app.bookstore.domain.entities.UserEntity;
import com.proj.app.bookstore.mappers.Mapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapperImpl implements Mapper<UserEntity, UserDto> {

    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDto mapTo(UserEntity userEntity) {
        UserDto res = modelMapper.map(userEntity, UserDto.class);
        res.setPassword(null);
        return res;
    }

    @Override
    public UserEntity mapFrom(UserDto userDto) {
        UserEntity res = modelMapper.map(userDto, UserEntity.class);
        res.setPassword(passwordEncoder.encode(userDto.getPassword()));
        return res;
    }
}
