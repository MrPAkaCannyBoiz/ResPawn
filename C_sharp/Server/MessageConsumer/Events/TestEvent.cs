using System.Text.Json.Serialization;

namespace KafkaConsumer.Events;

public record TestEvent(
    [property: JsonPropertyName("message")] string Message);
