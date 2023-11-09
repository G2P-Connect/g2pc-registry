package g2pc.dp.core.lib.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import g2pc.core.lib.dto.common.AcknowledgementDTO;
import g2pc.core.lib.dto.common.cache.CacheDTO;
import g2pc.core.lib.dto.common.header.RequestHeaderDTO;
import g2pc.core.lib.dto.common.message.request.MessageDTO;
import g2pc.core.lib.exceptions.G2pcValidationException;

import java.util.List;

public interface RequestHandlerService {

    AcknowledgementDTO buildCacheRequest(String requestData, String cacheKey) throws Exception;

    void saveCacheRequest(CacheDTO cacheDTO, String cacheKey) throws JsonProcessingException;

    List<String> getCacheKeys(String cacheKeySearchString);

    String getRequestData(String cacheKey) throws JsonProcessingException;

    public void validateRequestHeader(RequestHeaderDTO headerDTO) throws G2pcValidationException, JsonProcessingException;

    public void validateRequestMessage(MessageDTO messageDTO) throws G2pcValidationException, JsonProcessingException;
}
