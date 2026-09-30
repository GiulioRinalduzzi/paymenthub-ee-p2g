package org.mifos.pheebillpay.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * The payer tenant, and the two mock payer accounts that make a request-to-pay fail on purpose. Every value is
 * required, as it was when it was a bare {@code @Value} field.
 *
 * <p>
 * application.yml spells the prefix {@code payer_fsp}; relaxed binding matches it to {@code payer-fsp}.
 * </p>
 */
@Validated
@ConfigurationProperties(prefix = "payer-fsp")
public record PayerFspProperties(@NotNull String tenant, @NotNull @Valid MockPayer mockPayerUnreachable,
        @NotNull @Valid MockPayer mockDebitFailed) {

    public record MockPayer(@NotNull String fspId, @NotNull String financialAddress) {
    }
}
