package com.example;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.TransportUtils;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.example.pojo.Hotel;
import com.example.pojo.HotelDoc;
import com.example.service.IHotelService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.List;

@SpringBootTest
public class HotelDocumentTest {
    @Autowired
    private IHotelService hotelService;

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

    @AfterEach
    void tearDown() throws IOException {
        transport.close();
        restClient.close();
    }

    @Test
    void testAddDocument() throws IOException {
        Hotel hotel = hotelService.getById(36934L);
        HotelDoc hotelDoc = new HotelDoc(hotel);
        IndexRequest<HotelDoc> indexRequest = new IndexRequest
                .Builder<HotelDoc>()
                .index("hotel")
                .id("36934")
                .document(hotelDoc).build();
        IndexResponse response = client.index(indexRequest);
        System.out.println(response);
    }

    @Test
    void testGetDocument() throws IOException {
        GetRequest getRequest = new GetRequest.Builder()
                .index("hotel").id("36934").build();
        GetResponse<HotelDoc> getResponse = client.get(getRequest, HotelDoc.class);
        System.out.println(getResponse.found());
        HotelDoc hotelDoc = getResponse.source();
        System.out.println(getResponse.id());
        System.out.println(hotelDoc);
    }

    @Test
    void testUpdateDocument() throws IOException {
        HotelDoc hotelDoc = new HotelDoc();
        hotelDoc.setPrice(500);
        hotelDoc.setStarName("四钻");
        UpdateRequest<HotelDoc, HotelDoc> updateRequest = new UpdateRequest.Builder<HotelDoc, HotelDoc>()
                .index("hotel").id("36934")
                .doc(hotelDoc)
                .build();
        UpdateResponse<HotelDoc> updateResponse = client.update(updateRequest, HotelDoc.class);
        System.out.println(updateResponse.id());
    }

    @Test
    void testDeleteDocument() throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest.Builder()
                .index("hotel").id("36934").build();
        DeleteResponse deleteResponse = client.delete(deleteRequest);
        System.out.println(deleteResponse);
    }

    @Test
    void testBulkRequest() throws IOException {
        BulkRequest.Builder builder = new BulkRequest.Builder();
        List<Hotel> list = hotelService.list();
        for (Hotel hotel : list) {
            HotelDoc doc = new HotelDoc(hotel);
            builder.operations(b -> b.index(idx -> idx.index("hotel").id(doc.getId().toString()).document(doc)));
        }
        BulkResponse bulkResponse = client.bulk(builder.build());
        List<BulkResponseItem> responseItems = bulkResponse.items();
        for (BulkResponseItem responseItem : responseItems) {
            System.out.println(responseItem.result());
            if (responseItem.error() != null) {
                System.out.println(responseItem.error().reason());
            }
        }
    }
}
