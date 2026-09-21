package com.bookstore;
import java.util.*;
public final class BookIdSequence implements Iterable<Integer> { private final int start,count; public BookIdSequence(int start,int count){if(count<0)throw new IllegalArgumentException("Count must be zero or greater.");this.start=start;this.count=count;} public Iterator<Integer> iterator(){return new Iterator<>(){int i; public boolean hasNext(){return i<count;} public Integer next(){if(!hasNext())throw new NoSuchElementException();return start+i++;}};} }
