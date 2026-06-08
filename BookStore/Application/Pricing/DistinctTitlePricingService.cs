using BookStore.Domain.Catalog;
using BookStore.Domain.Pricing;

namespace BookStore.Application.Pricing;

/// <summary>
/// Application service that orchestrates cart pricing without owning discount policy details.
/// </summary>
public sealed class DistinctTitlePricingService : IPricingService
{
    private readonly IDiscountPolicy _discountPolicy;

    public DistinctTitlePricingService(IDiscountPolicy discountPolicy)
    {
        _discountPolicy = discountPolicy;
    }

    public int CalculatePrice(IReadOnlyCollection<Book> bookCart)
    {
        ArgumentNullException.ThrowIfNull(bookCart);

        if (bookCart.Count == 0)
        {
            return 0;
        }

        var bookPrice = bookCart.First().Price;
        var distinctTitleCount = bookCart.Select(book => book.Id).Distinct().Count();
        if (distinctTitleCount > 5)
        {
            return 0;
        }

        var normalBookCount = bookCart.Count - distinctTitleCount;
        var discountRate = _discountPolicy.GetDiscountRate(distinctTitleCount);
        var discountedTotal = distinctTitleCount * bookPrice * (1 - discountRate);
        var undiscountedTotal = normalBookCount * bookPrice;

        return (int)(discountedTotal + undiscountedTotal);
    }
}
