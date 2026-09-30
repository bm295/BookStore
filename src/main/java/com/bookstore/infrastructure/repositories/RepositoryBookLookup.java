package com.bookstore.infrastructure.repositories;

import com.bookstore.application.ports.BookLookup;
import java.util.Objects;

public final class RepositoryBookLookup implements BookLookup {
    private final BookRepository books;

    public RepositoryBookLookup(BookRepository books) {
        this.books = Objects.requireNonNull(books);
    }

    public boolean exists(int bookId) {
        return books.getAll().stream().anyMatch(book -> book.getId() == bookId);
    }
}
