using System.Text.Json.Serialization;

namespace MessageConsumer.Events;

public record WelcomeEmailEvent(
    // the property name in C# can be different from the JSON property name
    // we can use JsonPropertyName to specify the mapping.
    [property: JsonPropertyName("email")] string Email, 
    [property: JsonPropertyName("firstname")] string FirstName,
    [property: JsonPropertyName("lastname")] string LastName
    );