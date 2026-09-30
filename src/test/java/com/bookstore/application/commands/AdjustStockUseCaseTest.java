package com.bookstore.application.commands;

import com.bookstore.application.ports.InventoryRepository;
import com.bookstore.domain.inventory.InventoryItem;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AdjustStockUseCaseTest {
    @Test
    void increasingAvailableStockPersistsQuantityAndKeepsItemAboveThreshold() {
        Map<Integer, InventoryItem> items = new HashMap<>();
        items.put(1, new InventoryItem(1, 8, 5));
        InventoryRepository repository = new InventoryRepository() {
            public Optional<InventoryItem> findByBookId(int bookId) {
                return Optional.ofNullable(items.get(bookId));
            }

            public void save(InventoryItem item) {
                items.put(item.bookId(), item);
            }
        };

        var result = new AdjustStockUseCase(bookId -> true, repository).execute(1, 2);

        assertEquals(10, result.quantityOnHand());
        assertEquals(5, result.reorderThreshold());
        assertFalse(result.isLowStock());
        assertEquals(result, items.get(1));
    }
}
