package com.proj.app.bookstore.mappers.impl;

import com.proj.app.bookstore.domain.dto.UserDto;
import com.proj.app.bookstore.domain.entities.BookEntity;
import com.proj.app.bookstore.domain.entities.UserEntity;
import com.proj.app.bookstore.mappers.Mapper;
import com.proj.app.bookstore.services.EntityService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserMapperImpl implements Mapper<UserEntity, UserDto> {

    private final EntityService<BookEntity, String> bookService;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDto mapTo(UserEntity userEntity) {
        UserDto res = modelMapper.map(userEntity, UserDto.class);
        res.setPassword(null);
        if(userEntity.getPurchasedBooks() != null) {
            res.setPurchasedBookIsbn(
                    userEntity.getPurchasedBooks()
                            .stream()
                            .map(BookEntity::getIsbn)
                            .collect(Collectors.toSet()));
        }
        return res;
    }

    @Override
    public UserEntity mapFrom(UserDto userDto) {
        UserEntity res = modelMapper.map(userDto, UserEntity.class);
        res.setPassword(passwordEncoder.encode(userDto.getPassword()));
        if(userDto.getPurchasedBookIsbn() != null) {
            res.setPurchasedBooks(userDto.getPurchasedBookIsbn()
                    .stream()
                    .map(a -> bookService.findById(a).get())
                    .collect(Collectors.toSet()));
        }
        return res;
    }
}
