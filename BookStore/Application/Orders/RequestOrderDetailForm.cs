namespace BookStore.Application.Orders;

public sealed record RequestOrderDetailLine
{
    public int BookId { get; init; }
    public int Quantity { get; init; }
    public decimal UnitPrice { get; init; }
}

public sealed record RequestOrderDetailForm
{
    public string OrderId { get; init; } = string.Empty;
    public DateTime RequestedAtUtc { get; init; }
    public IReadOnlyList<RequestOrderDetailLine> Lines { get; init; } = Array.Empty<RequestOrderDetailLine>();
}
