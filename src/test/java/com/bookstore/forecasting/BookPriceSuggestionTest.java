package com.bookstore.forecasting;

import com.bookstore.application.forecasting.*;
import com.bookstore.infrastructure.forecasting.PolynomialBookPricePredictor;
import com.bookstore.infrastructure.repositories.FilePriceModelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.UncheckedIOException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class BookPriceSuggestionTest {
    @TempDir Path directory;
    private final PolynomialBookPricePredictor predictor = new PolynomialBookPricePredictor();

    private List<PriceSample> samples() {
        var rows = new ArrayList<PriceSample>();
        for (int pages = 100; pages <= 500; pages += 100)
            for (int age = 0; age <= 4; age++)
                rows.add(new PriceSample(new BookFeatures(pages, age), 20 + pages * .1 - age * 2));
        return rows;
    }

    @Test void learnsPricesAndSurvivesRepositoryReload() {
        var repository = new FilePriceModelRepository(directory);
        var service = new SuggestBookPriceUseCase(predictor, repository);
        service.train(samples());
        var reloaded = new SuggestBookPriceUseCase(predictor, new FilePriceModelRepository(directory));
        assertEquals(53.0, reloaded.suggest(new BookFeatures(350, 1)).doubleValue(), .05);
    }

    @Test void handlesConstantFeaturesAndRepeatedTrainingWithoutSharedState() {
        var rows = new ArrayList<PriceSample>();
        for (int i = 0; i < 12; i++) rows.add(new PriceSample(new BookFeatures(200, 2), 40));
        var first = predictor.train(rows);
        predictor.train(samples());
        assertEquals(40, predictor.predict(first, new BookFeatures(200, 2)), .001);
    }

    @Test void rejectsInvalidInputsAndMissingModel() {
        assertThrows(IllegalArgumentException.class, () -> new BookFeatures(0, 1));
        assertThrows(IllegalArgumentException.class, () -> new BookFeatures(100, -1));
        assertThrows(IllegalArgumentException.class, () -> new PriceSample(new BookFeatures(100, 1), Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> predictor.train(List.of()));
        assertThrows(IllegalArgumentException.class, () -> new SuggestBookPriceUseCase(predictor,
                new FilePriceModelRepository(directory)).suggest(new BookFeatures(100, 1)));
    }

    @Test void estimatesUnseenExamplesWithSmallAbsoluteError() {
        var model = predictor.train(samples());
        double error = 0;
        for (int pages : new int[]{150, 250, 350, 450})
            error += Math.abs(predictor.predict(model, new BookFeatures(pages, 2)) - (16 + pages * .1));
        assertTrue(error / 4 < .05);
    }

    @Test void learnsCubicAndInteractionTerms() {
        var rows = new ArrayList<PriceSample>();
        for (int pages = 100; pages <= 700; pages += 100)
            for (int age = 0; age <= 6; age++)
                rows.add(new PriceSample(new BookFeatures(pages, age), nonlinearPrice(pages, age)));
        var model = predictor.train(rows);
        assertEquals(nonlinearPrice(350, 3), predictor.predict(model, new BookFeatures(350, 3)), .01);
    }

    private double nonlinearPrice(int pages, int age) {
        double x = pages / 100.0;
        return 20 + x*x*x + x*age + age*age;
    }

    @Test void failedTrainingKeepsPreviousModel() {
        var service = new SuggestBookPriceUseCase(predictor, new FilePriceModelRepository(directory));
        service.train(samples());
        var before = service.suggest(new BookFeatures(350, 1));
        assertThrows(IllegalArgumentException.class, () -> service.train(List.of()));
        assertEquals(before, service.suggest(new BookFeatures(350, 1)));
    }

    @Test void rejectsCorruptOrUnsupportedSavedModels() throws Exception {
        Files.writeString(directory.resolve("price-model.json"), "{broken");
        var repository = new FilePriceModelRepository(directory);
        assertThrows(UncheckedIOException.class, repository::load);
        Files.writeString(directory.resolve("price-model.json"),
                "{\"schemaVersion\":2,\"means\":[0,0],\"scales\":[1,1],\"coefficients\":[0,0,0,0,0,0,0,0,0,0]}");
        assertThrows(UncheckedIOException.class, repository::load);
    }

    @Test void rejectsNegativeSuggestionsAndRoundsMoney() {
        var coefficients = new ArrayList<>(Collections.nCopies(10, 0.0));
        coefficients.set(0, -1.0);
        var repository = new FilePriceModelRepository(directory);
        repository.save(new PriceModel(1, List.of(0.0, 0.0), List.of(1.0, 1.0), coefficients));
        var service = new SuggestBookPriceUseCase(predictor, repository);
        assertThrows(IllegalArgumentException.class, () -> service.suggest(new BookFeatures(100, 1)));
        coefficients.set(0, 12.345);
        repository.save(new PriceModel(1, List.of(0.0, 0.0), List.of(1.0, 1.0), coefficients));
        assertEquals("12.35", service.suggest(new BookFeatures(100, 1)).toPlainString());
    }
}
