package com.example.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.*;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScore;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.*;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.TransportUtils;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mapper.HotelMapper;
import com.example.pojo.Hotel;
import com.example.pojo.HotelDoc;
import com.example.pojo.PageResult;
import com.example.pojo.RequestParams;
import com.example.service.IHotelService;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class HotelServiceImpl extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {

    private RestClient getRestClient() throws IOException {
        // 使用ca 证书里面包含ca 公钥和一些ca信息
        ClassPathResource classPathResource = new ClassPathResource("http_ca.crt");

        SSLContext sslContext = TransportUtils.sslContextFromHttpCaCrt(classPathResource.getFile());
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("elastic", "123456"));
        // create a low level client
        return RestClient.builder(
                        HttpHost.create("https://192.168.16.101:9200"))
                .setHttpClientConfigCallback(
                        (httpAsyncClientBuilder) -> {
                            httpAsyncClientBuilder.setSSLContext(sslContext);
                            httpAsyncClientBuilder.setDefaultCredentialsProvider(provider);
                            httpAsyncClientBuilder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
                            return httpAsyncClientBuilder;
                        })
                .build();
    }

    @Override
    public PageResult search(RequestParams requestParams) {
        RestClient restClient = null;
        ElasticsearchTransport transport = null;
        try {
            restClient = getRestClient();
            // create the transport with a Jackson Mapper
            transport = new RestClientTransport(
                    restClient, new JacksonJsonpMapper());
            // create a api
            ElasticsearchClient client = new ElasticsearchClient(transport);

            SearchRequest.Builder builder = new SearchRequest.Builder();
            builder.index("hotel");

            buildBasicQuery(requestParams, builder);

            // 排序
            String location = requestParams.getLocation();
            if (StringUtils.isNotBlank(location)) {
                builder.sort(new SortOptions.Builder()
                        .geoDistance(new GeoDistanceSort.Builder()
                                .field("location")
                                .location(new GeoLocation.Builder().text(location).build())
                                .unit(DistanceUnit.Kilometers)
                                .order(SortOrder.Asc)
                                .build())
                        .build());
            }

            // 分页
            Integer page = requestParams.getPage();
            Integer size = requestParams.getSize();

            builder.from((page - 1) * size).size(size);
            SearchRequest searchRequest = builder.build();
            System.out.println(searchRequest.toString());
            SearchResponse<HotelDoc> searchResponse = client.search(searchRequest, HotelDoc.class);
            return handleResponse(searchResponse);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            close(transport);
            close(restClient);
        }
    }

    private static void buildBasicQuery(RequestParams requestParams, SearchRequest.Builder builder) {
        BoolQuery.Builder boolQueryBuilder = QueryBuilders.bool();
        String key = requestParams.getKey();
        if (StringUtils.isBlank(key)) {
            boolQueryBuilder.must(QueryBuilders.matchAll().build()._toQuery());
        } else {
            boolQueryBuilder.must(QueryBuilders.match().field("all").query(key).build()._toQuery());
        }
        String city = requestParams.getCity();
        if (StringUtils.isNotBlank(city)) {
            boolQueryBuilder.filter(QueryBuilders.term()
                    .field("city").value(city).build()._toQuery());
        }
        String brand = requestParams.getBrand();
        if (StringUtils.isNotBlank(brand)) {
            boolQueryBuilder.filter(QueryBuilders.term()
                    .field("brand").value(brand).build()._toQuery());
        }
        String starName = requestParams.getStarName();
        if (StringUtils.isNotBlank(starName)) {
            boolQueryBuilder.filter(QueryBuilders.term()
                    .field("starName").value(starName).build()._toQuery());
        }
        Integer minPrice = requestParams.getMinPrice();
        Integer maxPrice = requestParams.getMaxPrice();
        if (minPrice != null && maxPrice != null) {
            boolQueryBuilder.filter(QueryBuilders.range()
                    .field("price")
                    .gte(JsonData.of(minPrice))
                    .lte(JsonData.of(maxPrice))
                    .build()._toQuery());
        }

        FunctionScoreQuery.Builder functionScoreBuilder = QueryBuilders.functionScore();
        functionScoreBuilder.query(boolQueryBuilder.build()._toQuery());
        FunctionScore.Builder functionBuilder = new FunctionScore.Builder();
        functionBuilder.filter(QueryBuilders.term().field("isAD").value(true).build()._toQuery());
        functionBuilder.weight(10.0D);
        functionScoreBuilder.functions(functionBuilder.build());
        builder.query(functionScoreBuilder.build()._toQuery());
    }

    private void close(AutoCloseable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception ignored) {

        }
    }

    private static PageResult handleResponse(SearchResponse<HotelDoc> searchResponse) {
        HitsMetadata<HotelDoc> hitsMetadata = searchResponse.hits();
        long total = hitsMetadata.total().value();
        List<Hit<HotelDoc>> hitList = hitsMetadata.hits();
        List<HotelDoc> list = new LinkedList<>();
        for (Hit<HotelDoc> hit : hitList) {
            HotelDoc hotelDoc = hit.source();
            List<FieldValue> sorts = hit.sort();
            if (!sorts.isEmpty() && sorts.size() == 2) {
                FieldValue fieldValue = sorts.get(1);
                hotelDoc.setDistance(fieldValue.doubleValue());
            }
            list.add(hotelDoc);
        }
        return new PageResult(total, list);
    }

    @Override
    public Map<String, List<String>> filters(RequestParams requestParams) {
        RestClient restClient = null;
        ElasticsearchTransport transport = null;
        Map<String, List<String>> map = new HashMap<>();
        try {
            restClient = getRestClient();
            // create the transport with a Jackson Mapper
            transport = new RestClientTransport(
                    restClient, new JacksonJsonpMapper());
            // create a api
            ElasticsearchClient client = new ElasticsearchClient(transport);

            SearchRequest.Builder builder = new SearchRequest.Builder();
            builder.index("hotel");
            buildBasicQuery(requestParams, builder);
            builder.size(0);
            aggregate(builder, "cityAgg", "city");
            aggregate(builder, "brandAgg", "brand");
            aggregate(builder, "starAgg", "starName");

            SearchRequest searchRequest = builder.build();
            System.out.println(searchRequest.toString());
            SearchResponse<HotelDoc> searchResponse = client.search(searchRequest, HotelDoc.class);
            Map<String, Aggregate> aggregations = searchResponse.aggregations();
            map.put("city", getAggKeyList(aggregations, "cityAgg"));
            map.put("brand", getAggKeyList(aggregations, "brandAgg"));
            map.put("starName", getAggKeyList(aggregations, "starAgg"));

            return map;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            close(transport);
            close(restClient);
        }
    }

    private void aggregate(SearchRequest.Builder builder, String aggName, String field) {
        builder.aggregations(aggName,
                new Aggregation
                        .Builder()
                        .terms(
                                new TermsAggregation
                                        .Builder()
                                        .field(field)
                                        .size(100)
                                        .build()
                        ).build()
        );
    }

    private List<String> getAggKeyList(Map<String, Aggregate> aggregations, String aggName) {
        Aggregate aggregate = aggregations.get(aggName);
        StringTermsAggregate sAggregate = aggregate.sterms();
        Buckets<StringTermsBucket> buckets = sAggregate.buckets();
        List<StringTermsBucket> list = buckets.array();
        List<String> result = new LinkedList<>();
        for (StringTermsBucket bucket :
                list) {
            result.add(bucket.key().stringValue());
        }
        return result;
    }

    @Override
    public List<String> suggestion(String prefix) {
        RestClient restClient = null;
        ElasticsearchTransport transport = null;
        List<String> list = new LinkedList<>();
        try {
            restClient = getRestClient();
            transport = new RestClientTransport(restClient,
                    new JacksonJsonpMapper());
            ElasticsearchClient client = new ElasticsearchClient(transport);
            SearchRequest searchRequest = new SearchRequest
                    .Builder()
                    .suggest(new Suggester
                            .Builder()
                            .text(prefix)
                            .suggesters("hotelSuggest", new FieldSuggester
                                    .Builder()
                                    .completion(new CompletionSuggester
                                            .Builder()
                                            .field("suggestion")
                                            .skipDuplicates(true)
                                            .size(20)
                                            .build()
                                    )
                                    .build()
                            )
                            .build()
                    )
                    .build();
            SearchResponse<HotelDoc> searchResponse = client.search(searchRequest, HotelDoc.class);
            Map<String, List<Suggestion<HotelDoc>>> suggest = searchResponse.suggest();
            List<Suggestion<HotelDoc>> suggestions = suggest.get("hotelSuggest");
            suggestions.forEach(suggestion -> {
                CompletionSuggest<HotelDoc> completionSuggest = suggestion.completion();
                List<CompletionSuggestOption<HotelDoc>> options = completionSuggest.options();
                options.forEach(option -> {
                    list.add(option.text());
                });
            });
            return list;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            close(transport);
            close(restClient);
        }
    }
}
