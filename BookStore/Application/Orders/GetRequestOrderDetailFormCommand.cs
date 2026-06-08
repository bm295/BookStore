namespace BookStore.Application.Orders;

public sealed record GetRequestOrderDetailFormCommand
{
    public string? OrderId { get; init; }
}
