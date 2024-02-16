package g2pc.dc.core.lib.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.dto.common.cache.CacheDTO;
import g2pc.core.lib.dto.common.header.HeaderDTO;
import g2pc.core.lib.dto.common.header.RequestHeaderDTO;
import g2pc.core.lib.dto.common.header.ResponseHeaderDTO;
import g2pc.core.lib.dto.search.message.request.RequestDTO;
import g2pc.core.lib.dto.search.message.request.RequestMessageDTO;
import g2pc.core.lib.dto.search.message.request.ResponseMessageDTO;
import g2pc.core.lib.dto.search.message.request.SearchRequestDTO;
import g2pc.core.lib.dto.search.message.response.ResponseDTO;
import g2pc.core.lib.dto.search.message.response.SearchResponseDTO;
import g2pc.core.lib.dto.status.message.request.StatusRequestDTO;
import g2pc.core.lib.dto.status.message.request.StatusRequestMessageDTO;
import g2pc.core.lib.dto.status.message.request.TxnStatusRequestDTO;
import g2pc.core.lib.dto.status.message.response.StatusResponseDTO;
import g2pc.core.lib.dto.status.message.response.StatusResponseMessageDTO;
import g2pc.core.lib.dto.status.message.response.TxnStatusResponseDTO;
import g2pc.core.lib.enums.ExceptionsENUM;
import g2pc.core.lib.enums.HeaderStatusENUM;
import g2pc.core.lib.exceptions.G2pcError;
import g2pc.core.lib.service.ElasticsearchService;
import g2pc.core.lib.utils.CommonUtils;
import g2pc.dc.core.lib.constants.DcConstants;
import g2pc.dc.core.lib.dto.ResponseDataDto;
import g2pc.dc.core.lib.dto.ResponseTrackerDto;
import g2pc.dc.core.lib.entity.ResponseDataEntity;
import g2pc.dc.core.lib.entity.ResponseTrackerEntity;
import g2pc.dc.core.lib.repository.ResponseDataRepository;
import g2pc.dc.core.lib.repository.ResponseTrackerRepository;
import g2pc.dc.core.lib.service.TxnTrackerService;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class TxnTrackerServiceImpl implements TxnTrackerService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ResponseTrackerRepository responseTrackerRepository;

    @Autowired
    private ResponseDataRepository responseDataRepository;

    @Autowired
    private ResponseHandlerServiceImpl responseHandlerService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Value("${sunbird.api_urls.response_data_api}")
    private String responseDataURL;

    @Value("${sunbird.api_urls.response_tracker_api}")
    private String responseTrackerURL;

    /**
     * Save initial payload to Redis and create a new transaction in DB table
     *
     * @param payloadMapList payloadMap list to save in initial transaction
     * @param transactionId  transactionId to store in initial transaction
     * @param status         status to store in initial transaction
     */
    @Override
    public void saveInitialTransaction(List<Map<String, Object>> payloadMapList, String transactionId, String status, String protocol) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        CacheDTO cacheDTO = createCache(objectMapper.writeValueAsString(payloadMapList), HeaderStatusENUM.PDNG.toValue(), protocol);
        saveCache(cacheDTO, "initial-" + transactionId);
    }

    /**
     * Save request transactions to Redis
     *
     * @param requestString string to store in cache
     * @param regType       regType to store in cache
     * @param transactionId transactionId to store cache
     */
    @Override
    public void saveRequestTransaction(String requestString, String regType, String transactionId, String protocol) throws JsonProcessingException {
        CacheDTO cacheDTO = createCache(requestString, HeaderStatusENUM.PDNG.toValue(), protocol);
        saveCache(cacheDTO, regType + "-" + transactionId);
    }

    /**
     * Create cache to save in Redis
     *
     * @param data   data to store in cache
     * @param status status to store in cache
     * @return CacheDTO
     */
    @Override
    public CacheDTO createCache(String data, String status, String protocol) {
        log.info("Save requests in cache with status pending");
        CacheDTO cacheDTO = new CacheDTO();
        cacheDTO.setData(data);
        cacheDTO.setStatus(status);
        cacheDTO.setProtocol(protocol);
        cacheDTO.setCreatedDate(CommonUtils.getCurrentTimeStamp());
        cacheDTO.setLastUpdatedDate(CommonUtils.getCurrentTimeStamp());
        return cacheDTO;
    }

    /**
     * Save cache in Redis
     *
     * @param cacheDTO cacheDTO
     * @param cacheKey cacheKey key for which data stored
     */
    @Override
    public void saveCache(CacheDTO cacheDTO, String cacheKey) throws JsonProcessingException {
        ValueOperations<String, String> val = redisTemplate.opsForValue();
        val.set(cacheKey, new ObjectMapper().writeValueAsString(cacheDTO));
    }

    /**
     * @param requestString request string to convert it in statusRequestDto
     * @param regType       regType to store in db
     * @param protocol      protocol to save in db
     * @throws JsonProcessingException
     */
    @Override
    public G2pcError saveRequestInDB(String requestString, String regType, String protocol,
                                     G2pcError g2pcError, String payloadFilename,
                                     String inboundFilename, Boolean sunbirdEnabled) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerSubtypes(RequestHeaderDTO.class, ResponseHeaderDTO.class, HeaderDTO.class);

        RequestDTO requestDTO = objectMapper.readValue(requestString, RequestDTO.class);
        HeaderDTO headerDTO = requestDTO.getHeader();
        RequestMessageDTO messageDTO = objectMapper.convertValue(requestDTO.getMessage(), RequestMessageDTO.class);
        String transactionId = messageDTO.getTransactionId();
        g2pcError = new G2pcError(HttpStatus.OK.toString(), "Successfully stored in db");
        if (Boolean.TRUE.equals(sunbirdEnabled)) {
            Map<String, String> fieldValues = new HashMap<>();
            fieldValues.put("transaction_id.keyword", transactionId);
            SearchResponse response = elasticsearchService.exactSearch("response_tracker", fieldValues);
            if (response.getHits().getHits().length > 0) {
                log.info("response: {}", response.getHits().getHits()[0].getSourceAsString());
            } else {
                ResponseTrackerDto responseTrackerDto = new ResponseTrackerDto();
                responseTrackerDto.setVersion(headerDTO.getVersion());
                responseTrackerDto.setMessageId(headerDTO.getMessageId());
                responseTrackerDto.setMessageTs(headerDTO.getMessageTs());
                responseTrackerDto.setAction(headerDTO.getAction());
                responseTrackerDto.setSenderId(headerDTO.getSenderId());
                responseTrackerDto.setReceiverId(headerDTO.getReceiverId());
                responseTrackerDto.setIsMsgEncrypted(headerDTO.getIsMsgEncrypted());
                responseTrackerDto.setTransactionId(transactionId);
                responseTrackerDto.setRegistryType(regType);
                responseTrackerDto.setProtocol(protocol);
                responseTrackerDto.setPayloadFilename(payloadFilename);
                responseTrackerDto.setInboundFilename(inboundFilename);
                List<SearchRequestDTO> searchRequestDTOList = messageDTO.getSearchRequest();
                for (SearchRequestDTO searchRequestDTO : searchRequestDTOList) {
                    ResponseDataDto responseDataDto = new ResponseDataDto();
                    responseDataDto.setRegistryTransactionsId(transactionId);
                    responseDataDto.setReferenceId(searchRequestDTO.getReferenceId());
                    responseDataDto.setTimestamp(searchRequestDTO.getTimestamp());
                    responseDataDto.setVersion(searchRequestDTO.getSearchCriteria().getVersion());
                    responseDataDto.setRegType(searchRequestDTO.getSearchCriteria().getRegType());
                    responseDataDto.setRegSubType(searchRequestDTO.getSearchCriteria().getRegSubType());
                    responseDataDto.setStatus(HeaderStatusENUM.PDNG.toValue());
                    responseDataDto.setStatusReasonCode(g2pcError.getCode());
                    String responseDataString = objectMapper.writeValueAsString(responseDataDto);
                    HttpResponse<JsonNode> responseData = Unirest.post(responseDataURL)
                            .header("Content-Type", "application/json")
                            .body(responseDataString)
                            .asJson();
                    log.info("ResponseData entity response-> " + responseData);
                    if (responseData.getStatus() != 200) {
                        g2pcError = new G2pcError(ExceptionsENUM.ERROR_SERVICE_UNAVAILABLE.toValue(), responseData.getBody().toString());
                    }
                }
                String responseTrackerString = objectMapper.writeValueAsString(responseTrackerDto);
                HttpResponse<JsonNode> responseT = Unirest.post(responseTrackerURL)
                        .header("Content-Type", "application/json")
                        .body(responseTrackerString)
                        .asJson();
                if (responseT.getStatus() != 200) {
                    g2pcError = new G2pcError(ExceptionsENUM.ERROR_SERVICE_UNAVAILABLE.toValue(), responseT.getBody().toString());
                }
                log.info("ResponseTracker entity response-> " + response);
            }
        } else {
            try {
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
                    responseTrackerEntity.setProtocol(protocol);
                    responseTrackerEntity.setPayloadFilename(payloadFilename);
                    responseTrackerEntity.setInboundFilename(inboundFilename);
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
                        responseDataEntity.setStatusReasonCode(g2pcError.getCode());
                        responseTrackerEntity.getResponseDataEntityList().add(responseDataEntity);
                    }
                    responseTrackerRepository.save(responseTrackerEntity);
                }
            } catch (Exception e) {
                g2pcError = new G2pcError(ExceptionsENUM.ERROR_SERVICE_UNAVAILABLE.toValue(), e.getMessage());
            }
        }
        return g2pcError;
    }

    /**
     * @param responseDTO responseDTO
     * @throws JsonProcessingException jsonProcessingException might be thrown
     */
    @Override
    public G2pcError updateTransactionDbAndCache(ResponseDTO responseDTO, String outboundFilename, Boolean sunbirdEnabled) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerSubtypes(ResponseHeaderDTO.class, HeaderDTO.class);
        G2pcError g2pcError;
        ResponseHeaderDTO headerDTO = objectMapper.convertValue(responseDTO.getHeader(), ResponseHeaderDTO.class);
        ResponseMessageDTO messageDTO = objectMapper.convertValue(responseDTO.getMessage(), ResponseMessageDTO.class);
        String transactionId = messageDTO.getTransactionId();
        List<SearchResponseDTO> searchResponseDTOList = messageDTO.getSearchResponse();
        if (Boolean.TRUE.equals(sunbirdEnabled)) {
            Map<String, String> fieldValues = new HashMap<>();
            fieldValues.put("transaction_id.keyword", transactionId);
            SearchResponse responseTrackerSearchResponse = elasticsearchService.exactSearch("response_tracker", fieldValues);
            if (responseTrackerSearchResponse.getHits().getHits().length > 0) {
                log.info("response: {}", responseTrackerSearchResponse.getHits().getHits()[0].getSourceAsString());
                String responseTrackerDtoString = responseTrackerSearchResponse.getHits().getHits()[0].getSourceAsString();
                Map<String, Object> resultMap = objectMapper.readValue(responseTrackerDtoString, new TypeReference<Map<String, Object>>() {
                });

                String osid = resultMap.get(DcConstants.OSID).toString().substring(2);
                ResponseTrackerDto responseTrackerDto = objectMapper.readerFor(ResponseTrackerDto.class).
                        readValue(responseTrackerDtoString);
                String cacheKey = responseTrackerDto.getRegistryType() + "-" + transactionId;
                for (SearchResponseDTO searchResponseDTO : searchResponseDTOList) {
                    Map<String, String> dataFieldValues = new HashMap<>();
                    dataFieldValues.put("reference_id.keyword", searchResponseDTO.getReferenceId());
                    SearchResponse responseDataSearchResponse = elasticsearchService.exactSearch("response_data", dataFieldValues);
                    if (responseDataSearchResponse.getHits().getHits().length > 0) {
                        String responseDataDtoString = responseDataSearchResponse.getHits().getHits()[0].getSourceAsString();
                        Map<String, Object> responseDataResultMap = objectMapper.readValue(responseDataDtoString, new TypeReference<Map<String, Object>>() {
                        });
                        String responseDataOsId = responseDataResultMap.get(DcConstants.OSID).toString().substring(2);
                        ResponseDataDto responseDataDto = objectMapper.readerFor(ResponseDataDto.class).
                                readValue(responseDataDtoString);
                        responseDataDto.setStatus(searchResponseDTO.getStatus());
                        responseDataDto.setStatusReasonCode(searchResponseDTO.getStatusReasonCode());
                        responseDataDto.setStatusReasonMessage(searchResponseDTO.getStatusReasonMessage());
                        responseDataDto.setRegSubType(searchResponseDTO.getData().getRegSubType());
                        responseDataDto.setRegRecordType(searchResponseDTO.getData().getRegRecordType());
                        responseDataDto.setRegRecords(objectMapper.writeValueAsString(searchResponseDTO.getData().getRegRecords()));
                        responseDataDto.setLastUpdatedDate(CommonUtils.getCurrentTimeStamp());
                        String responseDataString = objectMapper.writeValueAsString(responseDataDto);
                        HttpResponse<JsonNode> responseD = Unirest.put(responseDataURL + "/" + responseDataOsId)
                                .header("Content-Type", "application/json")
                                .body(responseDataString)
                                .asJson();
                        if (responseD.getStatus() != 200) {
                            g2pcError = new G2pcError(ExceptionsENUM.ERROR_SERVICE_UNAVAILABLE.toValue(), responseD.getBody().toString());
                            return g2pcError;
                        }
                    }
                }
                responseTrackerDto.setStatus(headerDTO.getStatus());
                responseTrackerDto.setStatusReasonCode(headerDTO.getStatusReasonCode());
                responseTrackerDto.setStatusReasonMessage(headerDTO.getStatusReasonMessage());
                responseTrackerDto.setTotalCount(headerDTO.getTotalCount());
                responseTrackerDto.setCompletedCount(headerDTO.getCompletedCount());
                responseTrackerDto.setCorrelationId(messageDTO.getCorrelationId());
                responseTrackerDto.setMeta(objectMapper.writeValueAsString(headerDTO.getMeta()));
                responseTrackerDto.setLastUpdatedDate(CommonUtils.getCurrentTimeStamp());
                responseTrackerDto.setOutboundFilename(outboundFilename);
                String responseTrackerString = objectMapper.writeValueAsString(responseTrackerDto);
                HttpResponse<JsonNode> responseT = Unirest.put(responseTrackerURL + "/" + osid)
                        .header("Content-Type", "application/json")
                        .body(responseTrackerString)
                        .asJson();
                if (responseT.getStatus() != 200) {
                    g2pcError = new G2pcError(ExceptionsENUM.ERROR_SERVICE_UNAVAILABLE.toValue(), responseT.getBody().toString());
                    return g2pcError;
                }
                responseHandlerService.updateCache(cacheKey);
            }
        } else {
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
                responseTrackerEntity.setOutboundFilename(outboundFilename);
                responseTrackerRepository.save(responseTrackerEntity);
                responseHandlerService.updateCache(cacheKey);
            }
        }
        return new G2pcError(HttpStatus.OK.toString(), "Successfully stored in db");
    }

    /**
     * @param txnType       transaction type to store in cache
     * @param transactionId to store in cache
     * @param status        status transactionId to store in cache
     * @param protocol      protocol to store in cache
     * @throws JsonProcessingException jsonProcessingException might be thrown
     */
    @Override
    public void saveInitialStatusTransaction(String txnType, String transactionId, String status, String protocol) throws JsonProcessingException {
        CacheDTO cacheDTO = createCache(txnType, HeaderStatusENUM.PDNG.toValue(), protocol);
        saveCache(cacheDTO, "initial-" + transactionId);

    }

    /**
     * @param requestString request string to convert it in statusRequestDto
     * @param regType       regType to store in db
     * @throws JsonProcessingException jsonProcessingException might be thrown
     */
    @Override
    public G2pcError saveRequestInStatusDB(String requestString, String regType) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerSubtypes(RequestHeaderDTO.class, ResponseHeaderDTO.class, HeaderDTO.class);

        StatusRequestDTO statusRequestDTO = objectMapper.readValue(requestString, StatusRequestDTO.class);
        HeaderDTO headerDTO = statusRequestDTO.getHeader();
        StatusRequestMessageDTO statusRequestMessageDTO = objectMapper.convertValue(statusRequestDTO.getMessage(), StatusRequestMessageDTO.class);
        String transactionId = statusRequestMessageDTO.getTransactionId();
        G2pcError g2pcError = new G2pcError(HttpStatus.OK.toString(), "Successfully stored in db");
        Map<String, String> fieldValues = new HashMap<>();
        fieldValues.put("transaction_id.keyword", transactionId);
        SearchResponse response = elasticsearchService.exactSearch("response_tracker", fieldValues);
        if (response.getHits().getHits().length > 0) {
            g2pcError = new G2pcError(HttpStatus.OK.toString(), "Data is already present in the db for transaction id" + transactionId);
            log.info("response: {}", response.getHits().getHits()[0].getSourceAsString());
        } else {
            ResponseTrackerDto responseTrackerDto = new ResponseTrackerDto();
            responseTrackerDto.setVersion(headerDTO.getVersion());
            responseTrackerDto.setMessageId(headerDTO.getMessageId());
            responseTrackerDto.setMessageTs(headerDTO.getMessageTs());
            responseTrackerDto.setAction(headerDTO.getAction());
            responseTrackerDto.setSenderId(headerDTO.getSenderId());
            responseTrackerDto.setReceiverId(headerDTO.getReceiverId());
            responseTrackerDto.setIsMsgEncrypted(headerDTO.getIsMsgEncrypted());
            responseTrackerDto.setTransactionId(transactionId);
            responseTrackerDto.setRegistryType(regType);

            TxnStatusRequestDTO txnStatusRequestDTO = statusRequestMessageDTO.getTxnStatusRequest();
            ResponseDataDto responseDataDto = new ResponseDataDto();
            responseDataDto.setTimestamp("");
            responseDataDto.setVersion("");
            responseDataDto.setRegType("");
            responseDataDto.setRegSubType("");
            responseDataDto.setTxnType(txnStatusRequestDTO.getTxnType());
            responseDataDto.setAttributeType(txnStatusRequestDTO.getAttributeType());
            responseDataDto.setAttributeValue((String) txnStatusRequestDTO.getAttributeValue());
            responseDataDto.setStatus(HeaderStatusENUM.PDNG.toValue());

            String responseDataString = objectMapper.writeValueAsString(responseDataDto);
            HttpResponse<JsonNode> responseD = Unirest.post(responseDataURL)
                    .header("Content-Type", "application/json")
                    .body(responseDataString)
                    .asJson();
            if (responseD.getStatus() != 200) {
                g2pcError = new G2pcError(ExceptionsENUM.ERROR_SERVICE_UNAVAILABLE.toValue(), responseD.getBody().toString());
                return g2pcError;
            }
            String responseTrackerString = objectMapper.writeValueAsString(responseTrackerDto);
            HttpResponse<JsonNode> responseT = Unirest.post(responseTrackerURL)
                    .header("Content-Type", "application/json")
                    .body(responseTrackerString)
                    .asJson();
            log.info("ResponseTracker entity response-> " + responseT);
            if (responseT.getStatus() != 200) {
                g2pcError = new G2pcError(ExceptionsENUM.ERROR_SERVICE_UNAVAILABLE.toValue(), responseT.getBody().toString());
                return g2pcError;
            }
        }
        return g2pcError;
    }

    /**
     * @param statusResponseDTO statusResponseDTO used to update transaction db
     * @throws JsonProcessingException jsonProcessingException might be thrown
     */
    @Override
    public G2pcError updateStatusTransactionDbAndCache(StatusResponseDTO statusResponseDTO) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerSubtypes(ResponseHeaderDTO.class, HeaderDTO.class);
        G2pcError g2pcError;
        ResponseHeaderDTO headerDTO = objectMapper.convertValue(statusResponseDTO.getHeader(), ResponseHeaderDTO.class);
        StatusResponseMessageDTO statusResponseMessageDTO = objectMapper.convertValue(statusResponseDTO.getMessage(), StatusResponseMessageDTO.class);
        String transactionId = statusResponseMessageDTO.getTransactionId();
        TxnStatusResponseDTO txnStatusResponseDTO = statusResponseMessageDTO.getTxnStatusResponse();
        Map<String, String> fieldValues = new HashMap<>();
        fieldValues.put("transaction_id.keyword", transactionId);
        SearchResponse responseTrackerSearchResponse = elasticsearchService.exactSearch("response_tracker", fieldValues);
        if (responseTrackerSearchResponse.getHits().getHits().length > 0) {
            log.info("response: {}", responseTrackerSearchResponse.getHits().getHits()[0].getSourceAsString());
            String responseTrackerDtoString = responseTrackerSearchResponse.getHits().getHits()[0].getSourceAsString();
            Map<String, Object> resultMap = objectMapper.readValue(responseTrackerDtoString, new TypeReference<Map<String, Object>>() {
            });

            String osid = resultMap.get(DcConstants.OSID).toString().substring(2);
            ResponseTrackerDto responseTrackerDto = objectMapper.readerFor(ResponseTrackerDto.class).
                    readValue(responseTrackerDtoString);
            String cacheKey = responseTrackerDto.getRegistryType() + "-" + transactionId;
            ResponseMessageDTO responseMessageDTO = objectMapper.convertValue(txnStatusResponseDTO.getTxnStatus(), ResponseMessageDTO.class);
            for (SearchResponseDTO searchResponseDTO : responseMessageDTO.getSearchResponse()) {
                Map<String, String> dataFieldValues = new HashMap<>();
                dataFieldValues.put("reference_id.keyword", searchResponseDTO.getReferenceId());
                SearchResponse responseDataSearchResponse = elasticsearchService.exactSearch("response_data", dataFieldValues);
                if (responseDataSearchResponse.getHits().getHits().length > 0) {
                    String responseDataDtoString = responseDataSearchResponse.getHits().getHits()[0].getSourceAsString();
                    Map<String, Object> responseDataResultMap = objectMapper.readValue(responseDataDtoString, new TypeReference<Map<String, Object>>() {
                    });
                    String responseDataOsid = responseDataResultMap.get(DcConstants.OSID).toString().substring(2);
                    ResponseDataDto responseDataDto = objectMapper.readerFor(ResponseDataDto.class).
                            readValue(responseDataDtoString);

                    responseDataDto.setStatus(searchResponseDTO.getStatus());
                    responseDataDto.setStatusReasonCode(searchResponseDTO.getStatusReasonCode());
                    responseDataDto.setStatusReasonMessage(searchResponseDTO.getStatusReasonMessage());
                    responseDataDto.setRegSubType(searchResponseDTO.getData().getRegSubType());
                    responseDataDto.setRegRecordType(searchResponseDTO.getData().getRegRecordType());
                    responseDataDto.setRegRecords(objectMapper.writeValueAsString(searchResponseDTO.getData().getRegRecords()));
                    responseDataDto.setLastUpdatedDate(CommonUtils.getCurrentTimeStamp());
                    String responseDataString = objectMapper.writeValueAsString(responseDataDto);
                    HttpResponse<JsonNode> responseD = Unirest.put(responseDataURL + "/" + responseDataOsid)
                            .header("Content-Type", "application/json")
                            .body(responseDataString)
                            .asJson();
                    if (responseD.getStatus() != 200) {
                        g2pcError = new G2pcError(ExceptionsENUM.ERROR_SERVICE_UNAVAILABLE.toValue(), responseD.getBody().toString());
                        return g2pcError;
                    }
                }
            }
            responseTrackerDto.setStatus(headerDTO.getStatus());
            responseTrackerDto.setStatusReasonCode(headerDTO.getStatusReasonCode());
            responseTrackerDto.setStatusReasonMessage(headerDTO.getStatusReasonMessage());
            responseTrackerDto.setTotalCount(headerDTO.getTotalCount());
            responseTrackerDto.setCompletedCount(headerDTO.getCompletedCount());
            responseTrackerDto.setCorrelationId(statusResponseMessageDTO.getCorrelationId());
            responseTrackerDto.setMeta(objectMapper.writeValueAsString(headerDTO.getMeta()));
            responseTrackerDto.setLastUpdatedDate(CommonUtils.getCurrentTimeStamp());
            String responseTrackerString = objectMapper.writeValueAsString(responseTrackerDto);
            HttpResponse<JsonNode> responseT = Unirest.put(responseTrackerURL + "/" + osid)
                    .header("Content-Type", "application/json")
                    .body(responseTrackerString)
                    .asJson();
            if (responseT.getStatus() != 200) {
                g2pcError = new G2pcError(ExceptionsENUM.ERROR_SERVICE_UNAVAILABLE.toValue(), responseT.getBody().toString());
                return g2pcError;
            }
            responseHandlerService.updateCache(cacheKey);
        }
        return new G2pcError(HttpStatus.OK.toString(), "Successfully stored in db");
    }
}
