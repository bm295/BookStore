using BookStore.Application.Sequences;
using Xunit;

namespace BookStoreTests.Unit;

public class SequenceOrderingTests
{
    [Fact]
    public void AssignLineSequence_PreservesInputOrderWithOneBasedPositions()
    {
        var sequencedLines = SequenceOrdering.AssignLineSequence(new[] { "first", "second", "third" });

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
        var rules = new[]
        {
            new PriorityRule("third", 30),
            new PriorityRule("first", 10),
            new PriorityRule("second", 20)
        };

        var orderedRules = SequenceOrdering.OrderByPriority(rules, rule => rule.Priority);

        Assert.Equal(new[] { "first", "second", "third" }, orderedRules.Select(rule => rule.Name));
    }

    [Fact]
    public void OrderByPriority_WhenPriorityIsDuplicated_ThrowsInvalidOperationException()
    {
        var rules = new[]
        {
            new PriorityRule("first", 10),
            new PriorityRule("duplicate", 10)
        };

        var exception = Assert.Throws<InvalidOperationException>(() => SequenceOrdering.OrderByPriority(rules, rule => rule.Priority));
        Assert.Contains("duplicated", exception.Message, StringComparison.OrdinalIgnoreCase);
    }

    [Fact]
    public void AssignLineSequence_WhenLinesAreNull_ThrowsArgumentNullException()
    {
        Assert.Throws<ArgumentNullException>(() => SequenceOrdering.AssignLineSequence<string>(null!));
    }

    [Fact]
    public void OrderByPriority_WhenSelectorIsNull_ThrowsArgumentNullException()
    {
        Assert.Throws<ArgumentNullException>(() => SequenceOrdering.OrderByPriority(Array.Empty<string>(), null!));
    }

    private sealed record PriorityRule(string Name, int Priority);
}
