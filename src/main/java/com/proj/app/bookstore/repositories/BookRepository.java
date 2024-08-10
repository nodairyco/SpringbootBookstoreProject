package com.proj.app.bookstore.repositories;

import com.proj.app.bookstore.domain.entities.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<BookEntity, String>,
        PagingAndSortingRepository<BookEntity, String> {
    Optional<BookEntity> findByIsbn(String isbn);
}
