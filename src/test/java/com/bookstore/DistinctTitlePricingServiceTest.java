package com.bookstore;
import com.bookstore.application.pricing.*; import com.bookstore.domain.catalog.Book; import com.bookstore.domain.pricing.*; import org.junit.jupiter.api.Test; import java.util.*; import static org.junit.jupiter.api.Assertions.*;
class DistinctTitlePricingServiceTest {
  private final PricingService service=new DistinctTitlePricingService(new DistinctTitleDiscountPolicy());
  private Book book(int id){var b=new Book(); b.setId(id); b.setPrice(100); return b;}
  @Test void emptyCartIsZero(){assertEquals(0,service.calculatePrice(List.of()));}
  @Test void twoDistinctTitlesGetFivePercentDiscount(){assertEquals(190,service.calculatePrice(List.of(book(1),book(2))));}
  @Test void moreThanFiveDistinctTitlesKeepsLegacyZero(){assertEquals(0,service.calculatePrice(Arrays.asList(book(1),book(2),book(3),book(4),book(5),book(6))));}
  @Test void negativePriceIsRejected(){var b=new Book(); assertThrows(RuntimeException.class,()->b.setPrice(-1));}
}
