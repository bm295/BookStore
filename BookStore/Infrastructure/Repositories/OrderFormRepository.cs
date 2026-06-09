using System.Threading;
using BookStore.Application.Orders;
using BookStore.Infrastructure.Database;
using Microsoft.EntityFrameworkCore;

namespace BookStore.Infrastructure.Repositories;

public sealed class OrderFormRepository
{
    private const string UnknownAuthor = "Unknown author";
    private const string UnknownBook = "Unknown book";
    private readonly BookStoreDbContext _dbContext;

    public OrderFormRepository(BookStoreDbContext dbContext)
    {
        _dbContext = dbContext ?? throw new ArgumentNullException(nameof(dbContext));
    }

    public async Task<List<RequestOrderDetailLine>> GetRequestOrderDetailFormAsync(
        GetRequestOrderDetailFormCommand command,
        CancellationToken cancellationToken = default)
    {
        ArgumentNullException.ThrowIfNull(command);

        if (string.IsNullOrWhiteSpace(command.OrderId))
        {
            return new List<RequestOrderDetailLine>();
        }

        var formId = await _dbContext.RequestOrderDetailForms
            .Where(form => form.OrderId == command.OrderId)
            .Select(form => (int?)form.Id)
            .FirstOrDefaultAsync(cancellationToken);

        if (formId is null)
        {
            return new List<RequestOrderDetailLine>();
        }

        var detailLines = await (
            from line in _dbContext.RequestOrderDetailLines
            where line.RequestOrderDetailFormEntityId == formId.Value
            join book in _dbContext.CatalogBooks
                on line.BookId equals book.BookId into catalogMatches
            from catalogBook in catalogMatches.DefaultIfEmpty()
            orderby line.Id
            select new
            {
                line.BookId,
                line.Quantity,
                line.UnitPrice,
                BookTitle = catalogBook == null ? UnknownBook : catalogBook.Title,
                BookAuthor = catalogBook == null ? UnknownAuthor : catalogBook.Author
            }).ToListAsync(cancellationToken);

        return detailLines.Select(line => new RequestOrderDetailLine
        {
            BookId = line.BookId,
            BookTitle = line.BookTitle,
            BookAuthor = line.BookAuthor,
            Quantity = line.Quantity,
            UnitPrice = line.UnitPrice,
            LineTotal = line.Quantity * line.UnitPrice
        }).ToList();
    }
}
