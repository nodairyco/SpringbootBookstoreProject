package com.proj.app.bookstore.services.impl;

import com.proj.app.bookstore.domain.entities.GroupEntity;
import com.proj.app.bookstore.repositories.GroupRepository;
import com.proj.app.bookstore.services.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {
    private final GroupRepository repository;

    @Override
    public GroupEntity save(GroupEntity group) {
        return repository.save(group);
    }

    @Override
    public void saveAll(Iterable<GroupEntity> s) {
        repository.saveAll(s);
    }

    @Override
    public Page<GroupEntity> getAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public Optional<GroupEntity> getById(Long id) {
        return repository.findById(id);
    }

    @Override
    public GroupEntity deleteById(Long groupId) {
        return getById(groupId)
                .map(g -> {
                    repository.delete(g);
                    return g;
                })
            .orElseThrow();
    }

    @Override
    public List<GroupEntity> findByAdminEmail(String email) {
        return repository.findAllByAdminUser_Email(email);
    }

    @Override
    public List<GroupEntity> findByMemberEmail(String email) {
        return repository.findAllByMemberEmail(email);
    }
}
