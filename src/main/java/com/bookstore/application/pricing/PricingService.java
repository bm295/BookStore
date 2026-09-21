package com.bookstore.application.pricing;
import com.bookstore.domain.catalog.Book; import java.util.Collection;
public interface PricingService { int calculatePrice(Collection<? extends Book> cart); }
