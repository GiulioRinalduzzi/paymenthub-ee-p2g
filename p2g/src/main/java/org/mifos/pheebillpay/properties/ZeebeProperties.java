package org.mifos.pheebillpay.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Where the Zeebe gateway is and how the client is sized. Every value is required, as it was when it was a bare
 * {@code @Value} field.
 *
 * <p>
 * Unlike the crm module, {@code zeebe.client.evenly-allocated-max-jobs} is a plain number in this module's
 * application.yml, so it can be bound here.
 * </p>
 */
@Validated
@ConfigurationProperties(prefix = "zeebe")
public record ZeebeProperties(@NotNull @Valid Broker broker, @NotNull @Valid Client client) {

    public record Broker(@NotNull String contactpoint) {
    }

    public record Client(@NotNull Integer maxExecutionThreads, @NotNull Integer evenlyAllocatedMaxJobs) {
    }
}
