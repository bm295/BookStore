using System.Reflection;
using Xunit;

namespace BookStoreTests.Architecture;

public class CleanArchitectureTests
{
    [Fact]
    public void DomainLayer_DoesNotDependOnApplicationOrInfrastructure()
    {
        var forbiddenPrefixes = new[]
        {
            "BookStore.Application",
            "BookStore.Infrastructure"
        };

        var domainTypes = typeof(BookStore.Domain.Catalog.Book).Assembly.GetTypes()
            .Where(type => type.Namespace?.StartsWith("BookStore.Domain", StringComparison.Ordinal) == true);

        var violations = domainTypes
            .SelectMany(type => GetReferencedTypes(type)
                .Where(reference => reference.Namespace is not null)
                .Where(reference => forbiddenPrefixes.Any(prefix => reference.Namespace!.StartsWith(prefix, StringComparison.Ordinal)))
                .Select(reference => $"{type.FullName} -> {reference.FullName}"))
            .ToList();

        Assert.Empty(violations);
    }

    [Fact]
    public void ApplicationLayer_DoesNotDependOnInfrastructure()
    {
        var applicationTypes = typeof(BookStore.Application.Pricing.CalculateCartPriceUseCase).Assembly.GetTypes()
            .Where(type => type.Namespace?.StartsWith("BookStore.Application", StringComparison.Ordinal) == true);

        var violations = applicationTypes
            .SelectMany(type => GetReferencedTypes(type)
                .Where(reference => reference.Namespace?.StartsWith("BookStore.Infrastructure", StringComparison.Ordinal) == true)
                .Select(reference => $"{type.FullName} -> {reference.FullName}"))
            .ToList();

        Assert.Empty(violations);
    }

    [Fact]
    public void SequencePersistence_DependsOnApplicationOwnedPort()
    {
        Assert.Equal("BookStore.Application.Sequences", typeof(BookStore.Application.Sequences.ISequenceRepository).Namespace);
        Assert.True(typeof(BookStore.Application.Sequences.ISequenceRepository)
            .IsAssignableFrom(typeof(BookStore.Infrastructure.Repositories.SequenceRepository)));
        Assert.Equal("BookStore.Application.Sequences", typeof(BookStore.Application.Sequences.SequenceService).Namespace);
    }

    [Fact]
    public void IdentifierAllocation_DoesNotOwnCollectionOrderingResponsibilities()
    {
        var methodNames = typeof(BookStore.Application.Sequences.SequenceService)
            .GetMethods(BindingFlags.Public | BindingFlags.Instance | BindingFlags.DeclaredOnly)
            .Select(method => method.Name);

        Assert.DoesNotContain("AssignLineSequence", methodNames);
        Assert.DoesNotContain("OrderByPriority", methodNames);
    }

    private static IEnumerable<Type> GetReferencedTypes(Type type)
    {
        return type.GetMembers(BindingFlags.Public | BindingFlags.NonPublic | BindingFlags.Instance | BindingFlags.Static)
            .SelectMany(member => member switch
            {
                FieldInfo field => new[] { field.FieldType },
                PropertyInfo property => new[] { property.PropertyType },
                MethodInfo method => method.GetParameters().Select(parameter => parameter.ParameterType).Append(method.ReturnType),
                ConstructorInfo constructor => constructor.GetParameters().Select(parameter => parameter.ParameterType),
                EventInfo eventInfo => new[] { eventInfo.EventHandlerType }.Where(eventType => eventType is not null)!,
                _ => Array.Empty<Type>()
            })
            .Select(UnwrapType)
            .Where(referencedType => referencedType is not null)!;
    }

    private static Type? UnwrapType(Type type)
    {
        if (type.IsArray)
        {
            return type.GetElementType();
        }

        if (type.IsGenericType)
        {
            return type.GetGenericArguments().Select(UnwrapType).FirstOrDefault(argument => argument?.Namespace?.StartsWith("BookStore", StringComparison.Ordinal) == true) ?? type.GetGenericTypeDefinition();
        }

        return type;
    }
}
