namespace BookStore.Application.Files;

public interface IReadSpecificLine
{
    string? ReadSpecificLine(string filePath, int lineNumber);
}
