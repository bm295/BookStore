namespace BookStore.Application.Sequences;

/// <summary>
/// Defines deterministic ordering rules independently of persisted identifier allocation.
/// </summary>
public static class SequenceOrdering
{
    public static IReadOnlyList<SequencedItem<T>> AssignLineSequence<T>(IEnumerable<T> lines)
    {
        ArgumentNullException.ThrowIfNull(lines);

        return lines
            .Select((line, index) => new SequencedItem<T>(index + 1, line))
            .ToArray();
    }

    public static IReadOnlyList<T> OrderByPriority<T>(IEnumerable<T> items, Func<T, int> prioritySelector)
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
}

public sealed record SequencedItem<T>(int Sequence, T Value);

file sealed record PrioritizedItem<T>(int Priority, T Value);
