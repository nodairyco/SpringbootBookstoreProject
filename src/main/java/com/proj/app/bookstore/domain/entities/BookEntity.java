package com.proj.app.bookstore.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "books")
@EqualsAndHashCode(exclude = {"purchasedBy"})
public class BookEntity {
    @Id
    @Column(unique = true)
    private String isbn;

    @ManyToOne
    @JoinColumn(name = "email")
    private UserEntity uploader;

    private String author;

    private String title;

    private Integer price;

    private LocalDate uploadDate;

    @ManyToMany(mappedBy = "purchasedBooks")
    private Set<UserEntity> purchasedBy;
}
