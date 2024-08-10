package com.proj.app.bookstore.services;

import com.proj.app.bookstore.domain.entities.BookEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface BookService {
    BookEntity save(BookEntity book);
    List<BookEntity> findAll();
    Page<BookEntity> findAll(Pageable pageable);
    Optional<BookEntity> findByIsbn(String isbn);
    BookEntity partialUpdateByIsbn(String isbn, BookEntity newBook);
    BookEntity deleteByIsbn(String isbn);
}
