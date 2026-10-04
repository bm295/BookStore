package com.bookstore.web;

import com.bookstore.application.forecasting.*;
import com.bookstore.infrastructure.forecasting.PolynomialBookPricePredictor;
import com.bookstore.infrastructure.repositories.FilePriceModelRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/pricing/suggestions")
public class BookPriceSuggestionController {
    private final SuggestBookPriceUseCase service;

    public BookPriceSuggestionController(@Value("${bookstore.data-directory:data}") String directory) {
        service = new SuggestBookPriceUseCase(new PolynomialBookPricePredictor(),
                new FilePriceModelRepository(Path.of(directory)));
    }

    @PostMapping("/train")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void train(@RequestBody List<PriceSample> samples) { service.train(samples); }

    @PostMapping
    public Suggestion suggest(@RequestBody BookFeatures features) {
        return new Suggestion(service.suggest(features));
    }

    public record Suggestion(BigDecimal suggestedPrice) {}
}
