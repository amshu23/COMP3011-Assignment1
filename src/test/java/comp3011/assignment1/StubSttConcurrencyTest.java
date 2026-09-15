package comp3011.assignment1;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.sun.net.httpserver.HttpServer;

@SpringBootTest(
        webEnvironment =
                SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "openai.api-key=test-key"
        }
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StubSttConcurrencyTest {

    @LocalServerPort
    private int port;

    private static HttpServer stubServer;

    static {
        try {
            startStubServer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void startStubServer()
            throws IOException {

        stubServer = HttpServer.create(
                new InetSocketAddress(0),
                0
        );

        stubServer.createContext(
                "/v1/audio/transcriptions",
                exchange -> {

                    try {

                        // Simulate a slow external STT service.
                        Thread.sleep(1000);

                        String response =
                                "{\"text\":\"stub transcription\","
                                + "\"usage\":"
                                + "{\"input_tokens\":10,"
                                + "\"output_tokens\":5}}";

                        byte[] responseBytes =
                                response.getBytes();

                        exchange.getResponseHeaders().set(
                                "Content-Type",
                                "application/json"
                        );

                        exchange.sendResponseHeaders(
                                200,
                                responseBytes.length
                        );

                        try (OutputStream output =
                                     exchange.getResponseBody()) {

                            output.write(responseBytes);
                        }

                    } catch (InterruptedException e) {

                        Thread.currentThread().interrupt();
                    }
                }
        );

        stubServer.setExecutor(
                Executors.newFixedThreadPool(250)
        );

        stubServer.start();

        System.out.println(
                "Stub STT server started on port "
                + stubServer.getAddress().getPort()
        );
    }

    @DynamicPropertySource
    static void configureStubUrl(
            DynamicPropertyRegistry registry) {

        registry.add(
                "openai.transcription-url",
                () -> "http://localhost:"
                        + stubServer.getAddress().getPort()
                        + "/v1/audio/transcriptions"
        );
    }

    @AfterAll
    static void stopStubServer() {

        stubServer.stop(0);
    }

    @Test
    void handles201ConcurrentTranscriptionRequests()
            throws Exception {

        int numberOfRequests = 201;

        var executor =
                Executors.newFixedThreadPool(
                        numberOfRequests
                );

        var startSignal =
                new CountDownLatch(1);

        HttpClient client =
                HttpClient.newHttpClient();

        List<Future<String>> results =
                new ArrayList<>();

        for (int i = 0;
                i < numberOfRequests;
                i++) {

            results.add(
                    executor.submit(() -> {

                        startSignal.await();

                        String boundary =
                                "TestBoundary"
                                + System.nanoTime();

                        String body =
                                "--" + boundary + "\r\n"
                                + "Content-Disposition: "
                                + "form-data; "
                                + "name=\"file\"; "
                                + "filename=\"test.webm\"\r\n"
                                + "Content-Type: audio/webm\r\n"
                                + "\r\n"
                                + "test audio data\r\n"
                                + "--" + boundary
                                + "--\r\n";

                        HttpRequest request =
                                HttpRequest.newBuilder()
                                        .uri(
                                                URI.create(
                                                        "http://localhost:"
                                                        + port
                                                        + "/api/transcribe"
                                                )
                                        )
                                        .header(
                                                "Content-Type",
                                                "multipart/form-data; "
                                                + "boundary="
                                                + boundary
                                        )
                                        .POST(
                                                HttpRequest
                                                        .BodyPublishers
                                                        .ofString(body)
                                        )
                                        .build();

                        HttpResponse<String> response =
                                client.send(
                                        request,
                                        HttpResponse
                                                .BodyHandlers
                                                .ofString()
                                );

                        return response.body();
                    })
            );
        }

        long startTime =
                System.currentTimeMillis();

        startSignal.countDown();

        for (Future<String> result : results) {

            assertEquals(
                    "stub transcription",
                    result.get()
            );
        }

        long elapsed =
                System.currentTimeMillis()
                - startTime;

        System.out.println(
                "201 transcription requests completed in "
                + elapsed
                + " ms"
        );

        executor.shutdown();
    }
}