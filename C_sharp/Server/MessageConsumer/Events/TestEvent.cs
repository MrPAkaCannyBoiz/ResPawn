using System.Text.Json.Serialization;

namespace MessageConsumer.Events;

public record TestEvent(
    [property: JsonPropertyName("message")] string Message);
