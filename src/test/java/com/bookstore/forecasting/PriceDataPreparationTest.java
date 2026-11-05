package com.bookstore.forecasting;

import com.bookstore.application.forecasting.PriceDataPreparation;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PriceDataPreparationTest {
    private final PriceDataPreparation service = new PriceDataPreparation();

    @Test void reportsOriginalMissingValuesAndImputesEligibleRows() {
        var result = service.prepare(List.of(
                new PriceDataPreparation.Row(100, 0, 20.0),
                new PriceDataPreparation.Row(200, 3, 40.0),
                new PriceDataPreparation.Row(null, null, 30.0),
                new PriceDataPreparation.Row(999, 99, null)));
        assertEquals(3, result.samples().size());
        assertEquals(150, result.samples().get(2).features().pageCount());
        assertEquals(2, result.samples().get(2).features().publicationAgeYears());
        assertEquals(1, result.report().droppedRowsMissingPrice());
        assertEquals(1, result.report().imputedPageCount());
        assertEquals(1, result.report().pageCount().missing());
        assertEquals(200.0, result.report().pageCount().median());
        assertEquals(30.0, result.report().price().mean());
    }

    @Test void rejectsMissingTargetOrUnusableFeaturesAndInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> service.prepare(List.of(new PriceDataPreparation.Row(1, 0, null))));
        assertThrows(IllegalArgumentException.class, () -> service.prepare(List.of(new PriceDataPreparation.Row(null, 0, 10.0))));
        assertThrows(IllegalArgumentException.class, () -> service.prepare(List.of(new PriceDataPreparation.Row(1, 0, Double.NaN))));
        assertThrows(IllegalArgumentException.class, () -> service.prepare(List.of(new PriceDataPreparation.Row(-1, 0, 1.0))));
        assertThrows(IllegalArgumentException.class, () -> service.prepare(List.of()));
    }
}
