using System.Text.Json;
using DomainBook = BookStore.Domain.Catalog.Book;

namespace BookStore.Infrastructure.Repositories;

/// <summary>
/// File-based repository for books.
/// </summary>
public sealed class BookRepository
{
    private const string SchemaVersionValue = "1";
    private readonly string _dataDirectory;
    private readonly string _filePath;
    private readonly JsonSerializerOptions _jsonOptions;

    public BookRepository(string? dataDirectory = null)
    {
        _dataDirectory = string.IsNullOrWhiteSpace(dataDirectory)
            ? Path.Combine(Environment.CurrentDirectory, "data")
            : dataDirectory;
        _filePath = Path.Combine(_dataDirectory, "books.json");
        _jsonOptions = new JsonSerializerOptions
        {
            PropertyNamingPolicy = JsonNamingPolicy.CamelCase,
            WriteIndented = true
        };
    }

    public IReadOnlyCollection<DomainBook> GetAll()
    {
        if (!File.Exists(_filePath))
        {
            return Array.Empty<DomainBook>();
        }

        using var stream = File.OpenRead(_filePath);
        var envelope = JsonSerializer.Deserialize<RepositoryEnvelope>(stream, _jsonOptions);
        return envelope?.Items ?? Array.Empty<DomainBook>();
    }

    public void SaveAll(IEnumerable<DomainBook> books)
    {
        Directory.CreateDirectory(_dataDirectory);

        var envelope = new RepositoryEnvelope
        {
            SchemaVersion = 1,
            GeneratedAtUtc = DateTime.UtcNow,
            Items = books.ToArray()
        };

        var temporaryFilePath = Path.GetTempFileName();
        try
        {
            using (var fileStream = File.Create(temporaryFilePath))
            {
                JsonSerializer.Serialize(fileStream, envelope, _jsonOptions);
            }

            File.Copy(temporaryFilePath, _filePath, overwrite: true);
        }
        finally
        {
            File.Delete(temporaryFilePath);
        }
    }

    private sealed class RepositoryEnvelope
    {
        public int SchemaVersion { get; set; }
        public DateTime GeneratedAtUtc { get; set; }
        public DomainBook[] Items { get; set; } = Array.Empty<DomainBook>();
    }
}
