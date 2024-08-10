package com.proj.app.bookstore.repositories;

import com.proj.app.bookstore.domain.entities.GroupEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface GroupRepository extends CrudRepository<GroupEntity, Long>,
        PagingAndSortingRepository<GroupEntity, Long> {

    List<GroupEntity> findAllByAdminUser_Email(String email);

    @Query("SELECT g FROM GroupEntity g JOIN g.members m WHERE m.email = ?1")
    List<GroupEntity> findAllByMemberEmail(String email);
}
