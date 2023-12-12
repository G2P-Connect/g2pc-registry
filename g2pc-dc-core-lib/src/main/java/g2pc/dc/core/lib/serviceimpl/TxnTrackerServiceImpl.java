package g2pc.dc.core.lib.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.dto.common.cache.CacheDTO;
import g2pc.core.lib.dto.common.header.HeaderDTO;
import g2pc.core.lib.dto.common.header.RequestHeaderDTO;
import g2pc.core.lib.dto.common.header.ResponseHeaderDTO;
import g2pc.core.lib.dto.common.message.request.RequestDTO;
import g2pc.core.lib.dto.common.message.request.RequestMessageDTO;
import g2pc.core.lib.dto.common.message.request.SearchRequestDTO;
import g2pc.core.lib.dto.common.message.response.ResponseDTO;
import g2pc.core.lib.dto.common.message.response.ResponseMessageDTO;
import g2pc.core.lib.dto.common.message.response.SearchResponseDTO;
import g2pc.core.lib.enums.HeaderStatusENUM;
import g2pc.core.lib.utils.CommonUtils;
import g2pc.dc.core.lib.entity.ResponseDataEntity;
import g2pc.dc.core.lib.entity.ResponseTrackerEntity;
import g2pc.dc.core.lib.repository.ResponseDataRepository;
import g2pc.dc.core.lib.repository.ResponseTrackerRepository;
import g2pc.dc.core.lib.service.TxnTrackerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class TxnTrackerServiceImpl implements TxnTrackerService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    ResponseTrackerRepository responseTrackerRepository;

    @Autowired
    private ResponseDataRepository responseDataRepository;

    @Autowired
    private ResponseHandlerServiceImpl responseHandlerService;

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

    @Override
    public  void saveRequestInDB(String requestString, String regType) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerSubtypes(RequestHeaderDTO.class, ResponseHeaderDTO.class, HeaderDTO.class);

        RequestDTO requestDTO = objectMapper.readValue(requestString, RequestDTO.class);
        HeaderDTO headerDTO = requestDTO.getHeader();
        RequestMessageDTO messageDTO = objectMapper.convertValue(requestDTO.getMessage(), RequestMessageDTO.class);
        String transactionId = messageDTO.getTransactionId();

        Optional<ResponseTrackerEntity> responseTrackerEntityOptional = responseTrackerRepository.findByTransactionId(transactionId);
        if (responseTrackerEntityOptional.isEmpty()) {
            ResponseTrackerEntity responseTrackerEntity = new ResponseTrackerEntity();
            responseTrackerEntity.setVersion(headerDTO.getVersion());
            responseTrackerEntity.setMessageId(headerDTO.getMessageId());
            responseTrackerEntity.setMessageTs(headerDTO.getMessageTs());
            responseTrackerEntity.setAction(headerDTO.getAction());
            responseTrackerEntity.setSenderId(headerDTO.getSenderId());
            responseTrackerEntity.setReceiverId(headerDTO.getReceiverId());
            responseTrackerEntity.setIsMsgEncrypted(headerDTO.getIsMsgEncrypted());
            responseTrackerEntity.setTransactionId(transactionId);
            responseTrackerEntity.setRegistryType(regType);

            List<SearchRequestDTO> searchRequestDTOList = messageDTO.getSearchRequest();
            for (SearchRequestDTO searchRequestDTO : searchRequestDTOList) {
                ResponseDataEntity responseDataEntity = new ResponseDataEntity();
                responseDataEntity.setReferenceId(searchRequestDTO.getReferenceId());
                responseDataEntity.setTimestamp(searchRequestDTO.getTimestamp());
                responseDataEntity.setVersion(searchRequestDTO.getSearchCriteria().getVersion());
                responseDataEntity.setRegType(searchRequestDTO.getSearchCriteria().getRegType());
                responseDataEntity.setRegSubType(searchRequestDTO.getSearchCriteria().getRegSubType());
                responseDataEntity.setStatus(HeaderStatusENUM.PDNG.toValue());
                responseDataEntity.setResponseTrackerEntity(responseTrackerEntity);
                responseTrackerEntity.getResponseDataEntityList().add(responseDataEntity);
            }
            responseTrackerRepository.save(responseTrackerEntity);
        }
    }

    @Override
    public  void updateTransactionDbAndCache(ResponseDTO responseDTO) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerSubtypes(ResponseHeaderDTO.class, HeaderDTO.class);

        ResponseHeaderDTO headerDTO = objectMapper.convertValue(responseDTO.getHeader(), ResponseHeaderDTO.class);
        ResponseMessageDTO messageDTO = objectMapper.convertValue(responseDTO.getMessage(), ResponseMessageDTO.class);
        String transactionId = messageDTO.getTransactionId();
        List<SearchResponseDTO> searchResponseDTOList = messageDTO.getSearchResponse();

        Optional<ResponseTrackerEntity> responseTrackerEntityOptional = responseTrackerRepository.findByTransactionId(transactionId);
        if (responseTrackerEntityOptional.isPresent()) {
            ResponseTrackerEntity responseTrackerEntity = responseTrackerEntityOptional.get();
            String cacheKey = responseTrackerEntity.getRegistryType() + "-" + transactionId;

            for (SearchResponseDTO searchResponseDTO : searchResponseDTOList) {
                Optional<ResponseDataEntity> entityOptional = responseDataRepository.findByReferenceId(searchResponseDTO.getReferenceId());
                if (entityOptional.isPresent()) {
                    ResponseDataEntity responseDataEntity = entityOptional.get();
                    responseDataEntity.setStatus(searchResponseDTO.getStatus());
                    responseDataEntity.setStatusReasonCode(searchResponseDTO.getStatusReasonCode());
                    responseDataEntity.setStatusReasonMessage(searchResponseDTO.getStatusReasonMessage());
                    responseDataEntity.setRegSubType(searchResponseDTO.getData().getRegSubType());
                    responseDataEntity.setRegRecordType(searchResponseDTO.getData().getRegRecordType());
                    //TODO: check for object conversion method
                    responseDataEntity.setRegRecords(objectMapper.writeValueAsString(searchResponseDTO.getData().getRegRecords()));
                    responseDataEntity.setLastUpdatedDate(new Timestamp(System.currentTimeMillis()));
                    responseDataRepository.save(responseDataEntity);
                }
            }
            responseTrackerEntity.setStatus(headerDTO.getStatus());
            responseTrackerEntity.setStatusReasonCode(headerDTO.getStatusReasonCode());
            responseTrackerEntity.setStatusReasonMessage(headerDTO.getStatusReasonMessage());
            responseTrackerEntity.setTotalCount(headerDTO.getTotalCount());
            responseTrackerEntity.setCompletedCount(headerDTO.getCompletedCount());
            responseTrackerEntity.setCorrelationId(messageDTO.getCorrelationId());
            responseTrackerEntity.setMeta(objectMapper.writeValueAsString(headerDTO.getMeta()));
            responseTrackerEntity.setLastUpdatedDate(new Timestamp(System.currentTimeMillis()));
            responseTrackerRepository.save(responseTrackerEntity);

            responseHandlerService.updateCache(cacheKey);
        }
    }
}
