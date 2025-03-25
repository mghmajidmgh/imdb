package mghj.imdb.bussiness;


import jakarta.servlet.Filter;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

public class RequestCountingFilter implements  jakarta.servlet.Filter {

    private final RequestCounterService requestCounterService;

    // Constructor for dependency injection
    public RequestCountingFilter(RequestCounterService requestCounterService) {
        this.requestCounterService = requestCounterService;
    }





    @Override
    public void init(jakarta.servlet.FilterConfig filterConfig) throws jakarta.servlet.ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(jakarta.servlet.ServletRequest servletRequest, jakarta.servlet.ServletResponse servletResponse, jakarta.servlet.FilterChain filterChain) throws IOException, jakarta.servlet.ServletException {
        // Increment the request count for each incoming request
        requestCounterService.increment();

        // Continue the request-response chain
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        // Cleanup if needed
    }
}
