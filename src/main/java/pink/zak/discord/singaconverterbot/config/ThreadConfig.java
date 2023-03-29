package pink.zak.discord.singaconverterbot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class ThreadConfig {

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        return new ThreadPoolTaskScheduler();
    }

    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        return this.threadPoolTaskScheduler().getScheduledExecutor();
    }
}
