package graduate.schedule.config;

import graduate.schedule.ScheduleApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackageClasses = ScheduleApplication.class)
public class FeignClientConfig {
}