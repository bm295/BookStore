namespace BookStore;

/// <summary>
/// Backward-compatible facade for existing callers. New code should prefer
/// <see cref="Domain.Catalog.Book" />.
/// </summary>
public class Book : Domain.Catalog.Book
{
}
