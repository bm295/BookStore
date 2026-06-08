namespace BookStore.Domain.Errors;

/// <summary>
/// Base exception type for bookstore-specific errors.
/// </summary>
public abstract class BookStoreException : Exception
{
    protected BookStoreException(string message) : base(message)
    {
    }

    protected BookStoreException(string message, Exception innerException) : base(message, innerException)
    {
    }
}
