using BookStore.Domain.Errors;

namespace BookStore.Domain.Catalog;

/// <summary>
/// Domain representation of a catalog book.
/// </summary>
public class Book
{
    public int Id { get; set; }

    /// <summary>
    /// Backward-compatible integer price used by the current kata pricing rules.
    /// Future slices can promote this to richer Money/decimal behavior.
    /// </summary>
    public int Price
    {
        get => _price;
        set
        {
            if (value < 0)
            {
                throw new DomainValidationException("Book price cannot be negative.");
            }

            _price = value;
        }
    }

    private int _price;
}
