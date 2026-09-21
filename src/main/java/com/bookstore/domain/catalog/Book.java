package com.bookstore.domain.catalog;
import com.bookstore.domain.errors.DomainValidationException;
public class Book {
  private int id; private int price;
  public int getId(){return id;} public void setId(int id){this.id=id;}
  public int getPrice(){return price;}
  public void setPrice(int price){if(price<0) throw new DomainValidationException("Book price cannot be negative."); this.price=price;}
}
