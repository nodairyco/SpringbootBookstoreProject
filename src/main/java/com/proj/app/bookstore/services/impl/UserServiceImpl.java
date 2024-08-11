package com.proj.app.bookstore.services.impl;

import com.proj.app.bookstore.domain.entities.UserEntity;
import com.proj.app.bookstore.repositories.UserRepository;
import com.proj.app.bookstore.services.EntityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements EntityService<UserEntity, Long> {
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
    public Page<UserEntity> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public Iterable<UserEntity> findAll() {
        return repository.findAll();
    }

    @Override
    public UserEntity partialUpdateById(Long id, UserEntity userEntity) {
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
    public UserEntity deleteById(Long id) {
        UserEntity res = findById(id).get();
        repository.delete(res);
        return res;
    }

    @Override
    public List<UserEntity> findAllByBookIsbn(String isbn){
        return repository.findAllByBookIsbn(isbn);
    }
}
