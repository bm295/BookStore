package com.bookstore.application.orders;

import com.bookstore.infrastructure.database.Entities.OrderLine;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SplitOrderPaymentServiceTest {
    private final SplitOrderPaymentService service = new SplitOrderPaymentService();

    @Test
    void returnsExactPaymentStatusWhenChargedAmountEqualsFairShare() {
        var result = service.calculate(List.of(line(10), line(20), line(30)), 1, money("20"));

        assertEquals(money("20"), result.fairShare());
        assertEquals(money("0"), result.refund());
        assertEquals(PaymentStatus.EXACT_PAYMENT, result.status());
    }

    @Test
    void returnsRefundWhenCustomerWasOvercharged() {
        var result = service.calculate(List.of(line(10), line(20), line(30)), 1, money("25"));

        assertEquals(money("5"), result.refund());
        assertEquals(PaymentStatus.REFUND_DUE, result.status());
    }

    @Test
    void excludesTheSelectedLineFromTheSharedTotal() {
        var result = service.calculate(List.of(line(10), line(20), line(30)), 0, money("25"));

        assertEquals(money("0"), result.refund());
        assertEquals(PaymentStatus.EXACT_PAYMENT, result.status());
    }

    @Test
    void rejectsInvalidLineIndexAndNegativePayment() {
        assertThrows(IndexOutOfBoundsException.class,
                () -> service.calculate(List.of(line(10)), 1, money("5")));
        assertThrows(IllegalArgumentException.class,
                () -> service.calculate(List.of(line(10)), 0, money("-1")));
    }

    private static OrderLine line(int unitPrice) {
        return new OrderLine(1, 1, money(Integer.toString(unitPrice)), 1);
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }
}
