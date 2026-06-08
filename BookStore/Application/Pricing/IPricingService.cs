using BookStore.Domain.Catalog;

namespace BookStore.Application.Pricing;

public interface IPricingService
{
    int CalculatePrice(IReadOnlyCollection<Book> bookCart);
}
