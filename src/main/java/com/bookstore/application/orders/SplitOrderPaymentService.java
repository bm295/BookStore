package com.bookstore.application.orders;

import com.bookstore.infrastructure.database.Entities.OrderLine;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/** Calculates one customer's fair share after excluding the item they did not consume. */
public final class SplitOrderPaymentService {
    public PaymentResult calculate(
            List<OrderLine> lines,
            int excludedLineIndex,
            BigDecimal chargedAmount) {
        Objects.requireNonNull(lines, "lines");
        Objects.requireNonNull(chargedAmount, "chargedAmount");
        if (chargedAmount.signum() < 0) {
            throw new IllegalArgumentException("Charged amount cannot be negative.");
        }
        if (excludedLineIndex < 0 || excludedLineIndex >= lines.size()) {
            throw new IndexOutOfBoundsException("Excluded line index is outside the order.");
        }

        BigDecimal sharedTotal = BigDecimal.ZERO;
        for (int i = 0; i < lines.size(); i++) {
            if (i != excludedLineIndex) {
                var line = Objects.requireNonNull(lines.get(i), "lines cannot contain null");
                if (line.quantity() < 0 || line.unitPrice().signum() < 0) {
                    throw new IllegalArgumentException("Order line quantity and price cannot be negative.");
                }
                sharedTotal = sharedTotal.add(
                        line.unitPrice().multiply(BigDecimal.valueOf(line.quantity())));
            }
        }

        var fairShare = sharedTotal.divide(BigDecimal.valueOf(2));
        var refund = chargedAmount.subtract(fairShare);
        var status = refund.signum() == 0
                ? PaymentStatus.EXACT_PAYMENT
                : refund.signum() > 0 ? PaymentStatus.REFUND_DUE : PaymentStatus.UNDERPAID;
        return new PaymentResult(fairShare, refund, status);
    }

    public record PaymentResult(BigDecimal fairShare, BigDecimal refund, PaymentStatus status) {
        public PaymentResult {
            Objects.requireNonNull(fairShare, "fairShare");
            Objects.requireNonNull(refund, "refund");
            Objects.requireNonNull(status, "status");
        }
    }
}
