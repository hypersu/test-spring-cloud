package com.example;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.*;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.TransportUtils;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import javax.net.ssl.SSLContext;
import java.io.IOException;

public class HotelIndexTest {
    private RestClient restClient;
    private ElasticsearchTransport transport;
    private ElasticsearchClient client;

    @BeforeEach
    void setUp() throws IOException {
        // 使用ca 证书里面包含ca 公钥和一些ca信息
        ClassPathResource classPathResource = new ClassPathResource("http_ca.crt");

        SSLContext sslContext = TransportUtils.sslContextFromHttpCaCrt(classPathResource.getFile());
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("elastic", "123456"));
        // create a low level client
        restClient = RestClient.builder(
                        HttpHost.create("https://192.168.16.101:9200"))
                .setHttpClientConfigCallback(
                        (httpAsyncClientBuilder) -> {
                            httpAsyncClientBuilder.setSSLContext(sslContext);
                            httpAsyncClientBuilder.setDefaultCredentialsProvider(provider);
                            httpAsyncClientBuilder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
                            return httpAsyncClientBuilder;
                        })
                .build();
        // create the transport with a Jackson Mapper
        transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());
        // create a api
        client = new ElasticsearchClient(transport);
    }

    @Test
    void testCreateIndex() throws IOException {
        ClassPathResource resource = new ClassPathResource("hotel.json");
        CreateIndexRequest request = new CreateIndexRequest
                .Builder()
                .index("hotel")
                .withJson(resource.getInputStream())
                .build();
        CreateIndexResponse response = client.indices().create(request);
        System.out.println(response);
    }

    @Test
    void testDeleteIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest
                .Builder().index("hotel").build();
        DeleteIndexResponse response = client.indices().delete(request);
        System.out.println(response);
    }

    @Test
    void testExistsIndex() throws IOException {
        ExistsRequest request = new ExistsRequest
                .Builder().index("hotel").build();
        BooleanResponse response = client.indices().exists(request);
        System.out.println(response.value());
    }

    @AfterEach
    void tearDown() throws IOException {
        transport.close();
        restClient.close();
    }

    @Test
    void testInit() {

    }


}
