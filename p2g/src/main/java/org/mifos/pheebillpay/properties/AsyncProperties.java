package org.mifos.pheebillpay.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Sizing of the {@code asyncExecutor} pool. Every value is required, as it was when it was a bare {@code @Value} field.
 *
 * <p>
 * application.yml spells the keys with underscores ({@code async.core_pool_size}); relaxed binding matches them to
 * these components, as it matched them to the dashed names the {@code @Value} fields used.
 * </p>
 */
@Validated
@ConfigurationProperties(prefix = "async")
public record AsyncProperties(@NotNull Integer corePoolSize, @NotNull Integer maxPoolSize, @NotNull Integer queueCapacity) {
}
