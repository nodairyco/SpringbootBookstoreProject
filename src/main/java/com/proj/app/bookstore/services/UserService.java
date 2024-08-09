package com.proj.app.bookstore.services;

import com.proj.app.bookstore.domain.entities.BookEntity;
import com.proj.app.bookstore.domain.entities.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.Set;

public interface UserService {
    UserEntity save(UserEntity userEntity);
    Optional<UserEntity> getUserById(Long id);
    Page<UserEntity> getAll(Pageable pageable);
    UserEntity partialUpdateUser(Long id, UserEntity userEntity);
    Optional<UserEntity> findByEmail(String email);
    void saveAll(Iterable<UserEntity> lst);
    Page<BookEntity> getAllBooks(Set<UserEntity> lst, Pageable pageable);
    UserEntity deleteById(Long id);
}
