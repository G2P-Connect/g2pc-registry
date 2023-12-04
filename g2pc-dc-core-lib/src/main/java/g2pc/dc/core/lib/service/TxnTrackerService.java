package g2pc.dc.core.lib.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import g2pc.core.lib.dto.common.cache.CacheDTO;

import java.util.List;
import java.util.Map;

public interface TxnTrackerService {

    void saveInitialTransaction(List<Map<String, Object>> payloadMapList, String transactionId, String status) throws JsonProcessingException;

    void saveRequestTransaction(String requestString, String regType, String transactionId) throws JsonProcessingException;

    CacheDTO createCache(String data, String status);

    void saveCache(CacheDTO cacheDTO, String cacheKey) throws JsonProcessingException;
}
