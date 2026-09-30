package com.bookstore.application.ports;

import com.bookstore.domain.inventory.InventoryItem;
import java.util.Optional;

public interface InventoryRepository {
    Optional<InventoryItem> findByBookId(int bookId);
    void save(InventoryItem item);
}
