package comp3011.assignment1.service;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import comp3011.assignment1.config.OpenAiConfig;
import java.net.URI;
import java.net.http.HttpClient;
import org.springframework.stereotype.Service;


import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import org.springframework.web.multipart.MultipartFile;

@Service
public class TranscriptionService {
	private final OpenAiConfig openAiConfig;
	private static final String OPENAI_URL = "https://api.openai.com/v1/audio/transcriptions";

	private static final String MODEL_NAME = "gpt-4o-mini-transcribe";
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
	
	public CompletableFuture<String> transcribe(MultipartFile audioFile) {

	    String apiKey = openAiConfig.getApiKey();

	    if (apiKey == null || apiKey.isBlank()) {
	        return CompletableFuture.failedFuture(new IllegalStateException("OpenAI API key is not configured.")
	        );
	    }

	    try {
	    	byte[] audioData = audioFile.getBytes();

	    	String boundry = "Boundary...Java" + System.currentTimeMillis();

	    	String contentType = audioFile.getContentType();

	    	if (contentType == null || contentType.isBlank()) {
	    	    contentType = "application/octet-stream";
	    	}
	    	String fileName = audioFile.getOriginalFilename();

	    	if (fileName == null || fileName.isBlank()) {
	    	    fileName = "audio.webm";
	    	}

	    	StringBuilder body = new StringBuilder();

	    	body.append("--").append(boundry).append("\r\n");
	    	body.append("Content-Disposition: form-data; name=\"model\"\r\n\r\n");
	    	body.append(MODEL_NAME).append("\r\n");

	    	body.append("--").append(boundry).append("\r\n");
	    	body.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
	    	        .append(fileName)
	    	        .append("\"\r\n");
	    	body.append("Content-Type: ").append(contentType).append("\r\n\r\n");
	    	return CompletableFuture.completedFuture(
	    	        "Audio received: " + audioData.length + " bytes"
	    	);

	    } catch (Exception e) {
	        return CompletableFuture.failedFuture(e);
	    }
	}
}