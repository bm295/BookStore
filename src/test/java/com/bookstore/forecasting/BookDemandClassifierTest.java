package com.bookstore.forecasting;

import com.bookstore.application.forecasting.BookDemandClassifier;
import com.bookstore.application.forecasting.BookDemandClassifier.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookDemandClassifierTest {
    @Test
    void learnsBothLabelsFromHistoricalExamples() {
        var classifier = new BookDemandClassifier();
        classifier.train(List.of(
                new Sample(new Features(1, 20.0), Label.SLOW_SELLING),
                new Sample(new Features(3, 22.0), Label.SLOW_SELLING),
                new Sample(new Features(25, 21.0), Label.FAST_SELLING),
                new Sample(new Features(30, 19.0), Label.FAST_SELLING)));
        assertEquals(Label.FAST_SELLING, classifier.predict(new Features(27, 20.0)));
        assertEquals(Label.SLOW_SELLING, classifier.predict(new Features(2, 21.0)));
    }

    @Test
    void failedTrainingDoesNotReplaceCurrentModel() {
        var classifier = new BookDemandClassifier();
        var slow = new Sample(new Features(0, 10.0), Label.SLOW_SELLING);
        var fast = new Sample(new Features(20, 10.0), Label.FAST_SELLING);
        assertThrows(IllegalArgumentException.class, () -> classifier.predict(fast.features()));
        classifier.train(List.of(slow, fast));
        assertThrows(IllegalArgumentException.class, () -> classifier.train(List.of(slow)));
        assertEquals(Label.FAST_SELLING, classifier.predict(fast.features()));
        assertThrows(IllegalArgumentException.class, () -> new Features(null, 10.0));
    }
}
