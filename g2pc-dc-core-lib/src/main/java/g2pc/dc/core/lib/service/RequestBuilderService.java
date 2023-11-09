package g2pc.dc.core.lib.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import g2pc.core.lib.dto.common.cache.CacheDTO;
import g2pc.core.lib.dto.common.header.HeaderDTO;
import g2pc.core.lib.dto.common.message.request.MessageDTO;
import g2pc.core.lib.dto.common.message.request.SearchCriteriaDTO;

public interface RequestBuilderService {

    MessageDTO buildMessage(SearchCriteriaDTO searchCriteriaDTO) ;

    HeaderDTO buildHeader();

    String buildRequest(SearchCriteriaDTO searchCriteriaDTO,String transactionId) throws JsonProcessingException;

    void sendRequest(String requestString, String uri);

    CacheDTO createCache(String data, String status);

    void saveCache(CacheDTO cacheDTO, String cacheKey) throws JsonProcessingException;
}
