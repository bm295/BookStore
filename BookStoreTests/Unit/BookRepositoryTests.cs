using BookStore.Domain.Catalog;
using BookStore.Infrastructure.Repositories;
using Xunit;

namespace BookStoreTests.Unit;

public class BookRepositoryTests
{
    [Fact]
    public void GetAll_WhenNoFile_ReturnsEmptyCollection()
    {
        var dataDirectory = Path.Combine(Path.GetTempPath(), Guid.NewGuid().ToString("N"));
        var repository = new BookRepository(dataDirectory);

        var books = repository.GetAll();

        Assert.Empty(books);
    }

    [Fact]
    public void SaveAll_PersistsBooks_AndGetAllReadsThemBack()
    {
        var dataDirectory = Path.Combine(Path.GetTempPath(), Guid.NewGuid().ToString("N"));
        var repository = new BookRepository(dataDirectory);
        var book = new Book { Id = 1, Price = 19 };

        repository.SaveAll(new[] { book });

        var savedBooks = repository.GetAll().ToArray();

        Assert.Single(savedBooks);
        Assert.Equal(1, savedBooks[0].Id);
        Assert.Equal(19, savedBooks[0].Price);
    }
}
