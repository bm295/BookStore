using System.Globalization;
using BookStore.Infrastructure.Repositories;

namespace BookStore.Infrastructure.Services;

/// <summary>
/// Infrastructure service for sequence-oriented bookstore concerns such as generated IDs,
/// business-visible line ordering, and deterministic priority ordering.
/// </summary>
public sealed class SequenceService
{
    public const string BookIdSequenceKey = "book-id";
    public const string OrderIdSequenceKey = "order-id";

    private readonly ISequenceRepository _sequenceRepository;

    public SequenceService(ISequenceRepository sequenceRepository)
    {
        _sequenceRepository = sequenceRepository ?? throw new ArgumentNullException(nameof(sequenceRepository));
    }

    public long AllocateNextValue(string key)
    {
        ValidateKey(key);

        var sequence = _sequenceRepository.GetByKey(key);
        if (!string.Equals(sequence.Key, key, StringComparison.Ordinal))
        {
            throw new InvalidOperationException($"Sequence repository returned key '{sequence.Key}' for requested key '{key}'.");
        }

        return _sequenceRepository.AllocateSequence(sequence);
    }

    public int AllocateNextBookId()
    {
        var nextValue = AllocateNextValue(BookIdSequenceKey);
        if (nextValue > int.MaxValue)
        {
            throw new InvalidOperationException("No more book IDs can be allocated because the sequence is exhausted.");
        }

        return (int)nextValue;
    }

    public string AllocateNextOrderId()
    {
        return AllocateNextValue(OrderIdSequenceKey).ToString(CultureInfo.InvariantCulture);
    }

    public IReadOnlyList<SequencedItem<T>> AssignLineSequence<T>(IEnumerable<T> lines)
    {
        ArgumentNullException.ThrowIfNull(lines);

        return lines
            .Select((line, index) => new SequencedItem<T>(index + 1, line))
            .ToArray();
    }

    public IReadOnlyList<T> OrderByPriority<T>(IEnumerable<T> items, Func<T, int> prioritySelector)
    {
        ArgumentNullException.ThrowIfNull(items);
        ArgumentNullException.ThrowIfNull(prioritySelector);

        var prioritizedItems = items
            .Select(item => new PrioritizedItem<T>(prioritySelector(item), item))
            .ToArray();

        var duplicatePriority = prioritizedItems
            .GroupBy(item => item.Priority)
            .FirstOrDefault(group => group.Count() > 1);

        if (duplicatePriority is not null)
        {
            throw new InvalidOperationException($"Priority '{duplicatePriority.Key}' is duplicated; sequence priority ties must be explicit before ordering.");
        }

        return prioritizedItems
            .OrderBy(item => item.Priority)
            .Select(item => item.Value)
            .ToArray();
    }

    private static void ValidateKey(string key)
    {
        if (string.IsNullOrWhiteSpace(key))
        {
            throw new ArgumentException("Sequence key is required.", nameof(key));
        }
    }
}

public sealed record SequencedItem<T>(int Sequence, T Value);

file sealed record PrioritizedItem<T>(int Priority, T Value);
