package comp3011.assignment1.service;
import comp3011.assignment1.config.OpenAiConfig;
import org.springframework.stereotype.Service;

@Service
public class TranscriptionService {
	private final OpenAiConfig openAiConfig;
	public TranscriptionService(OpenAiConfig openAiConfig)
	{
		this.openAiConfig = openAiConfig;
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