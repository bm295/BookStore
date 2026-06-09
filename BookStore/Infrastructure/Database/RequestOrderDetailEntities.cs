namespace BookStore.Infrastructure.Database;

public sealed class RequestOrderDetailFormEntity
{
    public int Id { get; set; }
    public string OrderId { get; set; } = string.Empty;
    public DateTime RequestedAtUtc { get; set; }
    public List<RequestOrderDetailLineEntity> Lines { get; set; } = new();
}

public sealed class RequestOrderDetailLineEntity
{
    public int Id { get; set; }
    public int RequestOrderDetailFormEntityId { get; set; }
    public RequestOrderDetailFormEntity Form { get; set; } = null!;
    public int BookId { get; set; }
    public int Quantity { get; set; }
    public decimal UnitPrice { get; set; }
}

public sealed class CatalogBookEntity
{
    public int Id { get; set; }
    public int BookId { get; set; }
    public string Title { get; set; } = string.Empty;
    public string Author { get; set; } = string.Empty;
}
