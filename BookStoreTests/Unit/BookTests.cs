using BookStore.Domain.Catalog;
using BookStore.Domain.Errors;
using Xunit;

namespace BookStoreTests.Unit;

public class BookTests
{
    [Fact]
    public void Price_WhenNegative_ThrowsDomainValidationException()
    {
        var book = new Book();

        var exception = Assert.Throws<DomainValidationException>(() => book.Price = -1);

        Assert.Equal("Book price cannot be negative.", exception.Message);
    }
}
