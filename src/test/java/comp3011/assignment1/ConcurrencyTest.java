package comp3011.assignment1;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ConcurrencyTest {

    @LocalServerPort
    private int port;

    @Test
    void handlesMoreThan200ConcurrentRequests() throws Exception {

        int numberOfRequests = 201;

        ExecutorService executor =
                Executors.newFixedThreadPool(numberOfRequests);

        CountDownLatch startSignal =
                new CountDownLatch(1);

        HttpClient client =
                HttpClient.newHttpClient();

        List<Future<Integer>> results =
                new ArrayList<>();

        for (int i = 0; i < numberOfRequests; i++) {

            results.add(
                executor.submit(() -> {

                    startSignal.await();

                    HttpRequest request =
                            HttpRequest.newBuilder()
                                .uri(
                                    URI.create(
                                        "http://localhost:"
                                        + port
                                        + "/api/v1/admin/uptime"
                                    )
                                )
                                .GET()
                                .build();

                    HttpResponse<String> response =
                            client.send(
                                request,
                                HttpResponse.BodyHandlers.ofString()
                            );

                    return response.statusCode();
                })
            );
        }

        startSignal.countDown();

        for (Future<Integer> result : results) {
            assertEquals(200, result.get());
        }

        executor.shutdown();
    }
}