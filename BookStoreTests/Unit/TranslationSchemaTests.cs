using BookStore.Infrastructure.Database;
using Microsoft.EntityFrameworkCore;
using Xunit;

namespace BookStoreTests.Unit;

public class TranslationSchemaTests
{
    [Fact]
    public async Task LanguageMasterAndLanguageResource_CanPersistBookTitleTranslation()
    {
        var options = new DbContextOptionsBuilder<BookStoreDbContext>()
            .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString("N"))
            .Options;

        await using var context = new BookStoreDbContext(options);
        context.LanguageMaster.Add(new LanguageMasterEntity
        {
            LanguageCode = "vn",
            LanguageName = "Vietnamese"
        });
        context.LanguageResource.Add(new LanguageResourceEntity
        {
            LanguageCode = "vn",
            ResourceKey = "book:1:title",
            ResourceValue = "Nhà Giả Kim"
        });

        await context.SaveChangesAsync();

        var resource = await context.LanguageResource.SingleAsync(
            value => value.LanguageCode == "vn" && value.ResourceKey == "book:1:title");

        Assert.Equal("Nhà Giả Kim", resource.ResourceValue);
    }

    [Fact]
    public void LanguageMaster_RequiresLanguageCodeAndName()
    {
        using var context = CreateContext();
        var entityType = context.Model.FindEntityType(typeof(LanguageMasterEntity));

        Assert.NotNull(entityType);
        Assert.False(entityType.FindProperty(nameof(LanguageMasterEntity.LanguageCode))!.IsNullable);
        Assert.False(entityType.FindProperty(nameof(LanguageMasterEntity.LanguageName))!.IsNullable);
    }

    [Fact]
    public void LanguageResource_RequiresResourceKeyAndValue()
    {
        using var context = CreateContext();
        var entityType = context.Model.FindEntityType(typeof(LanguageResourceEntity));

        Assert.NotNull(entityType);
        Assert.False(entityType.FindProperty(nameof(LanguageResourceEntity.ResourceKey))!.IsNullable);
        Assert.False(entityType.FindProperty(nameof(LanguageResourceEntity.ResourceValue))!.IsNullable);
    }

    private static BookStoreDbContext CreateContext()
    {
        var options = new DbContextOptionsBuilder<BookStoreDbContext>()
            .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString("N"))
            .Options;

        return new BookStoreDbContext(options);
    }
}
