package com.bookstore.infrastructure.forecasting;

import com.bookstore.application.forecasting.*;
import java.util.Arrays;
import java.util.List;

/** Normalized cubic ridge regression. All training state is local to the invocation. */
public final class PolynomialBookPricePredictor implements BookPricePredictor {
    private static final int TERMS = 10;
    private static final double RIDGE = 1e-6;

    @Override public PriceModel train(List<PriceSample> samples) {
        if (samples == null || samples.size() < TERMS || samples.stream().anyMatch(x -> x == null))
            throw new IllegalArgumentException("At least 10 valid training samples are required.");
        double[] means = new double[2];
        for (var sample : samples) {
            means[0] += sample.features().pageCount() / (double) samples.size();
            means[1] += sample.features().publicationAgeYears() / (double) samples.size();
        }
        double[] scales = new double[2];
        for (var sample : samples) {
            double x = sample.features().pageCount() - means[0];
            double y = sample.features().publicationAgeYears() - means[1];
            scales[0] += x * x / samples.size();
            scales[1] += y * y / samples.size();
        }
        for (int i = 0; i < 2; i++) scales[i] = scales[i] == 0 ? 1 : Math.sqrt(scales[i]);
        double[][] matrix = new double[TERMS][TERMS];
        double[] rhs = new double[TERMS];
        for (var sample : samples) {
            double[] terms = terms(sample.features(), means, scales);
            for (int i = 0; i < TERMS; i++) {
                rhs[i] += terms[i] * sample.price() / samples.size();
                for (int j = 0; j < TERMS; j++) matrix[i][j] += terms[i] * terms[j] / samples.size();
            }
        }
        // Do not penalize the intercept.
        for (int i = 1; i < TERMS; i++) matrix[i][i] += RIDGE;
        return new PriceModel(1, boxed(means), boxed(scales), boxed(solve(matrix, rhs)));
    }

    @Override public double predict(PriceModel model, BookFeatures features) {
        if (model == null || features == null) throw new IllegalArgumentException("Model and features are required.");
        double[] terms = terms(features, unboxed(model.means()), unboxed(model.scales()));
        double result = 0;
        for (int i = 0; i < TERMS; i++) result += terms[i] * model.coefficients().get(i);
        if (!Double.isFinite(result)) throw new IllegalArgumentException("Prediction is not finite.");
        return result;
    }

    private static double[] terms(BookFeatures f, double[] means, double[] scales) {
        double x = (f.pageCount() - means[0]) / scales[0];
        double y = (f.publicationAgeYears() - means[1]) / scales[1];
        return new double[]{1, x, y, x*x, x*y, y*y, x*x*x, x*x*y, x*y*y, y*y*y};
    }

    private static double[] solve(double[][] a, double[] b) {
        for (int column = 0; column < TERMS; column++) {
            int pivot = column;
            for (int row = column + 1; row < TERMS; row++)
                if (Math.abs(a[row][column]) > Math.abs(a[pivot][column])) pivot = row;
            double[] swap = a[column]; a[column] = a[pivot]; a[pivot] = swap;
            double value = b[column]; b[column] = b[pivot]; b[pivot] = value;
            if (!Double.isFinite(a[column][column]) || Math.abs(a[column][column]) < 1e-12)
                throw new IllegalArgumentException("Training data produces an unstable model.");
            for (int row = column + 1; row < TERMS; row++) {
                double factor = a[row][column] / a[column][column];
                for (int j = column; j < TERMS; j++) a[row][j] -= factor * a[column][j];
                b[row] -= factor * b[column];
            }
        }
        double[] result = new double[TERMS];
        for (int row = TERMS - 1; row >= 0; row--) {
            double value = b[row];
            for (int j = row + 1; j < TERMS; j++) value -= a[row][j] * result[j];
            result[row] = value / a[row][row];
        }
        return result;
    }

    private static List<Double> boxed(double[] values) { return Arrays.stream(values).boxed().toList(); }
    private static double[] unboxed(List<Double> values) { return values.stream().mapToDouble(Double::doubleValue).toArray(); }
}
