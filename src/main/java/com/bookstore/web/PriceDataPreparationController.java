package com.bookstore.web;

import com.bookstore.application.forecasting.PriceDataPreparation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/pricing/suggestions")
public class PriceDataPreparationController {
    private final PriceDataPreparation service = new PriceDataPreparation();

    @PostMapping("/prepare")
    public PriceDataPreparation.Result prepare(@RequestBody List<PriceDataPreparation.Row> rows) {
        return service.prepare(rows);
    }
}
