package com.bookstore.web;

import com.bookstore.application.pricing.CalculateCartPriceUseCase;
import com.bookstore.domain.catalog.Book;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pricing")
public class PricingController {
    private final CalculateCartPriceUseCase pricing = CalculateCartPriceUseCase.createDefault();

    @PostMapping("/cart")
    public PriceResponse calculate(@RequestBody List<CartItem> items) {
        var books = items.stream().map(item -> {
            var book = new Book();
            book.setId(item.bookId());
            book.setPrice(item.price());
            return book;
        }).toList();
        return new PriceResponse(pricing.execute(books));
    }

    public record CartItem(int bookId, int price) {}
    public record PriceResponse(int total) {}
}
