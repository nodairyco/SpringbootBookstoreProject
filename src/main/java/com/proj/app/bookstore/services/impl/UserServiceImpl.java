package com.proj.app.bookstore.services.impl;

import com.proj.app.bookstore.domain.entities.BookEntity;
import com.proj.app.bookstore.domain.entities.UserEntity;
import com.proj.app.bookstore.repositories.UserRepository;
import com.proj.app.bookstore.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public UserEntity save(UserEntity userEntity) {
        return repository.save(userEntity);
    }

    @Override
    public Optional<UserEntity> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Page<UserEntity> getAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public UserEntity partialUpdateUser(Long id, UserEntity userEntity) {
        userEntity.setId(id);
        return repository.findById(id).map(
                user -> {
                    Optional.ofNullable(userEntity.getEmail()).ifPresent(user :: setEmail);
                    Optional.ofNullable(userEntity.getName()).ifPresent(user ::setName);
                    return repository.save(user);
                }
        ).orElseThrow(() -> new IllegalArgumentException("illegal id"));
    }

    @Override
    public Optional<UserEntity> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    @Override
    public void saveAll(Iterable<UserEntity> lst) {
        repository.saveAll(lst);
    }

    @Override
    public Page<BookEntity> getAllBooks(Set<UserEntity> lst, Pageable pageable) {
        Page<UserEntity> userPage = repository.findAll(pageable);

        List<BookEntity> books = userPage.getContent().stream()
                .filter(lst::contains)
                .flatMap(user -> user.getPurchasedBooks().stream())
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), books.size());
        List<BookEntity> pagedBooks = books.subList(start, end);

        return new PageImpl<>(pagedBooks, pageable, books.size());
    }

    @Override
    public UserEntity deleteById(Long id) {
        UserEntity res = findById(id).get();
        repository.delete(res);
        return res;
    }

}
