package comp3011.assignment1.controller;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import comp3011.assignment1.service.ServerStatsService;

@RestController
public class AdminController {

    private final ServerStatsService statsService;
    private final ConfigurableApplicationContext applicationContext;

    private final Instant serverStartTime;

    private final AtomicBoolean shutdownStarted =
            new AtomicBoolean(false);

    public AdminController(
            ServerStatsService statsService,
            ConfigurableApplicationContext applicationContext) {

        this.statsService = statsService;
        this.applicationContext = applicationContext;
        this.serverStartTime = Instant.now();
    }

    @GetMapping("/api/v1/admin/uptime")
    public UptimeResponse getUptime() {

        Instant now = Instant.now();

        double uptimeSeconds =
                Duration.between(serverStartTime, now).toNanos()
                / 1_000_000_000.0;

        return new UptimeResponse(
                serverStartTime,
                now,
                uptimeSeconds
        );
    }

    @GetMapping("/api/v1/global/stats")
    public GlobalStatsResponse getGlobalStats() {

        return new GlobalStatsResponse(
                statsService.getInputTokens(),
                statsService.getOutputTokens()
        );
    }

    @PostMapping("/api/v1/admin/shutdown")
    public ResponseEntity<?> shutdown() {

        if (!shutdownStarted.compareAndSet(false, true)) {

            ErrorResponse error = new ErrorResponse(
                    Instant.now(),
                    409,
                    "Conflict",
                    "Shutdown has already been requested.",
                    "/api/v1/admin/shutdown"
            );

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(error);
        }

        CompletableFuture.runAsync(() -> {
            applicationContext.close();
        });

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(
                        new ShutdownResponse(
                                "Graceful shutdown requested."
                        )
                );
    }

    public record UptimeResponse(
            Instant utcServerStart,
            Instant utcNow,
            double serverUptimeSeconds
    ) {
    }

    public record ShutdownResponse(
            String message
    ) {
    }

    public record GlobalStatsResponse(
            long inputTokens,
            long outputTokens
    ) {
    }

    public record ErrorResponse(
            Instant timestamp,
            int status,
            String error,
            String message,
            String path
    ) {
    }
}