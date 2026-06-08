using BookStore.Application.Pricing;

namespace BookStore;

/// <summary>
/// Backward-compatible facade over the application pricing use case.
/// </summary>
public class BookStoreEngine
{
    private readonly CalculateCartPriceUseCase _calculateCartPrice;

    public BookStoreEngine() : this(CalculateCartPriceUseCase.CreateDefault())
    {
    }

    public BookStoreEngine(CalculateCartPriceUseCase calculateCartPrice)
    {
        _calculateCartPrice = calculateCartPrice;
    }

    public int CalculatePrice(List<Book> bookCart)
    {
        return _calculateCartPrice.Execute(bookCart);
    }
}
