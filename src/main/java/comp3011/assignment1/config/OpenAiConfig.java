package comp3011.assignment1.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class OpenAiConfig {

    @Value("${openai.api-key}")
    private String apiKey;

    public String getApiKey() {
        return apiKey;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}