using Microsoft.EntityFrameworkCore;

namespace BookStore.Infrastructure.Database;

public sealed class BookStoreDbContext : DbContext
{
    public BookStoreDbContext(DbContextOptions<BookStoreDbContext> options)
        : base(options)
    {
    }

    public DbSet<RequestOrderDetailFormEntity> RequestOrderDetailForms => Set<RequestOrderDetailFormEntity>();
    public DbSet<RequestOrderDetailLineEntity> RequestOrderDetailLines => Set<RequestOrderDetailLineEntity>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<RequestOrderDetailFormEntity>(builder =>
        {
            builder.HasKey(e => e.Id);
            builder.HasIndex(e => e.OrderId).IsUnique();
            builder.Property(e => e.OrderId).IsRequired();
            builder.HasMany(e => e.Lines)
                .WithOne(e => e.Form)
                .HasForeignKey(e => e.RequestOrderDetailFormEntityId)
                .IsRequired();
        });

        modelBuilder.Entity<RequestOrderDetailLineEntity>(builder =>
        {
            builder.HasKey(e => e.Id);
            builder.Property(e => e.BookId).IsRequired();
            builder.Property(e => e.Quantity).IsRequired();
            builder.Property(e => e.UnitPrice).IsRequired();
        });
    }
}
