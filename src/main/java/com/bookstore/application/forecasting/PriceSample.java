package com.bookstore.application.forecasting;

public record PriceSample(BookFeatures features, double price) {
    public PriceSample {
        if (features == null || !Double.isFinite(price) || price < 0)
            throw new IllegalArgumentException("A sample requires features and a finite nonnegative price.");
    }
}
