package g2pc.dc.core.lib.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import g2pc.core.lib.dto.common.cache.CacheDTO;
import g2pc.core.lib.dto.common.header.ResponseHeaderDTO;
import g2pc.core.lib.dto.common.message.response.ResponseMessageDTO;
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

/**
 * The type Response handler service.
 */
@Service
@Slf4j
public class ResponseHandlerServiceImpl implements ResponseHandlerService {

    @Autowired
    private CommonUtils commonUtils;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public void updateCache(String cacheKey) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        CacheDTO cacheDTO = objectMapper.readerFor(CacheDTO.class).readValue(redisTemplate.opsForValue().get(cacheKey));

        cacheDTO.setStatus(DcConstants.COMPLETED);
        cacheDTO.setLastUpdatedDate(CommonUtils.getCurrentTimeStamp());

        ValueOperations<String, String> val = redisTemplate.opsForValue();
        val.set(cacheKey, new ObjectMapper().writeValueAsString(cacheDTO));
    }

    @Override
    public void validateResponseHeader(ResponseHeaderDTO responseHeaderDTO) throws G2pcValidationException, JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        String headerInfoString = new ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(responseHeaderDTO);
        InputStream schemaStream = commonUtils.getResponseHeaderString();
        JsonNode jsonNodeMessage = objectMapper.readTree(headerInfoString);
        JsonSchema schemaMessage = null;
        if(schemaStream !=null){
            schemaMessage  = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4).
                    getSchema(schemaStream);
        }
        Set<ValidationMessage> errorMessage = schemaMessage.validate(jsonNodeMessage);
        List<G2pcError> errorCombinedMessage= new ArrayList<>();
        for (ValidationMessage error : errorMessage){
            log.info("Validation errors" + error );
            errorCombinedMessage.add(new G2pcError("",error.getMessage()));

        }
        if (errorMessage.size()>0){
            throw new G2pcValidationException(errorCombinedMessage);
        }

    }

    @Override
    public void validateResponseMessage(ResponseMessageDTO messageDTO) throws G2pcValidationException, JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        String messageString = new ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(messageDTO);
        log.info("MessageString -> " + messageString);
        InputStream schemaStream = commonUtils.getResponseMessageString();
        JsonNode jsonNodeMessage = objectMapper.readTree(messageString);
        JsonSchema schemaMessage = null;
        if(schemaStream !=null){
            schemaMessage  = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4).
                    getSchema(schemaStream);
        }
        Set<ValidationMessage> errorMessage = schemaMessage.validate(jsonNodeMessage);
        List<G2pcError> errorCombinedMessage= new ArrayList<>();
        for (ValidationMessage error : errorMessage){
            log.info("Validation errors" + error );
            errorCombinedMessage.add(new G2pcError("",error.getMessage()));

        }
        if (errorMessage.size()>0){
            throw new G2pcValidationException(errorCombinedMessage);
        }

    }
}
