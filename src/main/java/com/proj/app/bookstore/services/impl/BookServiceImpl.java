package com.proj.app.bookstore.services.impl;

import com.proj.app.bookstore.domain.entities.BookEntity;
import com.proj.app.bookstore.repositories.BookRepository;
import com.proj.app.bookstore.services.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private final BookRepository repository;

    @Override
    public BookEntity save(BookEntity book) {
        return repository.save(book);
    }

    @Override
    public List<BookEntity> findAll() {
        return repository.findAll();
    }

    @Override
    public Page<BookEntity> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public Optional<BookEntity> findByIsbn(String isbn) {
        return repository.findByIsbn(isbn);
    }

    @Override
    public BookEntity partialUpdateByIsbn(String isbn, BookEntity newBook) {
        return repository.findByIsbn(isbn).map(book -> {
            Optional.ofNullable(newBook.getTitle()).ifPresent(book::setTitle);
            Optional.ofNullable(newBook.getAuthor()).ifPresent(book::setAuthor);

            return save(book);
        }).orElseThrow(() -> new IllegalArgumentException("isbn incorrect"));
    }

    @Override
    public BookEntity deleteByIsbn(String isbn) {
        BookEntity res = findByIsbn(isbn).orElseThrow(() -> new IllegalArgumentException("isbn incorrect"));
        repository.delete(res);
        return res;
    }
}
