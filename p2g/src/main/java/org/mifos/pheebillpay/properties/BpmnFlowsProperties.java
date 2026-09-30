package org.mifos.pheebillpay.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * The three workflows this service starts, with {@code {dfspid}} replaced by the tenant. Every value is required, as it
 * was when it was a bare {@code @Value} field.
 */
@Validated
@ConfigurationProperties(prefix = "bpmn.flows")
public record BpmnFlowsProperties(@NotNull String billPay, @NotNull String paymentNotification, @NotNull String billRequest) {
}
