package org.mifos.connector.crm.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * The bill id that makes the payment worker pause, and how long the pause is, in seconds. Every value is required, as
 * it was when it was a bare {@code @Value} field.
 */
@Validated
@ConfigurationProperties(prefix = "status")
public record StatusProperties(@NotNull String billReqAcceptedId, @NotNull Integer billTimeout) {
}
