package com.bookstore.domain.pricing;
import java.util.Map;
public final class DistinctTitleDiscountPolicy implements DiscountPolicy {
  private static final Map<Integer,Double> RATES=Map.of(1,0d,2,.05d,3,.10d,4,.20d,5,.25d);
  public double getDiscountRate(int count){return RATES.getOrDefault(count,0d);}
}
