package com.mindgames.backend.config;

import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest5_client.Rest5ClientTransport;
import co.elastic.clients.transport.rest5_client.low_level.Rest5Client;

/**
 * Cliente de Elasticsearch. La URL y las credenciales se leen de la configuración
 * (variables ES_URIS, ES_USERNAME y ES_PASSWORD); no se escriben en el código.
 */
@Configuration
public class ElasticsearchConfig {

    @Bean
    public Rest5Client rest5Client(@Value("${spring.elasticsearch.uris}") String uri,
                                   @Value("${spring.elasticsearch.username}") String usuario,
                                   @Value("${spring.elasticsearch.password}") String password) throws Exception {

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return null; }
                public void checkClientTrusted(X509Certificate[] c, String a) {}
                public void checkServerTrusted(X509Certificate[] c, String a) {}
            }
        }, null);

        TlsStrategy tlsStrategy = ClientTlsStrategyBuilder.create()
            .setSslContext(sslContext)
            .setHostnameVerifier((host, session) -> true)
            .build();

        BasicCredentialsProvider credsProv = new BasicCredentialsProvider();
        credsProv.setCredentials(
            new org.apache.hc.client5.http.auth.AuthScope(null, -1),
            new UsernamePasswordCredentials(usuario, password.toCharArray())
        );

        return Rest5Client.builder(HttpHost.create(uri))
            .setHttpClientConfigCallback(hc -> hc
                .setConnectionManager(
                    PoolingAsyncClientConnectionManagerBuilder.create()
                        .setTlsStrategy(tlsStrategy)
                        .build()
                )
                .setDefaultCredentialsProvider(credsProv)
            )
            .build();
    }

    @Bean
    public ElasticsearchTransport elasticsearchTransport(Rest5Client rest5Client) {
        return new Rest5ClientTransport(rest5Client, new JacksonJsonpMapper());
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport transport) {
        return new ElasticsearchClient(transport);
    }

    @Bean
    public ElasticsearchTemplate elasticsearchTemplate(ElasticsearchClient client) {
        return new ElasticsearchTemplate(client);
    }
}
