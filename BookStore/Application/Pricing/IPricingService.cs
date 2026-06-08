using DomainBook = BookStore.Domain.Catalog.Book;

namespace BookStore.Application.Pricing;

public interface IPricingService
{
    int CalculatePrice(IReadOnlyCollection<DomainBook> bookCart);
}
