using BookStore.Application.Orders;
using BookStore.Infrastructure.Database;
using BookStore.Infrastructure.Repositories;
using Microsoft.EntityFrameworkCore;
using System.Threading;
using Xunit;

namespace BookStoreTests.Unit;

public class OrderFormRepositoryTests
{
    [Fact]
    public async Task GetRequestOrderDetailFormAsync_WhenOrderIdMissing_ReturnsEmptyList()
    {
        var options = new DbContextOptionsBuilder<BookStoreDbContext>()
            .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString("N"))
            .Options;

        await using var context = new BookStoreDbContext(options);
        var repository = new OrderFormRepository(context);

        var lines = await repository.GetRequestOrderDetailFormAsync(
            new GetRequestOrderDetailFormCommand(),
            CancellationToken.None);

        Assert.Empty(lines);
    }

    [Fact]
    public async Task GetRequestOrderDetailFormAsync_WhenFormExists_ReturnsLineItemsWithCatalogDetails()
    {
        var options = new DbContextOptionsBuilder<BookStoreDbContext>()
            .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString("N"))
            .Options;

        await using var context = new BookStoreDbContext(options);
        var expectedOrderId = "test-order";
        context.CatalogBooks.Add(new CatalogBookEntity
        {
            BookId = 1,
            Title = "Domain-Driven BookStore",
            Author = "Ada Lovelace"
        });
        context.RequestOrderDetailForms.Add(new RequestOrderDetailFormEntity
        {
            OrderId = expectedOrderId,
            RequestedAtUtc = DateTime.Parse("2026-06-08T00:00:00Z"),
            Lines = new List<RequestOrderDetailLineEntity>
            {
                new() { BookId = 1, Quantity = 2, UnitPrice = 10.5m }
            }
        });

        await context.SaveChangesAsync();
        var repository = new OrderFormRepository(context);

        var lines = await repository.GetRequestOrderDetailFormAsync(
            new GetRequestOrderDetailFormCommand { OrderId = expectedOrderId },
            CancellationToken.None);

        Assert.Single(lines);
        Assert.Equal(1, lines[0].BookId);
        Assert.Equal(2, lines[0].Quantity);
        Assert.Equal(10.5m, lines[0].UnitPrice);
        Assert.Equal(21.0m, lines[0].LineTotal);
        Assert.Equal("Domain-Driven BookStore", lines[0].BookTitle);
        Assert.Equal("Ada Lovelace", lines[0].BookAuthor);
    }

    [Fact]
    public async Task GetRequestOrderDetailFormAsync_WhenCatalogBookMissing_ReturnsFallbackCatalogDetails()
    {
        var options = new DbContextOptionsBuilder<BookStoreDbContext>()
            .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString("N"))
            .Options;

        await using var context = new BookStoreDbContext(options);
        var expectedOrderId = "test-order-with-missing-book";
        context.RequestOrderDetailForms.Add(new RequestOrderDetailFormEntity
        {
            OrderId = expectedOrderId,
            RequestedAtUtc = DateTime.Parse("2026-06-08T00:00:00Z"),
            Lines = new List<RequestOrderDetailLineEntity>
            {
                new() { BookId = 404, Quantity = 3, UnitPrice = 7.25m }
            }
        });

        await context.SaveChangesAsync();
        var repository = new OrderFormRepository(context);

        var lines = await repository.GetRequestOrderDetailFormAsync(
            new GetRequestOrderDetailFormCommand { OrderId = expectedOrderId },
            CancellationToken.None);

        Assert.Single(lines);
        Assert.Equal(404, lines[0].BookId);
        Assert.Equal("Unknown book", lines[0].BookTitle);
        Assert.Equal("Unknown author", lines[0].BookAuthor);
        Assert.Equal(21.75m, lines[0].LineTotal);
    }
}
