package com.bookstore.application.forecasting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Prepares an analysis copy of historical prices; does not mutate or train a model. */
public final class PriceDataPreparation {
    public record Row(Integer pageCount, Integer publicationAgeYears, Double price) {}
    public record ColumnStats(int present, int missing, Double min, Double max, Double mean, Double median) {}
    public record Report(int inputRows, int outputRows, int droppedRowsMissingPrice,
                         int imputedPageCount, int imputedPublicationAgeYears,
                         ColumnStats pageCount, ColumnStats publicationAgeYears, ColumnStats price) {}
    public record Result(List<PriceSample> samples, Report report) {}

    public Result prepare(List<Row> rows) {
        if (rows == null || rows.isEmpty()) throw new IllegalArgumentException("At least one row is required.");
        for (Row row : rows) {
            if (row == null) throw new IllegalArgumentException("Rows cannot be null.");
            if (row.pageCount() != null && row.pageCount() <= 0)
                throw new IllegalArgumentException("Page count must be positive.");
            if (row.publicationAgeYears() != null && row.publicationAgeYears() < 0)
                throw new IllegalArgumentException("Publication age must be nonnegative.");
            if (row.price() != null && (!Double.isFinite(row.price()) || row.price() < 0))
                throw new IllegalArgumentException("Price must be finite and nonnegative.");
        }
        ColumnStats pageStats = stats(rows.stream().map(r -> r.pageCount() == null ? null : r.pageCount().doubleValue()).toList());
        ColumnStats ageStats = stats(rows.stream().map(r -> r.publicationAgeYears() == null ? null : r.publicationAgeYears().doubleValue()).toList());
        ColumnStats priceStats = stats(rows.stream().map(Row::price).toList());
        List<Row> eligible = rows.stream().filter(r -> r.price() != null).toList();
        if (eligible.isEmpty()) throw new IllegalArgumentException("No rows have a known price.");
        // Imputation uses only eligible rows, so discarded rows cannot influence the result.
        Double pageMedian = stats(eligible.stream().map(r -> r.pageCount() == null ? null : r.pageCount().doubleValue()).toList()).median();
        Double ageMedian = stats(eligible.stream().map(r -> r.publicationAgeYears() == null ? null : r.publicationAgeYears().doubleValue()).toList()).median();
        if (pageMedian == null || ageMedian == null)
            throw new IllegalArgumentException("Each feature needs at least one known value among rows with a price.");
        int pagesFilled = 0, agesFilled = 0;
        List<PriceSample> samples = new ArrayList<>();
        for (Row row : eligible) {
            int pages = row.pageCount() == null ? (int) Math.round(pageMedian) : row.pageCount();
            int age = row.publicationAgeYears() == null ? (int) Math.round(ageMedian) : row.publicationAgeYears();
            if (row.pageCount() == null) pagesFilled++;
            if (row.publicationAgeYears() == null) agesFilled++;
            samples.add(new PriceSample(new BookFeatures(pages, age), row.price()));
        }
        return new Result(List.copyOf(samples), new Report(rows.size(), samples.size(), rows.size() - samples.size(),
                pagesFilled, agesFilled, pageStats, ageStats, priceStats));
    }

    private ColumnStats stats(List<Double> values) {
        List<Double> present = values.stream().filter(v -> v != null).sorted(Comparator.naturalOrder()).toList();
        int n = present.size();
        if (n == 0) return new ColumnStats(0, values.size(), null, null, null, null);
        double median = n % 2 == 1 ? present.get(n / 2) : present.get(n / 2 - 1) / 2 + present.get(n / 2) / 2;
        double mean = present.stream().mapToDouble(Double::doubleValue).average().orElseThrow();
        return new ColumnStats(n, values.size() - n, present.get(0), present.get(n - 1), mean, median);
    }
}
