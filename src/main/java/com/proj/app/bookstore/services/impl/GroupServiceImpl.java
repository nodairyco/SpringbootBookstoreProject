package com.proj.app.bookstore.services.impl;

import com.proj.app.bookstore.domain.entities.BookEntity;
import com.proj.app.bookstore.domain.entities.GroupEntity;
import com.proj.app.bookstore.domain.entities.UserEntity;
import com.proj.app.bookstore.repositories.GroupRepository;
import com.proj.app.bookstore.repositories.UserRepository;
import com.proj.app.bookstore.services.EntityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements EntityService<GroupEntity, Long> {
    private final GroupRepository repository;
    private final UserRepository userRepository;

    @Override
    public GroupEntity save(GroupEntity group) {
        return repository.save(group);
    }


    @Override
    public Page<GroupEntity> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public Iterable<GroupEntity> findAll() {
        return repository.findAll();
    }

    @Override
    public GroupEntity partialUpdateById(Long aLong, GroupEntity entity) {
        throw new UnsupportedOperationException("not intended for this class");
    }

    @Override
    public Optional<GroupEntity> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public GroupEntity deleteById(Long groupId) {
        return findById(groupId)
                .map(g -> {
                    repository.delete(g);
                    return g;
                })
            .orElseThrow();
    }

    @Override
    public List<GroupEntity> findByMemberEmail(String email) {
        return repository.findAllByMemberEmail(email);
    }

    @Override
    public Page<BookEntity> getAllMemberBooks(Long id, Pageable pageable) {
        Page<UserEntity> userPage = userRepository.findAll(pageable);
        GroupEntity group = repository.findById(id).get();

        List<BookEntity> books = userPage.getContent().stream()
                .filter(group.getMembers()::contains)
                .flatMap(user -> user.getPurchasedBooks().stream())
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), books.size());
        List<BookEntity> pagedBooks = books.subList(start, end);

        return new PageImpl<>(pagedBooks, pageable, books.size());
    }
}
