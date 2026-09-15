package comp3011.assignment1.controller;
import java.util.concurrent.CompletableFuture;
import comp3011.assignment1.service.TranscriptionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
@RestController
public class SpeechController {

    private final TranscriptionService tService;
    public SpeechController(TranscriptionService tService) {
        this.tService = tService;
    }

    
    @GetMapping("/api/test")
    public String testConnection() {
        	return tService.checkApiKey();
    }
    
    @PostMapping(value = "/api/transcribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CompletableFuture<String> transcribe(
            @RequestPart("file") MultipartFile audioFile) {

        return tService.transcribe(audioFile);
    }
}