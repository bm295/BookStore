package com.bookstore.web;

import com.bookstore.application.forecasting.BookDemandClassifier;
import com.bookstore.application.forecasting.BookDemandClassifier.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books/demand")
public class BookDemandController {
    private final BookDemandClassifier classifier = new BookDemandClassifier();

    @PostMapping("/train")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void train(@RequestBody List<Sample> samples) {
        classifier.train(samples);
    }

    @PostMapping("/predict")
    public Prediction predict(@RequestBody Features features) {
        return new Prediction(classifier.predict(features));
    }

    public record Prediction(Label label) {}
}
