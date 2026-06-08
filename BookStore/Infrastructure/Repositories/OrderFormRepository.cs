using System.Threading;
using BookStore.Application.Orders;
using BookStore.Infrastructure.Database;
using Microsoft.EntityFrameworkCore;

namespace BookStore.Infrastructure.Repositories;

public sealed class OrderFormRepository
{
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

        var formEntity = await _dbContext.RequestOrderDetailForms
            .Include(f => f.Lines)
            .FirstOrDefaultAsync(f => f.OrderId == command.OrderId, cancellationToken);

        if (formEntity is null)
        {
            return new List<RequestOrderDetailLine>();
        }

        return formEntity.Lines.Select(line => new RequestOrderDetailLine
        {
            BookId = line.BookId,
            Quantity = line.Quantity,
            UnitPrice = line.UnitPrice
        }).ToList();
    }
}
