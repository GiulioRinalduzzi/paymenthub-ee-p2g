package org.mifos.pheebillpay.config;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.mifos.pheebillpay.properties.ZeebeProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * The one HTTP client for the outgoing calls: the payer request-to-pay, the two callbacks and the bill status lookup.
 *
 * <p>
 * Each of those four call sites used to build a whole HTTP client and a new {@code RestTemplate} for every call, so no
 * connection was ever reused. The TLS setup is exactly what they built: any certificate chain is trusted and the host
 * name is not checked. That is unchanged here; it is now written down in one place instead of four.
 * </p>
 *
 * <p>
 * A client built per call had a pool of its own and so no limit on how many calls could run at once. A shared pool
 * allows only five connections per host by default, which would queue the Zeebe workers behind each other. The limit is
 * set to {@code zeebe.client.max-execution-threads}, the most worker threads that can make a call at the same time.
 * </p>
 */
@Configuration
public class HttpClientConfig {

    @Bean(destroyMethod = "close")
    public CloseableHttpClient httpClient(ZeebeProperties zeebeProperties)
            throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        int maxConnections = zeebeProperties.client().maxExecutionThreads();
        return HttpClients.custom()
                .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
                        .setSSLSocketFactory(new SSLConnectionSocketFactory(
                                new SSLContextBuilder().loadTrustMaterial(null, (certificate, authType) -> true).build(),
                                NoopHostnameVerifier.INSTANCE))
                        .setMaxConnPerRoute(maxConnections).setMaxConnTotal(maxConnections).build())
                .build();
    }

    @Bean
    public RestTemplate restTemplate(CloseableHttpClient httpClient) {
        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
    }
}
