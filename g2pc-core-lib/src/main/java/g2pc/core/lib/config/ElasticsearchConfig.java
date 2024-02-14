package g2pc.core.lib.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {

    @Value("${sunbird.elasticsearch.host}")
    private String elasticsearchHost;

    @Value("${sunbird.elasticsearch.port}")
    private int elasticsearchPort;

    @Value("${sunbird.elasticsearch.scheme}")
    private String elasticsearchScheme;

    @Bean
    public RestHighLevelClient client() {
        return new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(elasticsearchHost, elasticsearchPort, elasticsearchScheme)
                )
        );
    }
}
