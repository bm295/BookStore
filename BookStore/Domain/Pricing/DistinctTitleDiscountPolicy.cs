namespace BookStore.Domain.Pricing;

/// <summary>
/// Encapsulates the legacy distinct-title discount table.
/// </summary>
public sealed class DistinctTitleDiscountPolicy : IDiscountPolicy
{
    private static readonly IReadOnlyDictionary<int, decimal> DiscountRates = new Dictionary<int, decimal>
    {
        [1] = 0.00m,
        [2] = 0.05m,
        [3] = 0.10m,
        [4] = 0.20m,
        [5] = 0.25m
    };

    public decimal GetDiscountRate(int distinctTitleCount)
    {
        return DiscountRates.TryGetValue(distinctTitleCount, out var discountRate)
            ? discountRate
            : 0.00m;
    }
}
