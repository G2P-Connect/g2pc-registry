package g2pc.dp.core.lib.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import g2pc.core.lib.dto.common.cache.CacheDTO;
import g2pc.core.lib.dto.common.message.response.DataDTO;

public interface ResponseBuilderService {

    String buildResponseMessage(DataDTO dataDTO) throws JsonProcessingException;

    String buildResponseHeader(String headerInfoString, String messageString) throws JsonProcessingException;

    String getResponse(String headerInfoString, String messageString, String algorithm) throws JsonProcessingException;

    String buildResponseString(String cacheKeySearchString, DataDTO dataDTO) throws JsonProcessingException;

    void sendOnSearchResponse(String responseString, String uri);

    void updateRequestStatus(String cacheKey, String status, CacheDTO cacheDTO) throws JsonProcessingException;
}
