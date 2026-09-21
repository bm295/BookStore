package com.bookstore.application.pricing;
import com.bookstore.domain.catalog.Book; import com.bookstore.domain.pricing.*; import java.util.Collection;
public final class CalculateCartPriceUseCase { private final PricingService service; public CalculateCartPriceUseCase(PricingService service){this.service=service;} public static CalculateCartPriceUseCase createDefault(){return new CalculateCartPriceUseCase(new DistinctTitlePricingService(new DistinctTitleDiscountPolicy()));} public int execute(Collection<? extends Book> cart){return service.calculatePrice(cart);} }
