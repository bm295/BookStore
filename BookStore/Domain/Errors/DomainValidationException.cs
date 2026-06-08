namespace BookStore.Domain.Errors;

/// <summary>
/// Raised when a domain value violates an invariant.
/// </summary>
public sealed class DomainValidationException : BookStoreException
{
    public DomainValidationException(string message) : base(message)
    {
    }
}
