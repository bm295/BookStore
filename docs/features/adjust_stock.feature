Feature: Adjust stock for a book
  As a store admin
  I want to adjust a book's available inventory
  So that stock levels and low-stock status stay current

  The adjustment takes a BookId, a QuantityDelta, and an optional reason.
  The reorder threshold belongs to the inventory item and is not changed by an adjustment.
  Stock is low when units on hand are less than or equal to the reorder threshold.

  Scenario: Increasing available stock
    Given a book exists with 8 units on hand and a reorder threshold of 5
    When the store admin adjusts its stock by 2 units
    Then the book has 10 units on hand
    And the book is not marked as low stock

  Scenario: An adjustment brings stock down to the reorder threshold
    Given a book exists with 8 units on hand and a reorder threshold of 5
    When the store admin adjusts its stock by -3 units
    Then the book has 5 units on hand
    And the book is marked as low stock

  Scenario: An adjustment cannot make stock negative
    Given a book exists with 2 units on hand
    When the store admin adjusts its stock by -3 units
    Then the adjustment fails with a DomainValidationException
    And the book still has 2 units on hand

  Scenario: An adjustment requires an existing book
    Given no book exists with the requested BookId
    When the store admin adjusts its stock by 1 unit
    Then the adjustment fails with an EntityNotFoundException
