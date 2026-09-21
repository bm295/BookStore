package com.bookstore.application.orders;
import java.math.BigDecimal;
public record RequestOrderDetailLine(int bookId,String bookTitle,String bookAuthor,int quantity,BigDecimal unitPrice,BigDecimal lineTotal) {}
