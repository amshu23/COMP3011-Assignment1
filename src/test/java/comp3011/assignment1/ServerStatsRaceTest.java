package comp3011.assignment1;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;

import comp3011.assignment1.service.ServerStatsService;

class ServerStatsRaceTest {

    @Test
    void handlesConcurrentTokenUpdates() throws Exception {

        int numberOfThreads = 201;

        ServerStatsService statsService =
                new ServerStatsService();

        var executor =
                Executors.newFixedThreadPool(
                        numberOfThreads
                );

        List<Future<?>> results =
                new ArrayList<>();

        for (int i = 0;
                i < numberOfThreads;
                i++) {

            results.add(
                    executor.submit(() -> {

                        statsService.addTokenUsage(
                                10,
                                5
                        );
                    })
            );
        }

        for (Future<?> result : results) {
            result.get();
        }

        executor.shutdown();

        assertEquals(
                2010,
                statsService.getInputTokens()
        );

        assertEquals(
                1005,
                statsService.getOutputTokens()
        );
    }
}