package org.mifos.connector.crm.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Where the Zeebe gateway is and how many threads the client uses. Every value is required, as it was when it was a
 * bare {@code @Value} field.
 *
 * <p>
 * {@code zeebe.client.evenly-allocated-max-jobs} is deliberately not here: in this module's application.yml it is a
 * SpEL expression, and only {@code @Value} evaluates SpEL.
 * </p>
 */
@Validated
@ConfigurationProperties(prefix = "zeebe")
public record ZeebeProperties(@NotNull @Valid Broker broker, @NotNull @Valid Client client) {

    public record Broker(@NotNull String contactpoint) {
    }

    public record Client(@NotNull Integer maxExecutionThreads) {
    }
}
