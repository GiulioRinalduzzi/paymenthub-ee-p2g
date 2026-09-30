package org.mifos.pheebillpay.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * The operations API the bill status lookup calls. Every value is required, as it was when it was a bare {@code @Value}
 * field.
 */
@Validated
@ConfigurationProperties(prefix = "operations")
public record OperationsProperties(@NotNull String url, @NotNull @Valid Endpoint endpoint) {

    public record Endpoint(@NotNull String transactionReq) {
    }
}
