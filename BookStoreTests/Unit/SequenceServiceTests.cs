using BookStore.Infrastructure.Database;
using BookStore.Infrastructure.Repositories;
using BookStore.Infrastructure.Services;
using Microsoft.EntityFrameworkCore;
using Xunit;

namespace BookStoreTests.Unit;

public class SequenceServiceTests
{
    [Fact]
    public void AllocateNextValue_GetsSequenceByKeyThenAllocatesSequenceFromRepository()
    {
        var repository = new RecordingSequenceRepository(new SequenceRecord("catalog-book", 41), allocatedValue: 42);
        var service = new SequenceService(repository);

        var nextValue = service.AllocateNextValue("catalog-book");

        Assert.Equal(42, nextValue);
        Assert.Equal(new[] { "GetByKey:catalog-book", "AllocateSequence:catalog-book:41" }, repository.Calls);
    }

    [Fact]
    public void AllocateNextBookId_UsesBookIdSequenceKey()
    {
        var repository = new RecordingSequenceRepository(new SequenceRecord(SequenceService.BookIdSequenceKey, 7), allocatedValue: 8);
        var service = new SequenceService(repository);

        var nextBookId = service.AllocateNextBookId();

        Assert.Equal(8, nextBookId);
        Assert.Equal(SequenceService.BookIdSequenceKey, repository.RequestedKey);
    }

    [Fact]
    public void AllocateNextOrderId_UsesOrderIdSequenceKeyAndFormatsAllocatedValue()
    {
        var repository = new RecordingSequenceRepository(new SequenceRecord(SequenceService.OrderIdSequenceKey, 99), allocatedValue: 100);
        var service = new SequenceService(repository);

        var nextOrderId = service.AllocateNextOrderId();

        Assert.Equal("100", nextOrderId);
        Assert.Equal(SequenceService.OrderIdSequenceKey, repository.RequestedKey);
    }

    [Fact]
    public void AllocateNextValue_WhenKeyIsBlank_ThrowsArgumentException()
    {
        var repository = new RecordingSequenceRepository(new SequenceRecord("unused", 0), allocatedValue: 1);
        var service = new SequenceService(repository);

        Assert.Throws<ArgumentException>(() => service.AllocateNextValue(" "));
        Assert.Empty(repository.Calls);
    }

    [Fact]
    public void SequenceRepository_AllocatesIndependentSequencesByKeyInDatabaseTable()
    {
        using var context = CreateContext();
        var repository = new SequenceRepository(context);
        var service = new SequenceService(repository);

        var firstBookId = service.AllocateNextValue("book");
        var firstOrderId = service.AllocateNextValue("order");
        var secondBookId = service.AllocateNextValue("book");

        Assert.Equal(1, firstBookId);
        Assert.Equal(1, firstOrderId);
        Assert.Equal(2, secondBookId);
        Assert.Equal(2, context.Sequences.Single(sequence => sequence.SequenceKey == "book").CurrentValue);
        Assert.Equal(1, context.Sequences.Single(sequence => sequence.SequenceKey == "order").CurrentValue);
    }

    [Fact]
    public void SequenceRepository_WhenSequenceChangesAfterRead_ThrowsInvalidOperationException()
    {
        using var context = CreateContext();
        var repository = new SequenceRepository(context);
        context.Sequences.Add(new SequenceEntity { SequenceKey = "book", CurrentValue = 5 });
        context.SaveChanges();

        var staleSequence = repository.GetByKey("book");
        context.Sequences.Single(sequence => sequence.SequenceKey == "book").CurrentValue = 6;
        context.SaveChanges();

        var exception = Assert.Throws<InvalidOperationException>(() => repository.AllocateSequence(staleSequence));
        Assert.Contains("changed", exception.Message, StringComparison.OrdinalIgnoreCase);
    }

    [Fact]
    public void SequenceEntity_RequiresUniqueSequenceKeyAndCurrentValue()
    {
        using var context = CreateContext();
        var entityType = context.Model.FindEntityType(typeof(SequenceEntity));

        Assert.NotNull(entityType);
        Assert.False(entityType.FindProperty(nameof(SequenceEntity.SequenceKey))!.IsNullable);
        Assert.False(entityType.FindProperty(nameof(SequenceEntity.CurrentValue))!.IsNullable);
        Assert.Contains(
            entityType.GetIndexes(),
            index => index.IsUnique && index.Properties.Any(property => property.Name == nameof(SequenceEntity.SequenceKey)));
    }

    [Fact]
    public void AssignLineSequence_PreservesInputOrderWithOneBasedPositions()
    {
        var service = new SequenceService(new RecordingSequenceRepository(new SequenceRecord("unused", 0), allocatedValue: 1));

        var sequencedLines = service.AssignLineSequence(new[] { "first", "second", "third" });

        Assert.Collection(
            sequencedLines,
            line =>
            {
                Assert.Equal(1, line.Sequence);
                Assert.Equal("first", line.Value);
            },
            line =>
            {
                Assert.Equal(2, line.Sequence);
                Assert.Equal("second", line.Value);
            },
            line =>
            {
                Assert.Equal(3, line.Sequence);
                Assert.Equal("third", line.Value);
            });
    }

    [Fact]
    public void OrderByPriority_WhenPrioritiesAreUnique_ReturnsAscendingPriorityOrder()
    {
        var service = new SequenceService(new RecordingSequenceRepository(new SequenceRecord("unused", 0), allocatedValue: 1));
        var rules = new[]
        {
            new PriorityRule("third", 30),
            new PriorityRule("first", 10),
            new PriorityRule("second", 20)
        };

        var orderedRules = service.OrderByPriority(rules, rule => rule.Priority);

        Assert.Equal(new[] { "first", "second", "third" }, orderedRules.Select(rule => rule.Name));
    }

    [Fact]
    public void OrderByPriority_WhenPriorityIsDuplicated_ThrowsInvalidOperationException()
    {
        var service = new SequenceService(new RecordingSequenceRepository(new SequenceRecord("unused", 0), allocatedValue: 1));
        var rules = new[]
        {
            new PriorityRule("first", 10),
            new PriorityRule("duplicate", 10)
        };

        var exception = Assert.Throws<InvalidOperationException>(() => service.OrderByPriority(rules, rule => rule.Priority));
        Assert.Contains("duplicated", exception.Message, StringComparison.OrdinalIgnoreCase);
    }

    private static BookStoreDbContext CreateContext()
    {
        var options = new DbContextOptionsBuilder<BookStoreDbContext>()
            .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString("N"))
            .Options;

        return new BookStoreDbContext(options);
    }

    private sealed class RecordingSequenceRepository : ISequenceRepository
    {
        private readonly SequenceRecord _sequence;
        private readonly long _allocatedValue;
        private readonly List<string> _calls = new();

        public RecordingSequenceRepository(SequenceRecord sequence, long allocatedValue)
        {
            _sequence = sequence;
            _allocatedValue = allocatedValue;
        }

        public IReadOnlyList<string> Calls => _calls;

        public string? RequestedKey { get; private set; }

        public SequenceRecord GetByKey(string key)
        {
            RequestedKey = key;
            _calls.Add($"GetByKey:{key}");
            return _sequence;
        }

        public long AllocateSequence(SequenceRecord sequence)
        {
            _calls.Add($"AllocateSequence:{sequence.Key}:{sequence.CurrentValue}");
            return _allocatedValue;
        }
    }

    private sealed record PriorityRule(string Name, int Priority);
}
