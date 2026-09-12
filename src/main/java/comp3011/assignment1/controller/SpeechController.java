package comp3011.assignment1.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SpeechController {
	@GetMapping("/api/test")
    public String test() {
        return "API is working!";
}
}