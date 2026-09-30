package com.bookstore.infrastructure.repositories;

import com.bookstore.domain.inventory.InventoryItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class FileInventoryRepositoryTest {
    @TempDir Path directory;

    @Test
    void savesAdjustedItemUsingTheInventoryFile() throws java.io.IOException {
        var repository = new FileInventoryRepository(directory);
        repository.save(new InventoryItem(1, 8, 5));

        var useCase = new com.bookstore.application.commands.AdjustStockUseCase(
                bookId -> bookId == 1, repository);
        useCase.execute(1, 2);

        var stored = new FileInventoryRepository(directory).findByBookId(1).orElseThrow();
        assertEquals(10, stored.quantityOnHand());
        assertFalse(stored.isLowStock());
        assertFalse(Files.readString(directory.resolve("inventory.json")).contains("lowStock"));
    }
}
