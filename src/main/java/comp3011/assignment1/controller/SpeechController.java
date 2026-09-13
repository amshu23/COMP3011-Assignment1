package comp3011.assignment1.controller;

import comp3011.assignment1.service.TranscriptionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class SpeechController {

    private final TranscriptionService transcriptionService;
    public SpeechController(TranscriptionService transcriptionService) {
        this.transcriptionService = transcriptionService;
    }

    
    @GetMapping("/api/test")
    public String test() {
        	return transcriptionService.checkApiKey();
    }
}