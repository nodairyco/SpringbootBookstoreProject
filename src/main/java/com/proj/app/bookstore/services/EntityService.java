package com.proj.app.bookstore.services;

import com.proj.app.bookstore.domain.entities.BookEntity;
import com.proj.app.bookstore.domain.entities.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface EntityService<EntityType, EntityId> {
    EntityType save(EntityType entity);
    Optional<EntityType> findById(EntityId id);
    default Optional<EntityType> findByEmail(String email){
        throw new UnsupportedOperationException("not implemented");
    }
    default List<EntityType> findByMemberEmail(String email){
        throw new UnsupportedOperationException("not implemented");
    }
    Page<EntityType> findAll(Pageable pageable);
    Iterable<EntityType> findAll();
    EntityType partialUpdateById(EntityId id, EntityType entity);
    EntityType deleteById(EntityId id);
    default Page<BookEntity> getAllMemberBooks(Long id, Pageable pageable){
        throw new UnsupportedOperationException("not implemented");
    }
    default List<UserEntity> findAllByBookIsbn(String isbn){
        throw new UnsupportedOperationException("not implemented");
    }
}
