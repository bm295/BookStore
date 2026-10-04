package com.bookstore.infrastructure.repositories;

import com.bookstore.application.forecasting.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.Optional;

public final class FilePriceModelRepository implements PriceModelRepository {
    private final Path directory;
    private final Path file;
    private final ObjectMapper mapper = new ObjectMapper();

    public FilePriceModelRepository(Path directory) {
        this.directory = directory;
        this.file = directory.resolve("price-model.json");
    }

    @Override public Optional<PriceModel> load() {
        if (!Files.exists(file)) return Optional.empty();
        try {
            var model = mapper.readValue(file.toFile(), PriceModel.class);
            if (model == null) throw new IllegalArgumentException("Price model is empty.");
            return Optional.of(model);
        } catch (IOException e) { throw new UncheckedIOException("Cannot read price model.", e); }
    }

    @Override public void save(PriceModel model) {
        if (model == null) throw new IllegalArgumentException("Price model is required.");
        Path temporary = null;
        try {
            Files.createDirectories(directory);
            temporary = Files.createTempFile(directory, "price-model-", ".tmp");
            mapper.writerWithDefaultPrettyPrinter().writeValue(temporary.toFile(), model);
            try { Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException e) { Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING); }
        } catch (IOException e) { throw new UncheckedIOException("Cannot save price model.", e); }
        finally {
            if (temporary != null) {
                try { Files.deleteIfExists(temporary); }
                catch (IOException e) { throw new UncheckedIOException("Cannot clean temporary price model.", e); }
            }
        }
    }
}
