package com.proj.app.bookstore.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookDto {
    private String isbn;
    private String author;
    private String title;
    private Integer price;
    private String uploaderEmail;
    private LocalDate uploadDate;
}
