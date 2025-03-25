package mghj.imdb.bussiness;

import org.springframework.stereotype.Service;

@Service
public class RequestCounterService {

    private int requestCount = 0;

    // Increment the count on every request
    public void increment() {
        requestCount++;
    }

    // Get the current request count
    public int getRequestCount() {
        return requestCount;
    }
}
