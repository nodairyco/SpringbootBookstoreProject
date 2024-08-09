package com.proj.app.bookstore.services;

import com.proj.app.bookstore.domain.entities.GroupEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;


public interface GroupService {
    GroupEntity save(GroupEntity group);
    void saveAll(Iterable<GroupEntity> s);
    Page<GroupEntity> getAll(Pageable pageable);
    Optional<GroupEntity> getById(Long id);
    GroupEntity deleteById(Long groupId);
    List<GroupEntity> findByAdminEmail(String email);
    List<GroupEntity> findByMemberEmail(String email);
}
