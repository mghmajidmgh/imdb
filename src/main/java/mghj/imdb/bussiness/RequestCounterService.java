package mghj.imdb.bussiness;

import org.springframework.stereotype.Service;

@Service
public class RequestCounterService {

    private int requestCount = 0;


    public void increment() {
        requestCount++;
    }

    public int getRequestCount() {
        return requestCount;
    }
}
