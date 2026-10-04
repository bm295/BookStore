package com.bookstore.application.forecasting;

import java.util.List;

public interface BookPricePredictor {
    PriceModel train(List<PriceSample> samples);
    double predict(PriceModel model, BookFeatures features);
}
