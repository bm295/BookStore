package com.bookstore.application.forecasting;

/** Explicit feature order: page count, years since publication. IDs are not features. */
public record BookFeatures(int pageCount, int publicationAgeYears) {
    public BookFeatures {
        if (pageCount <= 0 || publicationAgeYears < 0)
            throw new IllegalArgumentException("Page count must be positive and publication age nonnegative.");
    }
}
