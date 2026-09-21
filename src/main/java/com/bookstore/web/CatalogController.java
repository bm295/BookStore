package com.bookstore.web;

import com.bookstore.domain.catalog.Book;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/books")
public class CatalogController {
    private final ConcurrentHashMap<Integer, Book> catalog = new ConcurrentHashMap<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    @GetMapping
    public List<Book> list() {
        return catalog.values().stream().sorted(Comparator.comparingInt(Book::getId)).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Book add(@RequestBody CreateBookRequest request) {
        if (request.price() < 0) {
            throw new IllegalArgumentException("Price cannot be negative.");
        }
        var book = new Book();
        book.setId(nextId.getAndIncrement());
        book.setPrice(request.price());
        catalog.put(book.getId(), book);
        return book;
    }

    public record CreateBookRequest(int price) {}
}
