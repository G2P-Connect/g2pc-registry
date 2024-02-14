package g2pc.core.lib.service;

import org.elasticsearch.action.search.SearchResponse;

import java.io.IOException;
import java.util.Map;

public interface ElasticsearchService {

    SearchResponse exactSearch(String index, Map<String, String> fieldValues) throws IOException;

    void clearData(String index) throws IOException;
}
