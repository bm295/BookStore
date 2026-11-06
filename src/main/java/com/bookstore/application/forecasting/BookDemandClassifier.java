package com.bookstore.application.forecasting;

import java.util.List;

/** Binary nearest-centroid classifier for historical sales observations. */
public final class BookDemandClassifier {
    public enum Label { FAST_SELLING, SLOW_SELLING }

    public record Features(Integer unitsSoldLast30Days, Double price) {
        public Features {
            if (unitsSoldLast30Days == null || unitsSoldLast30Days < 0 ||
                    price == null || !Double.isFinite(price) || price < 0) {
                throw new IllegalArgumentException("Sales and price must be present and nonnegative, and price must be finite.");
            }
        }
    }

    public record Sample(Features features, Label label) {
        public Sample {
            if (features == null || label == null) {
                throw new IllegalArgumentException("Each sample requires features and a label.");
            }
        }
    }

    private record Point(double units, double price) {}

    private record Model(double unitsScale, double priceScale, Point fast, Point slow) {}

    private volatile Model model;

    public void train(List<Sample> samples) {
        if (samples == null || samples.isEmpty() || samples.stream().anyMatch(sample -> sample == null)) {
            throw new IllegalArgumentException("Provide valid samples for both labels.");
        }
        var fast = samples.stream().filter(sample -> sample.label() == Label.FAST_SELLING).toList();
        var slow = samples.stream().filter(sample -> sample.label() == Label.SLOW_SELLING).toList();
        if (fast.isEmpty() || slow.isEmpty()) {
            throw new IllegalArgumentException("Training requires samples for both labels.");
        }
        double unitsScale = Math.max(1, samples.stream().mapToInt(s -> s.features().unitsSoldLast30Days()).max().orElseThrow());
        double priceScale = Math.max(1, samples.stream().mapToDouble(s -> s.features().price()).max().orElseThrow());
        model = new Model(unitsScale, priceScale, centroid(fast, unitsScale, priceScale),
                centroid(slow, unitsScale, priceScale));
    }

    public Label predict(Features features) {
        Model trained = model;
        if (trained == null || features == null) {
            throw new IllegalArgumentException("Train a demand model and provide features before predicting.");
        }
        // Compare squared distances after cancelling the common squared input term.
        // This avoids losing the difference between distances for very large finite prices.
        double units = features.unitsSoldLast30Days() / trained.unitsScale();
        double price = features.price() / trained.priceScale();
        Point fast = trained.fast();
        Point slow = trained.slow();
        double unitsVote = (units - (fast.units() + slow.units()) / 2) * (fast.units() - slow.units());
        double priceVote = (price - (fast.price() + slow.price()) / 2) * (fast.price() - slow.price());
        return unitsVote + priceVote > 0 ? Label.FAST_SELLING : Label.SLOW_SELLING;
    }

    private Point centroid(List<Sample> samples, double unitsScale, double priceScale) {
        double units = samples.stream().mapToDouble(s -> s.features().unitsSoldLast30Days() / unitsScale).average().orElseThrow();
        double price = samples.stream().mapToDouble(s -> s.features().price() / priceScale).average().orElseThrow();
        return new Point(units, price);
    }
}
