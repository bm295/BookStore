using BookStore.Application.Files;

namespace BookStore.Infrastructure.Files;

/// <summary>
/// Infrastructure adapter for line-oriented file access.
/// </summary>
public sealed class LineFileReader : IReadSpecificLine
{
    public string? ReadSpecificLine(string filePath, int lineNumber)
    {
        if (string.IsNullOrWhiteSpace(filePath))
        {
            throw new Exception("Invalid File Path");
        }

        using var file = new StreamReader(filePath);

        for (var i = 1; i <= lineNumber; i++)
        {
            file.ReadLine();
            if (file.EndOfStream)
            {
                break;
            }
        }

        return file.ReadLine();
    }
}
