using BookStore.Application.Sequences;
using BookStore.Infrastructure.Database;
using Microsoft.EntityFrameworkCore;

namespace BookStore.Infrastructure.Repositories;

public sealed class SequenceRepository : ISequenceRepository
{
    private readonly BookStoreDbContext _dbContext;

    public SequenceRepository(BookStoreDbContext dbContext)
    {
        _dbContext = dbContext ?? throw new ArgumentNullException(nameof(dbContext));
    }

    public SequenceRecord GetByKey(string key)
    {
        ValidateKey(key);

        var sequence = _dbContext.Sequences
            .AsNoTracking()
            .SingleOrDefault(entity => entity.SequenceKey == key);

        return sequence is null
            ? new SequenceRecord(key, 0)
            : new SequenceRecord(sequence.SequenceKey, sequence.CurrentValue);
    }

    public long AllocateSequence(SequenceRecord sequence)
    {
        ArgumentNullException.ThrowIfNull(sequence);
        ValidateKey(sequence.Key);

        var entity = _dbContext.Sequences.SingleOrDefault(value => value.SequenceKey == sequence.Key);
        if (entity is null)
        {
            if (sequence.CurrentValue != 0)
            {
                throw new InvalidOperationException($"Sequence '{sequence.Key}' does not exist and cannot be allocated from value '{sequence.CurrentValue}'.");
            }

            entity = new SequenceEntity
            {
                SequenceKey = sequence.Key,
                CurrentValue = 1
            };
            _dbContext.Sequences.Add(entity);
            _dbContext.SaveChanges();
            return entity.CurrentValue;
        }

        if (entity.CurrentValue != sequence.CurrentValue)
        {
            throw new InvalidOperationException($"Sequence '{sequence.Key}' changed after it was read.");
        }

        entity.CurrentValue = checked(entity.CurrentValue + 1);
        _dbContext.SaveChanges();
        return entity.CurrentValue;
    }

    private static void ValidateKey(string key)
    {
        if (string.IsNullOrWhiteSpace(key))
        {
            throw new ArgumentException("Sequence key is required.", nameof(key));
        }
    }
}
