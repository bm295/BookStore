using BookStore.Application.Files;
using BookStore.Infrastructure.Files;

namespace BookStore;

/// <summary>
/// Backward-compatible facade over file infrastructure.
/// </summary>
public class FileIO
{
    private readonly IReadSpecificLine _lineReader;

    public FileIO() : this(new LineFileReader())
    {
    }

    public FileIO(IReadSpecificLine lineReader)
    {
        _lineReader = lineReader;
    }

    public string? ReadSpecificLine(string filePath, int lineNumber)
    {
        return _lineReader.ReadSpecificLine(filePath, lineNumber);
    }
}
