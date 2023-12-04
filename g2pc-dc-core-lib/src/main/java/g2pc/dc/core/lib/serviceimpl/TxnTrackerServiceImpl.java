package g2pc.dc.core.lib.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.dto.common.cache.CacheDTO;
import g2pc.core.lib.enums.HeaderStatusENUM;
import g2pc.core.lib.utils.CommonUtils;
import g2pc.dc.core.lib.service.TxnTrackerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class TxnTrackerServiceImpl implements TxnTrackerService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * Save initial payload to Redis and create a new transaction in DB table
     *
     * @param payloadMapList required
     * @param transactionId required
     * @param status required
     *
     */
    @Override
    public void saveInitialTransaction(List<Map<String, Object>> payloadMapList, String transactionId, String status) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        CacheDTO cacheDTO = createCache(objectMapper.writeValueAsString(payloadMapList), HeaderStatusENUM.PDNG.toValue());
        saveCache(cacheDTO, "initial-" + transactionId);
    }

    /**
     * Save request transactions to Redis
     *
     * @param requestString required
     * @param regType required
     * @param transactionId required
     *
     */
    @Override
    public void saveRequestTransaction(String requestString, String regType, String transactionId) throws JsonProcessingException {
        CacheDTO cacheDTO = createCache(requestString, HeaderStatusENUM.PDNG.toValue());
        saveCache(cacheDTO, regType + "-" + transactionId);
    }

    /**
     * Create cache to save in Redis
     *
     * @param data   required
     * @param status required
     * @return CacheDTO
     */
    @Override
    public CacheDTO createCache(String data, String status) {
        log.info("Save requests in cache with status pending");
        CacheDTO cacheDTO = new CacheDTO();
        cacheDTO.setData(data);
        cacheDTO.setStatus(status);
        cacheDTO.setCreatedDate(CommonUtils.getCurrentTimeStamp());
        cacheDTO.setLastUpdatedDate(CommonUtils.getCurrentTimeStamp());
        return cacheDTO;
    }

    /**
     * Save cache in Redis
     *
     * @param cacheDTO required
     * @param cacheKey required
     */
    @Override
    public void saveCache(CacheDTO cacheDTO, String cacheKey) throws JsonProcessingException {
        ValueOperations<String, String> val = redisTemplate.opsForValue();
        val.set(cacheKey, new ObjectMapper().writeValueAsString(cacheDTO));
    }
}
