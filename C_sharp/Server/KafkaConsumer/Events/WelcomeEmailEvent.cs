using System.Text.Json.Serialization;

namespace KafkaConsumer.Events;

public record WelcomeEmailEvent(
    // the property name in C# can be different from the JSON property name
    // we can use JsonPropertyName to specify the mapping.
    [property: JsonPropertyName("emailAddress")] string EmailAddress, 
    [property: JsonPropertyName("customerName")] string CustomerName
    );