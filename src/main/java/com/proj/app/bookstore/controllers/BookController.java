package com.proj.app.bookstore.controllers;

import com.proj.app.bookstore.domain.dto.BookDto;
import com.proj.app.bookstore.domain.entities.BookEntity;
import com.proj.app.bookstore.domain.entities.UserEntity;
import com.proj.app.bookstore.mappers.Mapper;
import com.proj.app.bookstore.services.BookService;
import com.proj.app.bookstore.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/bookstore/books")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;
    private final UserService userService;
    private final Mapper<BookEntity, BookDto> mapper;

    @PostMapping("/create")
    public ResponseEntity<BookDto> createBook(@RequestBody BookDto book){
        book.setUploaderEmail(getCurrentUser().getEmail());
        BookEntity mapped = mapper.mapFrom(book);
        mapped.setUploadDate(LocalDate.now());
        var res = bookService.save(mapped);
        return ResponseEntity.ok(mapper.mapTo(res));
    }

    private UserEntity getCurrentUser(){
        return userService.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new IllegalArgumentException("jwt unauthenticated"));
    }
}
