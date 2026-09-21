package com.bookstore.application.pricing;
import com.bookstore.domain.catalog.Book; import com.bookstore.domain.pricing.DiscountPolicy; import java.util.*;
public final class DistinctTitlePricingService implements PricingService {
  private final DiscountPolicy policy; public DistinctTitlePricingService(DiscountPolicy policy){this.policy=Objects.requireNonNull(policy);}
  public int calculatePrice(Collection<? extends Book> cart){Objects.requireNonNull(cart); if(cart.isEmpty())return 0; int price=cart.iterator().next().getPrice(); int distinct=(int)cart.stream().map(Book::getId).distinct().count(); if(distinct>5)return 0; int normal=cart.size()-distinct; return (int)(distinct*price*(1-policy.getDiscountRate(distinct))+normal*price);}
}
