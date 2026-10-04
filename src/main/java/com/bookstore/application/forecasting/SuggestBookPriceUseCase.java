package com.bookstore.application.forecasting;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

public final class SuggestBookPriceUseCase {
    private final BookPricePredictor predictor;
    private final PriceModelRepository repository;

    public SuggestBookPriceUseCase(BookPricePredictor predictor, PriceModelRepository repository) {
        this.predictor = Objects.requireNonNull(predictor);
        this.repository = Objects.requireNonNull(repository);
    }

    public void train(List<PriceSample> samples) {
        repository.save(predictor.train(samples));
    }

    public BigDecimal suggest(BookFeatures features) {
        if (features == null) throw new IllegalArgumentException("Book features are required.");
        var model = repository.load().orElseThrow(
                () -> new IllegalArgumentException("Train a price model before requesting suggestions."));
        double price = predictor.predict(model, features);
        if (!Double.isFinite(price) || price < 0)
            throw new IllegalArgumentException("Model cannot provide a valid price for these features.");
        return BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP);
    }
}
