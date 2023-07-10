package com.example;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOptionsBuilders;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.*;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.TransportUtils;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.example.pojo.HotelDoc;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.CollectionUtils;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class HotelSearchTest {
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
    void testMatchAll() throws IOException {
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index("hotel")
                .query(builder -> builder
                        .matchAll(matchAllQuery -> matchAllQuery))
                .build();
        SearchResponse<HotelDoc> searchResponse = client.search(searchRequest, HotelDoc.class);
        // 解析结果
        handleResponse(searchResponse);
    }

    @Test
    void testMatch() throws IOException {
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index("hotel")
                .query(builder -> builder
                        .match(matchQuery -> matchQuery.field("all").query("如家")))
                .build();
        SearchResponse<HotelDoc> searchResponse = client.search(searchRequest, HotelDoc.class);
        // 解析结果
        handleResponse(searchResponse);
    }

    @Test
    void testBool() throws IOException {
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index("hotel")
                .query(builder -> builder
                        .bool(boolQuery -> boolQuery
                                .must(queryBuilder -> queryBuilder.term(QueryBuilders.term().field("city").value("上海").build()))
                                .filter(queryBuilder -> queryBuilder.range(QueryBuilders.range().field("price").lte(JsonData.of(200)).build()))
                        )
                )
                .build();
        SearchResponse<HotelDoc> searchResponse = client.search(searchRequest, HotelDoc.class);
        // 解析结果
        handleResponse(searchResponse);
    }

    @Test
    void testPageAndSort() throws IOException {
        int page = 1, size = 5;
        SearchRequest searchRequest = new SearchRequest.Builder()
                .query(QueryBuilders.matchAll().build()._toQuery())
                .from((page - 1) * size).size(size)
                .sort(SortOptionsBuilders.field(fieldSortBuilder -> fieldSortBuilder.field("price").order(SortOrder.Asc)))
                .build();
        SearchResponse<HotelDoc> searchResponse = client.search(searchRequest, HotelDoc.class);
        // 解析结果
        handleResponse(searchResponse);
    }

    @Test
    void testHighlight() throws IOException {
        SearchRequest searchRequest = new SearchRequest.Builder()
                .query(QueryBuilders.match().field("all").query("如家").build()._toQuery())
                .highlight(new Highlight.Builder().fields("name",
                        new HighlightField.Builder().requireFieldMatch(false).build()).build())
                .build();
        SearchResponse<HotelDoc> searchResponse = client.search(searchRequest, HotelDoc.class);
        // 解析结果
        handleResponse(searchResponse);
    }

    private static void handleResponse(SearchResponse<HotelDoc> searchResponse) {
        HitsMetadata<HotelDoc> hitsMetadata = searchResponse.hits();
        TotalHits totalHits = hitsMetadata.total();
        System.out.println("查询到的条数：" + totalHits.value());

        List<Hit<HotelDoc>> hitList = hitsMetadata.hits();
        for (Hit<HotelDoc> hit : hitList) {
            HotelDoc hotelDoc = hit.source();
            Map<String, List<String>> highlight = hit.highlight();
            if (!CollectionUtils.isEmpty(highlight)) {
                List<String> names = highlight.get("name");
                if (!CollectionUtils.isEmpty(names)) {
                    String name = names.get(0);
                    hotelDoc.setName(name);
                }
            }
            System.out.println(hotelDoc);
        }
    }

    @Test
    void testAggregation() throws IOException {
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index("hotel")
                .size(0)
                .aggregations("brandAgg",
                        new Aggregation.Builder()
                                .terms(new TermsAggregation
                                        .Builder()
                                        .field("brand")
                                        .size(20)
                                        .build()
                                ).build()
                ).build();

        SearchResponse<HotelDoc> searchResponse = client.search(
                searchRequest,
                HotelDoc.class);

        Map<String, Aggregate> map = searchResponse.aggregations();
        Aggregate aggregate = map.get("brandAgg");
        System.out.println(aggregate);
        System.out.println(aggregate._kind());
        StringTermsAggregate stringTermsAggregate = aggregate.sterms();
        Buckets<StringTermsBucket> buckets = stringTermsAggregate.buckets();
        List<StringTermsBucket> list = buckets.array();
        for (StringTermsBucket bucket :
                list) {
            System.out.println(bucket.key()._toJsonString());
            // System.out.println(bucket.docCount());
        }
    }
}
