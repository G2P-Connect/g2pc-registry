package g2pc.dp.core.lib.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.dto.common.cache.CacheDTO;
import g2pc.core.lib.utils.CommonUtils;
import g2pc.dp.core.lib.service.TxnTrackerRedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@Slf4j
public class TxnTrackerRedisServiceImpl implements TxnTrackerRedisService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * Save a request in Redis cache
     * @param cacheDTO cache dto to save in cache
     * @param cacheKey cache key for which data is stored
     */
    @Override
    public void saveRequestDetails(CacheDTO cacheDTO, String cacheKey) throws JsonProcessingException {
        ValueOperations<String, String> val = redisTemplate.opsForValue();
        val.set(cacheKey, (new ObjectMapper()).writeValueAsString(cacheDTO));
    }

    /**
     * Update a request in Redis cache after response
     *
     * @param cacheKey cache key for which data is stored
     * @param status status to be 
     * @param cacheDTO cache dto to save in cache
     */
    @Override
    public void updateRequestDetails(String cacheKey, String status, CacheDTO cacheDTO) throws JsonProcessingException {
        log.info("Updated cache status");

        cacheDTO.setStatus(status);
        cacheDTO.setLastUpdatedDate(CommonUtils.getCurrentTimeStamp());
        ValueOperations<String, String> val = redisTemplate.opsForValue();
        val.set(cacheKey, new ObjectMapper().writeValueAsString(cacheDTO));
    }

    /**
     * Get all cache keys from Redis
     *
     * @param cacheKeySearchString required unique to DP
     * @return List of cache keys
     */
    @Override
    public List<String> getCacheKeys(String cacheKeySearchString) {
        Set<String> redisKeys = redisTemplate.keys(cacheKeySearchString);

        return new ArrayList<>(Objects.requireNonNull(redisKeys));
    }

    /**
     * Geta a single request with its cache key
     *
     * @param cacheKey required unique to DP
     * @return Request data
     */
    @Override
    public String getRequestData(String cacheKey) {
        return redisTemplate.opsForValue().get(cacheKey);
    }
}
