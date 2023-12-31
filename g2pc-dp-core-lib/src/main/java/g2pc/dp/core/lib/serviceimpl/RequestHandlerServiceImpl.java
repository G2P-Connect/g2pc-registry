package g2pc.dp.core.lib.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import g2pc.core.lib.dto.common.AcknowledgementDTO;
import g2pc.core.lib.dto.common.cache.CacheDTO;
import g2pc.core.lib.dto.common.header.RequestHeaderDTO;
import g2pc.core.lib.dto.common.header.ResponseHeaderDTO;
import g2pc.core.lib.dto.common.message.request.RequestDTO;
import g2pc.core.lib.dto.common.message.request.RequestMessageDTO;
import g2pc.core.lib.enums.HeaderStatusENUM;
import g2pc.core.lib.exceptions.G2pcValidationException;
import g2pc.core.lib.utils.CommonUtils;
import g2pc.dp.core.lib.constants.DpConstants;
import g2pc.dp.core.lib.service.RequestHandlerService;
import g2pc.dp.core.lib.service.TxnTrackerDbService;
import g2pc.dp.core.lib.service.TxnTrackerRedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import g2pc.core.lib.exceptions.G2pcError;

import java.io.InputStream;
import java.util.*;

/**
 * The type Request handler service.
 */
@Service
@Slf4j
public class RequestHandlerServiceImpl implements RequestHandlerService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private CommonUtils commonUtils;

    @Autowired
    private TxnTrackerRedisService txnTrackerRedisService;

    @Autowired
    private TxnTrackerDbService txnTrackerDbService;

    /**
     * Build a request to save in Redis cache
     *
     * @param requestData required
     * @param cacheKey    required
     * @return Acknowledgement
     */
    @Override
    public AcknowledgementDTO buildCacheRequest(String requestData, String cacheKey) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerSubtypes(RequestHeaderDTO.class, ResponseHeaderDTO.class);
        log.info("Request saved in cache with status pending");
        CacheDTO cacheDTO = new CacheDTO();
        cacheDTO.setData(requestData);
        cacheDTO.setStatus(HeaderStatusENUM.PDNG.toValue());
        cacheDTO.setCreatedDate(CommonUtils.getCurrentTimeStamp());
        cacheDTO.setLastUpdatedDate(CommonUtils.getCurrentTimeStamp());

        RequestDTO requestDTO = objectMapper.readerFor(RequestDTO.class).readValue(cacheDTO.getData());

        txnTrackerRedisService.saveRequestDetails(cacheDTO, cacheKey);
        txnTrackerDbService.saveRequestDetails(requestDTO);

        AcknowledgementDTO acknowledgementDTO = new AcknowledgementDTO();
        acknowledgementDTO.setMessage(DpConstants.SEARCH_REQUEST_RECEIVED);
        acknowledgementDTO.setStatus(HeaderStatusENUM.RCVD.toValue());

        return acknowledgementDTO;
    }

    /**
     * The Object mapper.
     */
    @Override
    public void validateRequestHeader(RequestHeaderDTO headerDTO) throws G2pcValidationException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerSubtypes(RequestHeaderDTO.class, ResponseHeaderDTO.class);
        String headerInfoString = new ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(headerDTO);
        InputStream schemaStream = commonUtils.getRequestHeaderString();
        JsonNode jsonNodeMessage = objectMapper.readTree(headerInfoString);
        JsonSchema schemaMessage = null;
        if(schemaStream !=null){
            schemaMessage  = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4).
                    getSchema(schemaStream);
        }
        Set<ValidationMessage> errorMessage = schemaMessage.validate(jsonNodeMessage);
        List<G2pcError> errorcombinedMessage= new ArrayList<>();
        for (ValidationMessage error : errorMessage){
            log.info("Validation errors" + error );
            errorcombinedMessage.add(new G2pcError("",error.getMessage()));

        }
        if (errorMessage.size()>0){
            throw new G2pcValidationException(errorcombinedMessage);
        }
    }

    @Override
    public void validateRequestMessage(RequestMessageDTO messageDTO) throws G2pcValidationException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerSubtypes(RequestHeaderDTO.class, ResponseHeaderDTO.class);

        String messageString = objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(messageDTO);
        log.info("MessageString -> " + messageString);
        InputStream schemaStream = commonUtils.getRequestMessageString();
        JsonNode jsonNodeMessage = objectMapper.readTree(messageString);
        JsonSchema schemaMessage = null;
        if(schemaStream !=null){
            schemaMessage  = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4).
                    getSchema(schemaStream);
        }
        Set<ValidationMessage> errorMessage = schemaMessage.validate(jsonNodeMessage);
        List<G2pcError> errorcombinedMessage= new ArrayList<>();
        for (ValidationMessage error : errorMessage){
            log.info("Validation errors" + error );
            errorcombinedMessage.add(new G2pcError("",error.getMessage()));

        }
        if (errorMessage.size()>0){
            throw new G2pcValidationException(errorcombinedMessage);
        }
    }

}
