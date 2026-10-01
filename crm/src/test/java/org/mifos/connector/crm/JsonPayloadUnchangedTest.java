package org.mifos.connector.crm;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mifos.connector.crm.data.Bill;
import org.mifos.connector.crm.data.BillInquiryResponseDTO;
import org.mifos.connector.crm.data.BillPaymentsResponseDTO;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * The two routes that wrote their response with a private {@code new ObjectMapper()} now use the application's mapper,
 * the one Spring Boot configures. This checks that it writes exactly the same bytes as a plain
 * {@code new ObjectMapper()} for both responses. The bean comes from a context with Spring Boot's Jackson
 * auto-configuration, so it is the mapper the application really injects.
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
    void billInquiryResponseIsUnchanged() {
        // BillInquiryRouteBuilder
        Bill bill = new Bill("001", "Biller1", "UNPAID", "2026-10-31", "100", "110");
        assertSameJson(new BillInquiryResponseDTO("00", "Success", "c0ffee-1", "001", bill));
        assertSameJson(new BillInquiryResponseDTO("02", "Invalid bill id", "c0ffee-2", "002", null));
    }

    @Test
    void billPaymentResponseIsUnchanged() {
        // BillPayRouteBuilder
        assertSameJson(new BillPaymentsResponseDTO("00", "Success", "req-1", "001", "pay-ref-1", "PAID"));
        assertSameJson(new BillPaymentsResponseDTO(null, null, null, null, null, null));
    }
}
