package org.mifos.connector.crm.properties;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

/**
 * Binds every configuration record the way the application does, from the shipped configuration: this module's
 * application.yml and the application.yaml that paymenthub-ee-core puts on the classpath.
 */
class ConfigurationBindingTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
            .withInitializer(new ConfigDataApplicationContextInitializer()).withUserConfiguration(AllRecords.class);

    @Test
    void bindsEveryRecordFromTheShippedConfiguration() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            BillPayProperties billPay = context.getBean(BillPayProperties.class);
            // the yaml block is spelled billPay; it binds to the canonical billpay prefix
            assertThat(billPay.billAlreadyPaidId()).isEqualTo("003");
            assertThat(billPay.billIdInvalidId()).isEqualTo("002");
            assertThat(billPay.billIdEmptyId()).isEqualTo("00");
            assertThat(billPay.billPayTimeoutId()).isEqualTo("005");
            assertThat(context.getBean(ZeebeProperties.class).client().maxExecutionThreads()).isEqualTo(100);
            assertThat(context.getBean(StatusProperties.class).billReqAcceptedId()).isEqualTo("123");
            assertThat(context.getBean(StatusProperties.class).billTimeout()).isEqualTo(3);
        });
    }

    /**
     * Every key was a bare {@code @Value} before, so a missing one stopped startup. Each record is checked on its own,
     * because which one fails first when several are missing is not deterministic.
     */
    @ParameterizedTest
    @ValueSource(classes = { OnlyBillPay.class, OnlyZeebe.class, OnlyStatus.class })
    void refusesToStartWhenTheSectionIsMissing(Class<?> onlyOneRecord) {
        new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
                .withUserConfiguration(onlyOneRecord).run(ConfigurationBindingTest::failedOnValidation);
    }

    @Test
    void refusesToStartWhenANumberIsSetToNothing() {
        runner.withPropertyValues("status.billTimeout=").run(ConfigurationBindingTest::failedOnValidation);
    }

    private static void failedOnValidation(AssertableApplicationContext context) {
        assertThat(context).getFailure().hasRootCauseInstanceOf(BindValidationException.class);
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties({ BillPayProperties.class, ZeebeProperties.class, StatusProperties.class })
    static class AllRecords {}

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(BillPayProperties.class)
    static class OnlyBillPay {}

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(ZeebeProperties.class)
    static class OnlyZeebe {}

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(StatusProperties.class)
    static class OnlyStatus {}
}
