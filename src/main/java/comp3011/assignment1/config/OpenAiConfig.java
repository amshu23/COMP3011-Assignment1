package comp3011.assignment1.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAiConfig {

	// Gets the API key from the environment/configuration.
    @Value("${openai.api-key}")
    private String apiKey;
    
    public String getApiKey() 
    {
        return apiKey;
    }
}