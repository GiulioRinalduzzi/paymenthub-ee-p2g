package org.mifos.pheebillpay;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mifos.connector.common.channel.dto.PhErrorDTO;
import org.mifos.connector.common.exception.PaymentHubError;
import org.mifos.pheebillpay.data.BillDetails;
import org.mifos.pheebillpay.data.PayerRequestDTO;
import org.mifos.pheebillpay.data.ResponseDTO;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * The places that wrote JSON with a private {@code new ObjectMapper()} now use the application's mapper. That mapper is
 * the one Spring Boot configures, which is not the same object: it writes dates as text instead of numbers, for one. So
 * this checks, payload by payload, that the configured bean and a plain {@code new ObjectMapper()} write exactly the
 * same bytes for everything those call sites send.
 *
 * <p>
 * The bean is taken from a context with Spring Boot's own Jackson auto-configuration and the shipped application.yml,
 * so it is the mapper the application really injects, not a builder that only looks like it.
 * </p>
 */
class JsonPayloadUnchangedTest {

    private final ObjectMapper plain = new ObjectMapper();

    private void assertSameJson(Object payload) {
        new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class))
                .withInitializer(new ConfigDataApplicationContextInitializer()).run(context -> {
                    ObjectMapper configured = context.getBean(ObjectMapper.class);
                    assertThat(configured.writeValueAsString(payload)).isEqualTo(plain.writeValueAsString(payload));
                });
    }

    @Test
    void payerRequestToPayIsUnchanged() {
        // ZeebeWorkers, payerRtpRequest: the body posted to the connector
        PayerRequestDTO payload = new PayerRequestDTO("2251799813685249", "4f1c4b0a-6f0e-4f7e-9d1c-0c6f2b1b6c11", 123456,
                new BillDetails("001", "Biller1", 150.5));
        assertSameJson(payload);
        assertSameJson(new PayerRequestDTO("1", null, null, null));
    }

    @Test
    void errorCallbackIsUnchanged() {
        // ZeebeWorkers, sendError: the body posted to the caller's callback URL
        Map<String, Object> errorInfo = new HashMap<>();
        errorInfo.put("errorMessage", "Payer FI was unreachable");
        assertSameJson(errorInfo);
    }

    @Test
    void billInquiryCallbackIsUnchanged() {
        // BillInquiryRouteBuilder: the bill inquiry response arrives as a Zeebe variable, so as nested maps and numbers
        Map<String, Object> bill = new LinkedHashMap<>();
        bill.put("billerId", "001");
        bill.put("dueDate", "2026-10-31");
        bill.put("amountonDueDate", 100.0);
        bill.put("amountAfterDueDate", null);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("code", "00");
        response.put("billId", "001");
        response.put("billDetails", bill);
        response.put("items", List.of(1, 2.5, "three"));
        assertSameJson(response);
    }

    @Test
    void failureAndPaymentNotificationResponsesAreUnchanged() {
        // BillInquiryRouteBuilder failure branch and BillPaymentNotificationRouteBuilder
        assertSameJson(new ResponseDTO("01", "Bill Id does not exist", "c0ffee-1"));
        assertSameJson(new ResponseDTO(null, null, null));
    }

    @Test
    void headerValidationErrorIsUnchanged() {
        // HeaderValidationInterceptor: the 400 body returned when a required header is missing
        PhErrorDTO payload = new PhErrorDTO.PhErrorDTOBuilder(PaymentHubError.ExtValidationError).developerMessage("Header missing")
                .defaultUserMessage("Header missing").addErrorParameter("Platform-TenantId", "missing").build();
        assertSameJson(payload);
    }
}
