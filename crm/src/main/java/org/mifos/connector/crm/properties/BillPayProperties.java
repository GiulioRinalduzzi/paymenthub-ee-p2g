package org.mifos.connector.crm.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * The mock bill ids the biller side reacts to. Every value is required, as it was when it was a bare {@code @Value}
 * field.
 */
@Validated
@ConfigurationProperties(prefix = "billpay")
public record BillPayProperties(@NotNull String billAlreadyPaidId, @NotNull String billIdInvalidId, @NotNull String billIdEmptyId,
        @NotNull String billPayTimeoutId) {
}
