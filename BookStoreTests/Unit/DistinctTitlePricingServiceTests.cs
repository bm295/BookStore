using BookStore.Application.Pricing;
using BookStore.Domain.Catalog;
using BookStore.Domain.Pricing;
using Xunit;

namespace BookStoreTests.Unit;

public class DistinctTitlePricingServiceTests
{
    [Fact]
    public void CalculatePrice_EmptyCart_ReturnsZero()
    {
        var service = CreateService();

        var actual = service.CalculatePrice(Array.Empty<Book>());

        Assert.Equal(0, actual);
    }

    [Fact]
    public void CalculatePrice_TwoDistinctTitles_AppliesFivePercentDiscount()
    {
        var service = CreateService();
        var cart = new[]
        {
            new Book { Id = 1, Price = 100 },
            new Book { Id = 2, Price = 100 }
        };

        var actual = service.CalculatePrice(cart);

        Assert.Equal(190, actual);
    }

    [Fact]
    public void CalculatePrice_MoreThanFiveDistinctTitles_PreservesLegacyZeroTotal()
    {
        var service = CreateService();
        var cart = Enumerable.Range(1, 6)
            .Select(id => new Book { Id = id, Price = 100 })
            .ToArray();

        var actual = service.CalculatePrice(cart);

        Assert.Equal(0, actual);
    }

    private static DistinctTitlePricingService CreateService()
    {
        return new DistinctTitlePricingService(new DistinctTitleDiscountPolicy());
    }
}
