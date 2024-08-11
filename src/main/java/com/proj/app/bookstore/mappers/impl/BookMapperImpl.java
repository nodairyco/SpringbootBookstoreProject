package com.proj.app.bookstore.mappers.impl;

import com.proj.app.bookstore.domain.dto.BookDto;
import com.proj.app.bookstore.domain.entities.BookEntity;
import com.proj.app.bookstore.domain.entities.UserEntity;
import com.proj.app.bookstore.mappers.Mapper;
import com.proj.app.bookstore.services.EntityService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@RequiredArgsConstructor
@Component
public class BookMapperImpl implements Mapper<BookEntity, BookDto> {
    private final ModelMapper mapper;
    private final EntityService<UserEntity, Long> userService;

    @Override
    public BookDto mapTo(BookEntity bookEntity) {
        BookDto mapped = mapper.map(bookEntity, BookDto.class);

        mapped.setUploaderEmail(bookEntity.getUploader().getEmail());
        return mapped;
    }

    @Override
    public BookEntity mapFrom(BookDto bookDto) {
        BookEntity mapped = mapper.map(bookDto, BookEntity.class);
        mapped.setUploader(userService.findByEmail(bookDto.getUploaderEmail()).get());
        mapped.setPurchasedBy(new HashSet<>());
        return mapped;
    }
}
