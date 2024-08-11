package com.proj.app.bookstore.controllers;

import com.proj.app.bookstore.domain.dto.BookDto;
import com.proj.app.bookstore.domain.entities.BookEntity;
import com.proj.app.bookstore.domain.entities.UserEntity;
import com.proj.app.bookstore.mappers.Mapper;
import com.proj.app.bookstore.services.EntityService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/bookstore/books")
@RequiredArgsConstructor
public class BookController {
    private final EntityService<BookEntity, String> bookService;
    private final EntityService<UserEntity, Long> userService;
    private final Mapper<BookEntity, BookDto> mapper;

    @PostMapping("/create")
    public ResponseEntity<BookDto> createBook(@RequestBody BookDto book){
        book.setUploaderEmail(getCurrentUser().getEmail());
        BookEntity mapped = mapper.mapFrom(book);
        mapped.setUploadDate(LocalDate.now());
        var res = bookService.save(mapped);
        return ResponseEntity.ok(mapper.mapTo(res));
    }

    @PatchMapping("/buy/{isbn}")
    public ResponseEntity<List<BookDto>> buyBook(@PathVariable("isbn") String isbn){
        BookEntity bookToBuy;
        try{
            bookToBuy = checkBookValidity(isbn);
        }catch (IllegalArgumentException i){
            return ResponseEntity.notFound().build();
        }

        UserEntity current = getCurrentUser();

        if(current.getMoney() > bookToBuy.getPrice()){
            current.addBook(bookToBuy);
            current.setMoney(current.getMoney() - bookToBuy.getPrice());
        } else{
            return ResponseEntity.of(
                    ProblemDetail.forStatusAndDetail(HttpStatus.NOT_MODIFIED, "not enough money")
            ).build();
        }

        current = userService.save(current);
        return ResponseEntity.ok(current.getPurchasedBooks().stream()
                .map(mapper::mapTo)
                .toList());
    }

    @CheckUploader
    @PostMapping("/update/{isbn}")
    public ResponseEntity<BookDto> partialUpdateBook(@PathVariable("isbn") String isbn,
                                                     @RequestBody BookDto bookDto){
        BookEntity mapped = BookEntity.builder()
                .price(bookDto.getPrice())
                .title(bookDto.getTitle())
                .author(bookDto.getAuthor())
                .build();

        try{
            mapped = bookService.partialUpdateById(isbn, mapped);
        }catch (IllegalArgumentException i){
            return ResponseEntity.of(
                    ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "illegal isbn")
            ).build();
        }

        mapped = bookService.save(mapped);
        return ResponseEntity.ok(mapper.mapTo(mapped));
    }

    private BookEntity checkBookValidity(String isbn){
        Optional<BookEntity> opt = bookService.findById(isbn);
        if (opt.isEmpty())
            throw new IllegalArgumentException("illegal isbn");
        return opt.get();
    }

    public boolean checkOwnerShip(String isbn, UserEntity user){
        return checkBookValidity(isbn).getUploader().equals(user);
    }

    public UserEntity getCurrentUser(){
        return userService.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new IllegalArgumentException("jwt unauthenticated"));
    }


    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @Documented
    @PreAuthorize("@bookController.checkOwnerShip(#isbn, @bookController.getCurrentUser())")
    public @interface CheckUploader{
    }
}
