package mghj.imdb.bussiness;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<RequestCountingFilter> loggingFilter() {
        FilterRegistrationBean<RequestCountingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RequestCountingFilter());
        registrationBean.addUrlPatterns("/api/*", "/imdb/*", "/*");  // Optionally limit to specific URL patterns
        return registrationBean;
    }
}
