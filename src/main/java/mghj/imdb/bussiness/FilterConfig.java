package mghj.imdb.bussiness;


import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<RequestCountingFilter> loggingFilter(RequestCounterService requestCounterService) {
        FilterRegistrationBean<RequestCountingFilter> registrationBean = new FilterRegistrationBean<>();
        // Make sure the filter is registered properly with the requestCounterService injected
        registrationBean.setFilter(new RequestCountingFilter(requestCounterService));
        registrationBean.addUrlPatterns("/api/*", "/imdb/*", "/*");  // Optionally limit to specific URL patterns
        return registrationBean;
    }
}
