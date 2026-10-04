package com.bookstore.application.forecasting;

import java.util.List;

/** Version 1: cubic basis [1,x,y,x*x,x*y,y*y,x*x*x,x*x*y,x*y*y,y*y*y]. */
public record PriceModel(int schemaVersion, List<Double> means, List<Double> scales,
                         List<Double> coefficients) {
    public PriceModel {
        if (schemaVersion != 1 || !valid(means, 2) || !valid(scales, 2)
                || !valid(coefficients, 10) || scales.stream().anyMatch(x -> x <= 0))
            throw new IllegalArgumentException("Invalid or unsupported price model.");
        means = List.copyOf(means);
        scales = List.copyOf(scales);
        coefficients = List.copyOf(coefficients);
    }

    private static boolean valid(List<Double> values, int size) {
        return values != null && values.size() == size
                && values.stream().allMatch(x -> x != null && Double.isFinite(x));
    }
}
