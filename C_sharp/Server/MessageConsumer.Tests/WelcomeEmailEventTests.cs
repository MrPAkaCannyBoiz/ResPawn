using Xunit;
using System.Text.Json;
using MessageConsumer.Events;

namespace MessageConsumer.Tests;

public class WelcomeEmailEventTests
{
    [Fact]
    public void Deserialize_ValidJson_MapsAllProperties()
    {
        var json = """{"email":"john@example.com","firstname":"John","lastname":"Doe"}""";

        var result = JsonSerializer.Deserialize<WelcomeEmailEvent>(json);

        Assert.NotNull(result);
        Assert.Equal("john@example.com", result.Email);
        Assert.Equal("John", result.FirstName);
        Assert.Equal("Doe", result.LastName);
    }

    [Fact]
    public void Deserialize_CaseInsensitiveKeys_MapsProperties()
    {
        var json = """{"Email":"jane@example.com","FirstName":"Jane","LastName":"Smith"}""";

        var options = new JsonSerializerOptions { PropertyNameCaseInsensitive = true };
        var result = JsonSerializer.Deserialize<WelcomeEmailEvent>(json, options);

        Assert.NotNull(result);
        Assert.Equal("jane@example.com", result.Email);
    }

    [Fact]
    public void Deserialize_MissingFields_ReturnsNullStrings()
    {
        var json = """{"email":"only@email.com"}""";

        var result = JsonSerializer.Deserialize<WelcomeEmailEvent>(json);

        Assert.NotNull(result);
        Assert.Equal("only@email.com", result.Email);
        Assert.Null(result.FirstName);
        Assert.Null(result.LastName);
    }

    [Fact]
    public void Deserialize_InvalidJson_ThrowsJsonException()
    {
        var json = "not-valid-json";

        Assert.Throws<JsonException>(() => JsonSerializer.Deserialize<WelcomeEmailEvent>(json));
    }

    [Fact]
    public void Record_Equality_WorksCorrectly()
    {
        var a = new WelcomeEmailEvent("a@b.com", "A", "B");
        var b = new WelcomeEmailEvent("a@b.com", "A", "B");
        var c = new WelcomeEmailEvent("c@d.com", "C", "D");

        Assert.Equal(a, b);
        Assert.NotEqual(a, c);
    }
}
