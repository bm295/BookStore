package com.bookstore.domain.inventory;

import com.bookstore.domain.errors.DomainValidationException;

public record InventoryItem(int bookId, int quantityOnHand, int reorderThreshold) {
    public InventoryItem {
        if (bookId <= 0 || quantityOnHand < 0 || reorderThreshold < 0) {
            throw new DomainValidationException("Inventory values must be nonnegative and book ID must be positive.");
        }
    }

    public boolean isLowStock() {
        return quantityOnHand <= reorderThreshold;
    }

    public InventoryItem adjustBy(int quantityDelta) {
        long newQuantity = (long) quantityOnHand + quantityDelta;
        if (newQuantity < 0 || newQuantity > Integer.MAX_VALUE) {
            throw new DomainValidationException("Adjusted stock is outside the supported range.");
        }
        return new InventoryItem(bookId, (int) newQuantity, reorderThreshold);
    }
}
