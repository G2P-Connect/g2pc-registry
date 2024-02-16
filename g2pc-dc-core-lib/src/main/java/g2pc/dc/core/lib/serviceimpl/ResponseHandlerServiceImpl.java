package g2pc.dc.core.lib.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import g2pc.core.lib.constants.CoreConstants;
import g2pc.core.lib.dto.common.cache.CacheDTO;
import g2pc.core.lib.dto.common.header.RequestHeaderDTO;
import g2pc.core.lib.dto.common.header.ResponseHeaderDTO;
import g2pc.core.lib.dto.search.message.request.ResponseMessageDTO;
import g2pc.core.lib.dto.status.message.response.StatusResponseMessageDTO;
import g2pc.core.lib.exceptions.G2pcError;
import g2pc.core.lib.exceptions.G2pcValidationException;
import g2pc.core.lib.utils.CommonUtils;
import g2pc.dc.core.lib.constants.DcConstants;
import g2pc.dc.core.lib.service.ResponseHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.apache.commons.codec.Resources.getInputStream;

/**
 * The type Response handler service.
 */
@Service
@Slf4j
public class ResponseHandlerServiceImpl implements ResponseHandlerService {



    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     *
     * @param cacheKey the cache key to update data
     * @throws JsonProcessingException exception might be thrown
     */
    @Override
    public void updateCache(String cacheKey) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        CacheDTO cacheDTO = objectMapper.readerFor(CacheDTO.class).readValue(redisTemplate.opsForValue().get(cacheKey));

        cacheDTO.setStatus(DcConstants.COMPLETED);
        cacheDTO.setLastUpdatedDate(CommonUtils.getCurrentTimeStamp());

        ValueOperations<String, String> val = redisTemplate.opsForValue();
        val.set(cacheKey, new ObjectMapper().writeValueAsString(cacheDTO));
    }



}
