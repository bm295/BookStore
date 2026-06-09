namespace BookStore.Application.Orders;

public sealed record RequestOrderDetailLine
{
    public int BookId { get; init; }
    public string BookTitle { get; init; } = string.Empty;
    public string BookAuthor { get; init; } = string.Empty;
    public int Quantity { get; init; }
    public decimal UnitPrice { get; init; }
    public decimal LineTotal { get; init; }
}

public sealed record RequestOrderDetailForm
{
    public string OrderId { get; init; } = string.Empty;
    public DateTime RequestedAtUtc { get; init; }
    public IReadOnlyList<RequestOrderDetailLine> Lines { get; init; } = Array.Empty<RequestOrderDetailLine>();
}
