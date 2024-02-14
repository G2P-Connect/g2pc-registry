package g2pc.dp.core.lib.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import g2pc.core.lib.dto.common.cache.CacheDTO;
import java.util.List;

public interface TxnTrackerRedisService {

    void saveRequestDetails(CacheDTO cacheDTO, String cacheKey) throws JsonProcessingException;

    void updateRequestDetails(String cacheKey, String status, CacheDTO cacheDTO) throws JsonProcessingException;

    List<String> getCacheKeys(String cacheKeySearchString);

    String getRequestData(String cacheKey) throws JsonProcessingException;
}
