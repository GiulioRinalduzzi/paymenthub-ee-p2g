package org.mifos.pheebillpay.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * This service's own callback address, and the mock bill ids it reacts to. Every value is required, as it was when it
 * was a bare {@code @Value} field.
 *
 * <p>
 * application.yml has two blocks, {@code billpay} and {@code billPay}. Property names are case-insensitive once bound,
 * so both are the same prefix, and this one record binds them together.
 * </p>
 */
@Validated
@ConfigurationProperties(prefix = "billpay")
public record BillPayProperties(@NotNull String contactpoint, @NotNull @Valid Endpoint endpoint, @NotNull String fspNotOnboarded,
        @NotNull String billIdEmptyOriginal, @NotNull String billIdEmpty) {

    public record Endpoint(@NotNull String payerRtpResponse) {
    }
}
