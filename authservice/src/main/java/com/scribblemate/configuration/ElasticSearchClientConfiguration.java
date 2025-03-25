package com.scribblemate.configuration;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchClients;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.elasticsearch.support.HttpHeaders;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.scribblemate.repositories.elastic")
public class ElasticSearchClientConfiguration extends ElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris}")
    private String url;

    @Value("${spring.elasticsearch.username}")
    private String userName;

    @Value("${spring.elasticsearch.password}")
    private String password;

    @Bean
    @Override
    public ClientConfiguration clientConfiguration() {
        // Temporary for local development
        // Bypassing security
        SSLContext sslContext;
        try {
            sslContext = SSLContexts.custom()
                    .loadTrustMaterial(null, (chain, authType) -> true) // Trust all certificates
                    .build();
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            throw new RuntimeException("Failed to create a trust-all SSL context", e);
        }

        ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo(url)
                .usingSsl(sslContext)
                .withConnectTimeout(Duration.ofSeconds(5))
                .withSocketTimeout(Duration.ofSeconds(3))
                .withBasicAuth(userName, password)
                .withDefaultHeaders(compatibilityHeaders())
                .withHeaders(() -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("currentTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    return headers;
                })
                .withClientConfigurer(
                        ElasticsearchClients.ElasticsearchHttpClientConfigurationCallback.from(clientBuilder -> clientBuilder)).build();
        return clientConfiguration;
    }

    private HttpHeaders compatibilityHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(org.springframework.http.HttpHeaders.ACCEPT, "application/vnd.elasticsearch+json;compatible-with=7");
        headers.add(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/vnd.elasticsearch+json;compatible-with=7");
        return headers;
    }
}
