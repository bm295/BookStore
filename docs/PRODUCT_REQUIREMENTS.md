# Product Requirements Document (PRD)

## 1) Vision
Build a BookStore management application that allows bookstore operators to manage catalog, inventory, pricing, and customer purchases with clear business rules and reliable automation.

## 2) Target Users
- **Store Admin**: manages inventory, pricing, discounts, and reports.
- **Cashier/Operator**: builds carts and checks out orders.
- **System Integrator (future)**: imports/exports catalog and sales data.

## 3) In-Scope Capabilities (MVP to v1)

### 3.1 Catalog Management
- Add, update, remove books.
- Store metadata: id, ISBN, title, author, category, base price.
- Ensure unique identifiers (internal ID + ISBN).

### 3.2 Inventory Management
- Track stock quantity by book.
- Adjust stock for inbound purchases and sales.
- Prevent negative stock at checkout.
- Support low-stock alerts (threshold-based).

### 3.3 Pricing & Promotions
- Base price per title.
- Bundle discounts for distinct titles (existing concept).
- Extensible promotion engine for future rule types.
- Deterministic total price calculation.

### 3.4 Cart & Checkout
- Add/remove books in cart.
- Calculate subtotal, discounts, grand total.
- Validate stock during checkout.
- Produce order summary with line items.

### 3.5 Persistence & I/O
- Read/write data from local storage (file-based initially).
- Input validation and meaningful error messages.
- Safe handling of malformed files.

### 3.6 Reporting (v1)
- Daily sales summary.
- Top-selling books.
- Inventory snapshot.

### 3.7 Business Sequencing
- Generate stable, unique identifiers for books and orders without exposing user-controlled sequence values.
- Preserve cart and order line ordering so operator-facing summaries match the checkout flow.
- Apply checkout steps in a deterministic sequence: validate cart, validate stock, price the cart, create the order, decrement inventory, then persist the order.
- Apply promotion rules in explicit priority order so pricing remains repeatable as new discounts are added.

## 4) Non-Functional Requirements
- **Correctness**: discount and totals must be test-validated.
- **Maintainability**: clear boundaries and low coupling.
- **Testability**: business rules should be unit-test friendly.
- **Performance**: cart pricing under 100 ms for 1,000 line items (target).
- **Reliability**: no data corruption in normal read/write flows.

## 5) Constraints
- Current stack: C# / .NET 10 preview.
- Start as library-focused domain logic before full UI/API.
- Use TDD for domain and pricing rules.

## 6) Acceptance Criteria (System-level)
1. A user can manage books with validated metadata.
2. Cart pricing is accurate for normal and discounted scenarios.
3. Checkout cannot succeed when stock is insufficient.
4. All critical business rules are covered by automated tests.
5. Documentation reflects current behavior and architecture.

## 7) Out of Scope (for now)
- Authentication/authorization.
- Multi-store tenancy.
- Payment gateway integration.
- Cloud-native distributed architecture.

## 8) Success Metrics
- >90% pass rate on first run in CI for default branch.
- 0 known critical pricing defects.
- Unit test coverage goal: >=80% for domain layer.
- Mean time to implement a new discount rule: <1 developer day.
