package org.mifos.pheebillpay.utils;

import java.util.concurrent.Executor;
import org.mifos.pheebillpay.properties.AsyncProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@ComponentScan(basePackages = "org.mifos.pheebillpay")
public abstract class AbstractApplicationConfiguration {

    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor(AsyncProperties asyncProperties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(asyncProperties.corePoolSize());
        executor.setMaxPoolSize(asyncProperties.maxPoolSize());
        executor.setQueueCapacity(asyncProperties.queueCapacity());
        executor.setThreadNamePrefix("AsyncThread-");
        executor.initialize();
        return executor;
    }
}
