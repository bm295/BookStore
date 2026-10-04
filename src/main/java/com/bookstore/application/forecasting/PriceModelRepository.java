package com.bookstore.application.forecasting;

import java.util.Optional;

public interface PriceModelRepository {
    Optional<PriceModel> load();
    void save(PriceModel model);
}
