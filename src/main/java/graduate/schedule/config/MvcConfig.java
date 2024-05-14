package graduate.schedule.config;

import graduate.schedule.utils.auth.BearerAuthInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class MvcConfig implements WebMvcConfigurer {
    private final ClientIpResolver clientIpResolver;
    private final BearerAuthInterceptor bearerAuthInterceptor;

    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(clientIpResolver);
    }

    public void addInterceptors(InterceptorRegistry registry) {
        log.info("Interceptor 등록");
    }
}