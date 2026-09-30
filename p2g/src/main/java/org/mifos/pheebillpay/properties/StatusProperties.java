package org.mifos.pheebillpay.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * The bill id that makes the payment response pause, and how long the pause is, in seconds. Every value is required, as
 * it was when it was a bare {@code @Value} field.
 */
@Validated
@ConfigurationProperties(prefix = "status")
public record StatusProperties(@NotNull String billAcceptedId, @NotNull Integer billTimeout) {
}
