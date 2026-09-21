package com.bookstore.infrastructure.database;
import java.math.BigDecimal; import java.time.Instant; import java.util.*;
public final class Entities { private Entities(){} public record CatalogBook(int bookId,String title,String author){} public record OrderForm(String orderId,Instant requestedAtUtc,List<OrderLine> lines){} public record OrderLine(int bookId,int quantity,BigDecimal unitPrice,int sequence){} public record LanguageMaster(String code,String name){} public record LanguageResource(String code,String key,String value){} }
