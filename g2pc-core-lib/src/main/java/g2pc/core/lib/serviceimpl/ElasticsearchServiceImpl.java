package g2pc.core.lib.serviceimpl;

import g2pc.core.lib.service.ElasticsearchService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;

import org.elasticsearch.index.query.BoolQueryBuilder;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;


@Service
@Slf4j
public class ElasticsearchServiceImpl implements ElasticsearchService {


    @Autowired
    private RestHighLevelClient client;

    @Override
    public SearchResponse exactSearch(String index, Map<String, String> fieldValues) throws IOException {
        SearchRequest searchRequest = new SearchRequest(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        for (Map.Entry<String, String> entry : fieldValues.entrySet()) {
            boolQueryBuilder.must(QueryBuilders.termQuery(entry.getKey(), entry.getValue()));
        }
        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);
        log.info("searchRequest: {}", searchSourceBuilder);
        return client.search(searchRequest, RequestOptions.DEFAULT);
    }

    @Override
    public void clearData(String index) throws IOException {
        DeleteByQueryRequest request = new DeleteByQueryRequest(index);
        request.setQuery(QueryBuilders.matchAllQuery());
        BulkByScrollResponse deleteResponse = client.deleteByQuery(request, RequestOptions.DEFAULT);
        log.info("DeleteResponse: {}", deleteResponse.toString());
    }
}
