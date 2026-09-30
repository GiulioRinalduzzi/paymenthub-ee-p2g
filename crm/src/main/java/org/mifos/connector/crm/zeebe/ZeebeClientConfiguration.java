package org.mifos.connector.crm.zeebe;

import io.camunda.zeebe.client.ZeebeClient;
import java.time.Duration;
import org.mifos.connector.crm.properties.ZeebeProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZeebeClientConfiguration {

    @Bean
    public ZeebeClient setup(ZeebeProperties zeebeProperties) {
        return ZeebeClient.newClientBuilder().gatewayAddress(zeebeProperties.broker().contactpoint()).usePlaintext()
                .defaultJobPollInterval(Duration.ofMillis(1)).defaultJobWorkerMaxJobsActive(2000)
                .numJobWorkerExecutionThreads(zeebeProperties.client().maxExecutionThreads()).build();
    }
}
