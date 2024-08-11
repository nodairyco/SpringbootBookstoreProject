package com.proj.app.bookstore.repositories;

import com.proj.app.bookstore.domain.entities.UserEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, Long>,
        PagingAndSortingRepository<UserEntity, Long> {

    @Query("SELECT user FROM UserEntity user WHERE user.email = ?1")
    Optional<UserEntity> findByEmail(String email);

    @Query("SELECT user FROM UserEntity user JOIN user.purchasedBooks b where b.isbn = ?1")
    List<UserEntity> findAllByBookIsbn(String isbn);
}
