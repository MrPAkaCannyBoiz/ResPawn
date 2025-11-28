using System.Text.Json;

namespace BlazorApp.Services;

public static class JsonCaseInsensitiveExtension
{
    public static JsonSerializerOptions MakeJsonCaseInsensitive()
    {
        JsonSerializerOptions jsonSerializerOptions = new JsonSerializerOptions
        {
            PropertyNameCaseInsensitive = true
        };
        return jsonSerializerOptions;
    }
}
