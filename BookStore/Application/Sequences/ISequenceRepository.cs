namespace BookStore.Application.Sequences;

public interface ISequenceRepository
{
    SequenceRecord GetByKey(string key);

    long AllocateSequence(SequenceRecord sequence);
}

public sealed record SequenceRecord(string Key, long CurrentValue);
