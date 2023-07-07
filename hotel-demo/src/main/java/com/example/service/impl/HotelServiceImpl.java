package com.example.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
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
import java.util.LinkedList;
import java.util.List;

@Service
public class HotelServiceImpl extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {
    @Override
    public PageResult search(RequestParams requestParams) {
        RestClient restClient = null;
        ElasticsearchTransport transport = null;
        try {
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
            ElasticsearchClient client = new ElasticsearchClient(transport);

            SearchRequest.Builder builder = new SearchRequest.Builder();
            String key = requestParams.getKey();
            if (StringUtils.isBlank(key)) {
                builder.query(QueryBuilders.matchAll().build()._toQuery());
            } else {
                builder.query(QueryBuilders.match().field("all").query(key).build()._toQuery());
            }
            Integer page = requestParams.getPage();
            Integer size = requestParams.getSize();

            builder.from((page - 1) * size).size(size);
            SearchResponse<HotelDoc> searchResponse = client.search(builder.build(), HotelDoc.class);
            return handleResponse(searchResponse);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            close(transport);
            close(restClient);
        }
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
            list.add(hotelDoc);
        }
        return new PageResult(total, list);
    }
}
