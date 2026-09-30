package com.bookstore.infrastructure.repositories;

import com.bookstore.application.ports.InventoryRepository;
import com.bookstore.domain.inventory.InventoryItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class FileInventoryRepository implements InventoryRepository {
    private final Path directory;
    private final Path file;
    private final ObjectMapper mapper = new ObjectMapper();

    public FileInventoryRepository(Path directory) {
        this.directory = directory;
        this.file = directory.resolve("inventory.json");
    }

    public Optional<InventoryItem> findByBookId(int bookId) {
        return readAll().stream().filter(item -> item.bookId() == bookId).findFirst();
    }

    public void save(InventoryItem item) {
        List<InventoryItem> items = new ArrayList<>(readAll());
        items.removeIf(existing -> existing.bookId() == item.bookId());
        items.add(item);
        try {
            Files.createDirectories(directory);
            var root = mapper.createObjectNode()
                    .put("schemaVersion", 1)
                    .put("generatedAtUtc", Instant.now().toString());
            var storedItems = mapper.createArrayNode();
            for (InventoryItem stored : items) {
                storedItems.add(mapper.createObjectNode()
                        .put("bookId", stored.bookId())
                        .put("quantityOnHand", stored.quantityOnHand())
                        .put("reorderThreshold", stored.reorderThreshold()));
            }
            root.set("items", storedItems);
            Path temporary = Files.createTempFile(directory, "inventory-", ".tmp");
            try {
                mapper.writerWithDefaultPrettyPrinter().writeValue(temporary.toFile(), root);
                try {
                    Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } catch (java.nio.file.AtomicMoveNotSupportedException e) {
                    Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
                }
            } finally {
                Files.deleteIfExists(temporary);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private List<InventoryItem> readAll() {
        if (!Files.exists(file)) return List.of();
        try {
            var root = mapper.readTree(file.toFile());
            if (root == null || root.path("schemaVersion").asInt() != 1 || !root.path("items").isArray()) {
                throw new IllegalStateException("Invalid inventory file: " + file);
            }
            return Arrays.asList(mapper.treeToValue(root.get("items"), InventoryItem[].class));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
