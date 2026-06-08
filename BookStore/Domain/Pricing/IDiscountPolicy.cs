namespace BookStore.Domain.Pricing;

public interface IDiscountPolicy
{
    decimal GetDiscountRate(int distinctTitleCount);
}
