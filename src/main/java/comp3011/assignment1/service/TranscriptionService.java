package comp3011.assignment1.service;
import comp3011.assignment1.config.OpenAiConfig;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import org.springframework.web.multipart.MultipartFile;

@Service
public class TranscriptionService {
	private final OpenAiConfig openAiConfig;
	private final HttpClient httpClient;
	
	public TranscriptionService(OpenAiConfig openAiConfig)
	{
		this.openAiConfig = openAiConfig;
		this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
	}
	
	public String checkApiKey()
	{
		String apiKey = openAiConfig.getApiKey();
		
		if(apiKey == null || apiKey.isBlank())
		{
			return "OpenAI API key is not yet configured.";
	
		}
		
		return "OpenAI API key has been configured....";
	}
}