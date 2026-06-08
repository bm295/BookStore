using BookStore.Domain.Catalog;
using BookStore.Domain.Pricing;

namespace BookStore.Application.Pricing;

/// <summary>
/// Use-case boundary for calculating the current cart total.
/// </summary>
public sealed class CalculateCartPriceUseCase
{
    private readonly IPricingService _pricingService;

    public CalculateCartPriceUseCase(IPricingService pricingService)
    {
        _pricingService = pricingService;
    }

    public static CalculateCartPriceUseCase CreateDefault()
    {
        return new CalculateCartPriceUseCase(new DistinctTitlePricingService(new DistinctTitleDiscountPolicy()));
    }

    public int Execute(IReadOnlyCollection<Book> bookCart)
    {
        return _pricingService.CalculatePrice(bookCart);
    }
}
