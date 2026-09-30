package com.bookstore.application.ports;

@FunctionalInterface
public interface BookLookup {
    boolean exists(int bookId);
}
