package com.bookstore.application.orders;
import java.time.Instant; import java.util.List;
public record RequestOrderDetailForm(String orderId, Instant requestedAtUtc, List<RequestOrderDetailLine> lines) {}
