package comp3011.assignment1.service;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import comp3011.assignment1.config.OpenAiConfig;

@Service
public class TranscriptionService {

    private final OpenAiConfig openAiConfig;
    private final ServerStatsService statsService;
    private final ObjectMapper objectMapper;

    @Value("${openai.transcription-url:https://api.openai.com/v1/audio/transcriptions}")
    private String transcriptionUrl;

    private static final String MODEL_NAME =
            "gpt-4o-mini-transcribe";

    private final HttpClient httpClient;

    public TranscriptionService(
            OpenAiConfig openAiConfig,
            ObjectMapper objectMapper,
            ServerStatsService statsService) {

        this.openAiConfig = openAiConfig;
        this.objectMapper = objectMapper;
        this.statsService = statsService;

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String checkApiKey() {

        String apiKey = openAiConfig.getApiKey();

        if (apiKey == null || apiKey.isBlank()) {
            return "OpenAI API key is not yet configured.";
        }

        return "OpenAI API key has been configured....";
    }

    public CompletableFuture<String> transcribe(
            MultipartFile audioFile) {

        String apiKey = openAiConfig.getApiKey();

        if (apiKey == null || apiKey.isBlank()) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException(
                            "OpenAI API key is not configured."
                    )
            );
        }

        try {

            byte[] audioData = audioFile.getBytes();

            String boundary =
                    "BoundaryJava" + System.nanoTime();

            String contentType =
                    audioFile.getContentType();

            if (contentType == null || contentType.isBlank()) {
                contentType = "application/octet-stream";
            }

            String fileName =
                    audioFile.getOriginalFilename();

            if (fileName == null || fileName.isBlank()) {
                fileName = "audio.webm";
            }

            ByteArrayOutputStream body =
                    new ByteArrayOutputStream();

            body.write(
                    ("--" + boundary + "\r\n")
                            .getBytes(StandardCharsets.UTF_8)
            );

            body.write(
                    "Content-Disposition: form-data; name=\"model\"\r\n\r\n"
                            .getBytes(StandardCharsets.UTF_8)
            );

            body.write(
                    (MODEL_NAME + "\r\n")
                            .getBytes(StandardCharsets.UTF_8)
            );

            body.write(
                    ("--" + boundary + "\r\n")
                            .getBytes(StandardCharsets.UTF_8)
            );

            body.write(
                    ("Content-Disposition: form-data; name=\"file\"; filename=\""
                            + fileName
                            + "\"\r\n")
                            .getBytes(StandardCharsets.UTF_8)
            );

            body.write(
                    ("Content-Type: " + contentType + "\r\n\r\n")
                            .getBytes(StandardCharsets.UTF_8)
            );

            body.write(audioData);

            body.write(
                    ("\r\n--" + boundary + "--\r\n")
                            .getBytes(StandardCharsets.UTF_8)
            );

           

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(transcriptionUrl))
                            .timeout(Duration.ofSeconds(30))
                            .header(
                                    "Authorization",
                                    "Bearer " + apiKey
                            )
                            .header(
                                    "Content-Type",
                                    "multipart/form-data; boundary="
                                            + boundary
                            )
                            .POST(
                                    HttpRequest.BodyPublishers
                                            .ofByteArray(
                                                    body.toByteArray()
                                            )
                            )
                            .build();

            return httpClient
                    .sendAsync(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    )
                    .thenApply(response -> {

                        if (response.statusCode() < 200
                                || response.statusCode() >= 300) {

                            throw new RuntimeException(
                                    "Transcription request failed with status "
                                            + response.statusCode()
                            );
                        }

                        try {

                            JsonNode json =
                                    objectMapper.readTree(
                                            response.body()
                                    );

                            JsonNode usage =
                                    json.path("usage");

                            long inputTokens =
                                    usage.path("input_tokens")
                                            .asLong(0);

                            long outputTokens =
                                    usage.path("output_tokens")
                                            .asLong(0);

                            statsService.addTokenUsage(
                                    inputTokens,
                                    outputTokens
                            );

                            return json.path("text").asText();

                        } catch (Exception e) {

                            throw new RuntimeException(
                                    "Could not read transcription response.",
                                    e
                            );
                        }
                    });

        } catch (Exception e) {

            return CompletableFuture.failedFuture(e);
        }
    }
}