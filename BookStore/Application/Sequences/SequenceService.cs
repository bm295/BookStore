using System.Globalization;

namespace BookStore.Application.Sequences;

/// <summary>
/// Application service for allocating generated bookstore identifiers through a persistence port.
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

    private static void ValidateKey(string key)
    {
        if (string.IsNullOrWhiteSpace(key))
        {
            throw new ArgumentException("Sequence key is required.", nameof(key));
        }
    }
}
