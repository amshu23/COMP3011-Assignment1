package comp3011.assignment1.service;
import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicLong;



@Service
public class ServerStatsService {

    private final AtomicLong inputTokens = new AtomicLong(0);
    private final AtomicLong outputTokens = new AtomicLong(0);

    public long getInputTokens() {
        return inputTokens.get();
    }

    public long getOutputTokens() {
        return outputTokens.get();
    }

    public void addTokenUsage(long input, long output) {
        inputTokens.addAndGet(input);
        outputTokens.addAndGet(output);
    }
}