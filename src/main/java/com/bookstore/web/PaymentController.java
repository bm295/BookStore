package com.bookstore.web;

import com.bookstore.application.orders.SplitOrderPaymentService;
import com.bookstore.application.orders.PaymentStatus;
import com.bookstore.infrastructure.database.Entities.OrderLine;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final SplitOrderPaymentService splitPayment = new SplitOrderPaymentService();

    @PostMapping("/split")
    public SplitPaymentResponse split(@RequestBody SplitPaymentRequest request) {
        var lines = request.lines().stream()
                .map(line -> new OrderLine(line.bookId(), line.quantity(), line.unitPrice(), line.sequence()))
                .toList();
        var result = splitPayment.calculate(lines, request.excludedLineIndex(), request.chargedAmount());
        return new SplitPaymentResponse(result.fairShare(), result.refund(), result.status(), messageFor(result.status()));
    }

    private String messageFor(PaymentStatus status) {
        return switch (status) {
            case EXACT_PAYMENT -> "Đã thanh toán đủ";
            case REFUND_DUE -> "Cần hoàn tiền";
            case UNDERPAID -> "Thanh toán chưa đủ";
        };
    }

    public record SplitPaymentRequest(
            List<PaymentLine> lines,
            int excludedLineIndex,
            BigDecimal chargedAmount) {}

    public record PaymentLine(int bookId, int quantity, BigDecimal unitPrice, int sequence) {}

    public record SplitPaymentResponse(
            BigDecimal fairShare,
            BigDecimal refund,
            PaymentStatus status,
            String message) {}
}
