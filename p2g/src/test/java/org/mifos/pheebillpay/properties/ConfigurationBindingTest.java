package org.mifos.pheebillpay.properties;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
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
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;

/**
 * Binds every configuration record the way the application does, from the shipped configuration: this module's
 * application.yml and the application.yaml that paymenthub-ee-core puts on the classpath.
 *
 * <p>
 * Several of the names are awkward and each one is pinned: {@code billpay} and {@code billPay} are two blocks in the
 * yaml that bind as one prefix, {@code payer_fsp} and {@code async.core_pool_size} use underscores, and
 * {@code billPay.FspNotOnboarded} starts with a capital letter.
 * </p>
 *
 * <p>
 * The service hostnames are only checked to be bound URLs, not pinned by name: the services are being renamed from
 * {@code ph-ee-*} to {@code paymenthub-ee-*}, and that change belongs to the yaml, not to this test.
 * </p>
 */
class ConfigurationBindingTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
            .withInitializer(new ConfigDataApplicationContextInitializer()).withUserConfiguration(AllRecords.class);

    @Test
    void bindsEveryRecordFromTheShippedConfiguration() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context.getBean(ZeebeProperties.class).client().maxExecutionThreads()).isEqualTo(100);
            assertThat(context.getBean(ZeebeProperties.class).client().evenlyAllocatedMaxJobs()).isEqualTo(100);
            assertThat(context.getBean(AsyncProperties.class).queueCapacity()).isEqualTo(100);
            assertThat(context.getBean(BpmnFlowsProperties.class).billRequest()).isEqualTo("bill_request-{dfspid}");
            assertThat(context.getBean(OperationsProperties.class).endpoint().transactionReq())
                    .isEqualTo("/transactionRequests/?page=0&size=1");
            assertThat(context.getBean(ConnectorProperties.class).contactpoint()).startsWith("http://");
            assertThat(context.getBean(StatusProperties.class).billTimeout()).isEqualTo(6);
        });
    }

    @Test
    void bindsTheTwoBillPayBlocksAsOnePrefix() {
        runner.run(context -> {
            BillPayProperties properties = context.getBean(BillPayProperties.class);
            // from the lowercase billpay block
            assertThat(properties.contactpoint()).startsWith("http://");
            assertThat(properties.endpoint().payerRtpResponse()).isEqualTo("/billTransferRequests");
            // from the camel-case billPay block
            assertThat(properties.fspNotOnboarded()).isEqualTo("003");
            assertThat(properties.billIdEmptyOriginal()).isEqualTo("004");
            assertThat(properties.billIdEmpty()).isEqualTo("00");
        });
    }

    @Test
    void bindsThePayerFspPrefixWithItsUnderscore() {
        runner.run(context -> {
            PayerFspProperties properties = context.getBean(PayerFspProperties.class);
            assertThat(properties.tenant()).isEqualTo("lion");
            assertThat(properties.mockPayerUnreachable().financialAddress()).isEqualTo("122333");
            assertThat(properties.mockDebitFailed().financialAddress()).isEqualTo("1223334444");
        });
    }

    @Test
    void takesTheZeebeGatewayTheDeploymentSets() {
        runner.withInitializer(context -> context.getEnvironment().getPropertySources()
                .addFirst(new SystemEnvironmentPropertySource(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME + "-test",
                        Map.of("ZEEBE_BROKER_CONTACTPOINT", "paymenthub-infra-zeebe-gateway:26500"))))
                .run(context -> assertThat(context.getBean(ZeebeProperties.class).broker().contactpoint())
                        .isEqualTo("paymenthub-infra-zeebe-gateway:26500"));
    }

    /**
     * Every key was a bare {@code @Value} before, so a missing one stopped startup. Each record is checked on its own,
     * because which one fails first when several are missing is not deterministic.
     */
    @ParameterizedTest
    @ValueSource(classes = { OnlyZeebe.class, OnlyAsync.class, OnlyBpmnFlows.class, OnlyOperations.class, OnlyBillPay.class,
            OnlyConnector.class, OnlyPayerFsp.class, OnlyStatus.class })
    void refusesToStartWhenTheSectionIsMissing(Class<?> onlyOneRecord) {
        new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
                .withUserConfiguration(onlyOneRecord).run(ConfigurationBindingTest::failedOnValidation);
    }

    @Test
    void refusesToStartWhenANumberIsSetToNothing() {
        runner.withPropertyValues("status.billTimeout=").run(ConfigurationBindingTest::failedOnValidation);
    }

    @Test
    void acceptsAStringSetToNothing() {
        // a bare @Value accepted an empty string, and @NotNull does too: no stricter than before
        runner.withPropertyValues("connector.contactpoint=").run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context.getBean(ConnectorProperties.class).contactpoint()).isEmpty();
        });
    }

    private static void failedOnValidation(AssertableApplicationContext context) {
        assertThat(context).getFailure().hasRootCauseInstanceOf(BindValidationException.class);
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties({ ZeebeProperties.class, AsyncProperties.class, BpmnFlowsProperties.class, OperationsProperties.class,
            BillPayProperties.class, ConnectorProperties.class, PayerFspProperties.class, StatusProperties.class })
    static class AllRecords {}

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(ZeebeProperties.class)
    static class OnlyZeebe {}

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(AsyncProperties.class)
    static class OnlyAsync {}

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(BpmnFlowsProperties.class)
    static class OnlyBpmnFlows {}

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(OperationsProperties.class)
    static class OnlyOperations {}

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(BillPayProperties.class)
    static class OnlyBillPay {}

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(ConnectorProperties.class)
    static class OnlyConnector {}

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(PayerFspProperties.class)
    static class OnlyPayerFsp {}

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(StatusProperties.class)
    static class OnlyStatus {}
}
