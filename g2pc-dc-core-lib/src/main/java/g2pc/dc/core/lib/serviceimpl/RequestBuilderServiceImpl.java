package g2pc.dc.core.lib.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.config.G2pUnirestHelper;
import g2pc.core.lib.constants.CoreConstants;
import g2pc.core.lib.constants.G2pSecurityConstants;
import g2pc.core.lib.constants.SftpConstants;
import g2pc.core.lib.dto.common.header.HeaderDTO;
import g2pc.core.lib.dto.common.header.MetaDTO;
import g2pc.core.lib.dto.common.header.RequestHeaderDTO;
import g2pc.core.lib.dto.common.header.ResponseHeaderDTO;
import g2pc.core.lib.dto.common.security.G2pTokenResponse;
import g2pc.core.lib.dto.common.security.TokenExpiryDto;
import g2pc.core.lib.dto.search.message.request.*;
import g2pc.core.lib.dto.sftp.SftpServerConfigDTO;
import g2pc.core.lib.dto.status.message.request.StatusRequestDTO;
import g2pc.core.lib.dto.status.message.request.StatusRequestMessageDTO;
import g2pc.core.lib.dto.status.message.request.TxnStatusRequestDTO;
import g2pc.core.lib.enums.*;
import g2pc.core.lib.exceptionhandler.ErrorResponse;
import g2pc.core.lib.exceptions.G2pcError;
import g2pc.core.lib.security.service.AsymmetricSignatureService;
import g2pc.core.lib.security.service.G2pEncryptDecrypt;
import g2pc.core.lib.security.service.G2pTokenService;
import g2pc.core.lib.security.service.G2pcUtilityClass;
import g2pc.core.lib.service.SftpHandlerService;
import g2pc.core.lib.utils.CommonUtils;
import g2pc.dc.core.lib.service.RequestBuilderService;
import kong.unirest.HttpResponse;
import kong.unirest.UnirestException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.text.ParseException;


@Service
public class RequestBuilderServiceImpl implements RequestBuilderService {

    private static final Logger log = LoggerFactory.getLogger(RequestBuilderServiceImpl.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    G2pUnirestHelper g2pUnirestHelper;

    @Autowired
    G2pEncryptDecrypt encryptDecrypt;

    @Autowired
    G2pTokenService g2pTokenService;

    @Autowired
    AsymmetricSignatureService asymmetricSignatureService;

    @Autowired
    private SftpHandlerService sftpHandlerService;

    @Autowired
    G2pcUtilityClass utility;


    /**
     * Create a query from payload
     *
     * @param payloadMapList required query params data
     * @param entrySet       required query params map to be matched in config
     * @return query map
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Map<String, Object>> createQueryMap(List<Map<String, Object>> payloadMapList, Set<Map.Entry<String, Object>> entrySet) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, Object>> queryList = new ArrayList<>();
        for (Map<String, Object> payloadMap : payloadMapList) {
            Map<String, Object> queryMap = new HashMap<>();
            for (Map.Entry<String, Object> entry : entrySet) {
                Map<String, Object> queryParamsMap = objectMapper.readValue(objectMapper.writeValueAsString(entry.getValue()), HashMap.class);

                queryParamsMap.forEach((key, value) -> {
                    if (payloadMap.containsKey(key)) {

                        queryParamsMap.put(key, payloadMap.get(key));
                    }
                });
                queryMap.put(entry.getKey(), queryParamsMap);
            }
            queryList.add(queryMap);
        }
        return queryList;
    }


    /**
     * Create a search criteria from query params
     *
     * @param queryParamsMap            required
     * @param registrySpecificConfigMap required
     * @return SearchCriteriaDTO
     */
    @Override
    public SearchCriteriaDTO getSearchCriteriaDTO(Map<String, Object> queryParamsMap, Map<String, Object> registrySpecificConfigMap) {
        RequestPaginationDTO paginationDTO = new RequestPaginationDTO(10, 1);
        ConsentDTO consentDTO = new ConsentDTO();
        AuthorizeDTO authorizeDTO = new AuthorizeDTO();
        List<SortDTO> sortDTOList = new ArrayList<>();
        SortDTO sortDTO = new SortDTO();
        sortDTO.setAttributeName(registrySpecificConfigMap.get("sort_attribute").toString());
        sortDTO.setSortOrder(registrySpecificConfigMap.get("sort_order").toString());
        sortDTOList.add(sortDTO);
        return utility.getSearchCriteriaDTO(queryParamsMap, registrySpecificConfigMap, sortDTOList, paginationDTO, consentDTO, authorizeDTO);

    }


    /**
     * Create message component of request
     *
     * @param searchCriteriaDTOList required
     * @return MessageDTO
     */
    @Override
    public RequestMessageDTO buildMessage(List<SearchCriteriaDTO> searchCriteriaDTOList) {
        List<SearchRequestDTO> searchRequestDTOList = new ArrayList<>();
        for (SearchCriteriaDTO searchCriteriaDTO : searchCriteriaDTOList) {
            SearchRequestDTO searchRequestDTO = new SearchRequestDTO();
            searchRequestDTO.setReferenceId(CommonUtils.generateUniqueId("R"));
            searchRequestDTO.setTimestamp(CommonUtils.getCurrentTimeStamp());
            searchRequestDTO.setLocale(LocalesENUM.EN.toValue());
            searchRequestDTO.setSearchCriteria(searchCriteriaDTO);
            searchRequestDTOList.add(searchRequestDTO);
        }
        RequestMessageDTO messageDTO = new RequestMessageDTO();
        messageDTO.setTransactionId(CommonUtils.generateUniqueId("T"));
        messageDTO.setSearchRequest(searchRequestDTOList);
        return messageDTO;
    }


    /**
     * Create header component of request
     * @param txnType txnType is used to identify transaction type for header creation
     * @return headerDto is returning.
     */
    @Override
    public HeaderDTO buildHeader(ActionsENUM txnType) {
        RequestHeaderDTO requestHeaderDTO = new RequestHeaderDTO();
        requestHeaderDTO.setMessageId(CommonUtils.generateUniqueId("M"));
        requestHeaderDTO.setMessageTs(CommonUtils.getCurrentTimeStamp());
        requestHeaderDTO.setAction(txnType.toValue());
        requestHeaderDTO.setSenderId("spp.example.org");
        requestHeaderDTO.setReceiverId("pymts.example.org");
        requestHeaderDTO.setTotalCount(21800);
        requestHeaderDTO.setIsMsgEncrypted(false);
        Map<String, Object> metaMap = new HashMap<>();
        MetaDTO metaDTO = new MetaDTO();
        metaDTO.setData(metaMap);
        requestHeaderDTO.setMeta(metaDTO);
        requestHeaderDTO.setSenderUri("https://spp.example.org/{namespace}/callback/on-search");
        return requestHeaderDTO;
    }

    /**
     * Create whole request from message and header
     *
     * @param searchCriteriaDTOList searchCriteriaDTOList to save in messageDto
     * @param transactionId         transactionId to set in messageDto
     * @return request
     */
    @Override
    public String buildRequest(List<SearchCriteriaDTO> searchCriteriaDTOList, String transactionId, ActionsENUM txnType) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        RequestMessageDTO messageDTO = buildMessage(searchCriteriaDTOList);
        messageDTO.setTransactionId(transactionId);
        HeaderDTO headerDTO = buildHeader(txnType);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setSignature("new signature to be generated for request");
        requestDTO.setHeader(headerDTO);
        requestDTO.setMessage(messageDTO);

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(requestDTO);
    }

    /**
     *
     * @param requestString requestString which need to be sent
     * @param uri uri to which communication need to happen.
     * @param clientId keycloak clientId
     * @param clientSecret keycloak clientSecret
     * @param keyClockClientTokenUrl keycloak ClientTokenUrl
     * @param isEncrypt encryption flag
     * @param isSign signature flag
     * @param fis .p12 file input stream
     * @param p12Password p12Password
     * @param txnType txnType
     * @return G2pcError will return
     * @throws Exception Exception might get thrown from this method
     */
    @Override
    public G2pcError sendRequest(String requestString, String uri, String clientId, String clientSecret,
                                 String keyClockClientTokenUrl, boolean isEncrypt, boolean isSign, InputStream fis,
                                 String encryptedSalt, String p12Password, String txnType) throws Exception {
        log.info("Is encrypted ? -> " + isEncrypt);
        log.info("Is signed ? -> " + isSign);
        ObjectMapper objectMapper = new ObjectMapper();
        requestString = createSignature(isEncrypt, isSign, requestString, fis, encryptedSalt, p12Password, txnType);
        String jwtToken = getValidatedToken(keyClockClientTokenUrl, clientId, clientSecret);
        log.info("Updated Request -> " + requestString);
        HttpResponse<String> response = g2pUnirestHelper.g2pPost(uri)
                .header("Content-Type", "application/json")
                .header("Authorization", jwtToken)
                .body(requestString)
                .asString();
        G2pcError g2pcError = null;
        if (response.getStatus() == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            g2pcError = new G2pcError(ExceptionsENUM.ERROR_SERVICE_UNAVAILABLE.toValue(), response.getBody());
            log.info("Exception is thrown by search endpoint", response.getStatus());
        } else if (response.getStatus() == HttpStatus.UNAUTHORIZED.value()) {
            ErrorResponse errorResponse = objectMapper.readerFor(ErrorResponse.class).
                    readValue(response.getBody());
            g2pcError = errorResponse.getG2PcError();
        } else if (response.getStatus() == HttpStatus.BAD_REQUEST.value()) {
            g2pcError = new G2pcError(ExceptionsENUM.ERROR_BAD_REQUEST.toValue(), response.getBody());
        } else if (response.getStatus() != HttpStatus.OK.value()) {
            ErrorResponse errorResponse = objectMapper.readerFor(ErrorResponse.class).
                    readValue(response.getBody());
            g2pcError = errorResponse.getG2PcError();
        } else if (response.getStatus() == HttpStatus.OK.value()) {
            g2pcError = new G2pcError(HeaderStatusENUM.SUCC.toValue(), "request saved in cache");

        } else {
            g2pcError = new G2pcError(ExceptionsENUM.ERROR_SERVICE_UNAVAILABLE.toValue(), HttpStatus.INTERNAL_SERVER_ERROR.toString());
        }
        log.info("request send response status = {}", response.getStatus());
        return g2pcError;
    }

    /**
     * Method to save token in cache
     *
     * @param cacheKey cacheKey on which tokenExpiryDto is going to store
     * @param tokenExpiryDto tokenExpiryDto which is being stored
     * @throws JsonProcessingException
     */
    @Override
    public void saveToken(String cacheKey, TokenExpiryDto tokenExpiryDto) throws JsonProcessingException {
        ValueOperations<String, String> val = redisTemplate.opsForValue();
        val.set(cacheKey, new ObjectMapper().writeValueAsString(tokenExpiryDto));
    }

    /**
     * Method to get token from cache
     *
     * @param clientId clientId by which token is going to extract
     * @return tokenExpiryDto is going to return
     * @throws JsonProcessingException JsonProcessingException is might be thrown from this method
     */
    @Override
    public TokenExpiryDto getTokenFromCache(String clientId) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Set<String> redisKeys = this.redisTemplate.keys(clientId);
        List<String> cacheKeysList = new ArrayList((Collection) Objects.requireNonNull(redisKeys));
        if (!cacheKeysList.isEmpty()) {
            String cacheKey = cacheKeysList.get(0);
            String tokenData = (String) this.redisTemplate.opsForValue().get(cacheKey);
            TokenExpiryDto tokenExpiryDto = objectMapper.readerFor(TokenExpiryDto.class).readValue(tokenData);
            return tokenExpiryDto;
        }
        return null;
    }


    /**
     * Method to get validated token
     *
     * @param keyCloakUrl keycloak url
     * @param clientId client id for token
     * @param clientSecret client secret
     * @return string which is token
     * @throws IOException which can be thrown
     * @throws UnirestException which can be thrown
     * @throws ParseException which can be thrown
     */
    @Override
    public String getValidatedToken(String keyCloakUrl, String clientId, String clientSecret) throws IOException, UnirestException, ParseException {
        TokenExpiryDto tokenExpiryDto = getTokenFromCache(clientId);

        String jwtToken = "";
        if (Boolean.TRUE.equals(g2pTokenService.isTokenExpired(tokenExpiryDto))) {
            G2pTokenResponse tokenResponse = g2pTokenService.getToken(keyCloakUrl, clientId, clientSecret);
            jwtToken = tokenResponse.getAccessToken();
            saveToken(clientId + "-token", g2pTokenService.createTokenExpiryDto(tokenResponse));
        } else {
            jwtToken = tokenExpiryDto.getToken();
        }
        return jwtToken;
    }


    /**
     * This method is used to create signature and add appropriate message in request body according to configurations.
     *
     * @param isEncrypt     flag of encryption
     * @param isSign        flag of signature
     * @param requestString created request in string format
     * @return created signature in string format
     * @throws Exception exception is thrown general because there more than 6 exception being thrown by this method.
     */
   @SuppressWarnings("unchecked")
   @Override
    public String createSignature(boolean isEncrypt, boolean isSign,
                                   String requestString, InputStream fis, String encryptionSalt,
                                   String p12Password, String txnType) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerSubtypes(RequestHeaderDTO.class,
                ResponseHeaderDTO.class, HeaderDTO.class);
        RequestDTO requestDTO = objectMapper.readerFor(RequestDTO.class).
                readValue(requestString);
        byte[] json = objectMapper.writeValueAsBytes(requestDTO.getMessage());
        String messageString = "";
        if (txnType.equals(CoreConstants.STATUS_TXN_TYPE)) {
            StatusRequestMessageDTO statusRequestMessageDTO = objectMapper.readValue(json, StatusRequestMessageDTO.class);
            messageString = objectMapper.writeValueAsString(statusRequestMessageDTO);
        } else {
            RequestMessageDTO requestMessageDTO = objectMapper.readValue(json, RequestMessageDTO.class);
            messageString = objectMapper.writeValueAsString(requestMessageDTO);
        }

        String requestHeaderString;
        String signature = null;

        if (isSign) {
            if (isEncrypt) {
                String encryptedMessageString = encryptDecrypt.g2pEncrypt(messageString, G2pSecurityConstants.SECRET_KEY);
                requestDTO.setMessage(encryptionSalt + encryptedMessageString);
                requestDTO.getHeader().setIsMsgEncrypted(true);
                Map<String, Object> meta = (Map<String, Object>) requestDTO.getHeader().getMeta().getData();
                meta.put(CoreConstants.IS_SIGN, true);
                requestDTO.getHeader().getMeta().setData(meta);
                requestHeaderString = objectMapper.writeValueAsString(requestDTO.getHeader());
                byte[] asymmetricSignature = asymmetricSignatureService.sign(requestHeaderString + encryptedMessageString, fis, p12Password);
                signature = Base64.getEncoder().encodeToString(asymmetricSignature);
                log.info("Encrypted message ->" + encryptedMessageString);
                log.info("Hashed Signature ->" + signature);
            } else {
                requestDTO.getHeader().setIsMsgEncrypted(false);
                Map<String, Object> meta = (Map<String, Object>) requestDTO.getHeader().getMeta().getData();
                meta.put(CoreConstants.IS_SIGN, true);
                requestDTO.getHeader().getMeta().setData(meta);
                requestHeaderString = objectMapper.writeValueAsString(requestDTO.getHeader());
                byte[] asymmetricSignature = asymmetricSignatureService.sign(requestHeaderString + messageString, fis, p12Password);
                signature = Base64.getEncoder().encodeToString(asymmetricSignature);
                log.info("Hashed Signature ->" + signature);
            }
        } else {
            if (isEncrypt) {
                String encryptedMessageString = encryptDecrypt.g2pEncrypt(messageString, G2pSecurityConstants.SECRET_KEY);
                requestDTO.setMessage(encryptionSalt + encryptedMessageString);
                requestDTO.getHeader().setIsMsgEncrypted(true);
                Map<String, Object> meta = (Map<String, Object>) requestDTO.getHeader().getMeta().getData();
                meta.put(CoreConstants.IS_SIGN, false);
                requestDTO.getHeader().getMeta().setData(meta);
                log.info("Encrypted message ->" + encryptedMessageString);
            } else {
                requestDTO.getHeader().setIsMsgEncrypted(false);
                Map<String, Object> meta = (Map<String, Object>) requestDTO.getHeader().getMeta().getData();
                meta.put(CoreConstants.IS_SIGN, false);
                requestDTO.getHeader().getMeta().setData(meta);
            }
        }
        requestDTO.setSignature(signature);
        requestString = objectMapper.writeValueAsString(requestDTO);
        return requestString;
    }

    /**
     *
     * @param transactionID transactionID used to build transactionRequest of /status
     * @param transactionType transaction type for which transaction request is building
     * @return txnStatusRequestDTO
     */
    @Override
    public TxnStatusRequestDTO buildTransactionRequest(String transactionID, String transactionType) {

        TxnStatusRequestDTO txnStatusRequestDTO = new TxnStatusRequestDTO();
        if (transactionType.equals(StatusTransactionTypeEnum.SEARCH.toValue())) {
            txnStatusRequestDTO.setAttributeType(AttributeTypeEnum.TRANSACTION_ID.toValue());
            txnStatusRequestDTO.setTxnType(StatusTransactionTypeEnum.SEARCH.toValue());
            txnStatusRequestDTO.setAttributeValue(transactionID);
        }
        return txnStatusRequestDTO;
    }

    /**
     *
     * @param txnStatusRequestDTO txnStatusRequestDTO to build status request
     * @param transactionId transactionId for which request is building
     * @param txnType txnType for which request is building
     * @return string
     * @throws JsonProcessingException exception which is going thrown
     */
    @Override
    public String buildStatusRequest(TxnStatusRequestDTO txnStatusRequestDTO, String transactionId, ActionsENUM txnType) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        HeaderDTO headerDTO = buildHeader(txnType);
        StatusRequestMessageDTO messageDTO = buildStatusRequestMessage(txnStatusRequestDTO);
        messageDTO.setTransactionId(transactionId);
        StatusRequestDTO statusRequestDTO = new StatusRequestDTO();
        statusRequestDTO.setSignature("new signature to be generated for request");
        statusRequestDTO.setHeader(headerDTO);
        statusRequestDTO.setMessage(messageDTO);

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(statusRequestDTO);
    }

    /**
     *
     * @param txnStatusRequestDTO txnStatusRequestDTO for which status request is creating.
     * @return StatusRequestMessageDTO
     */
    @Override
    public StatusRequestMessageDTO buildStatusRequestMessage(TxnStatusRequestDTO txnStatusRequestDTO) {
        StatusRequestMessageDTO statusRequestMessageDTO = new StatusRequestMessageDTO();
        statusRequestMessageDTO.setTxnStatusRequest(txnStatusRequestDTO);
        return statusRequestMessageDTO;
    }

    /**
     * Create a payload for request
     * @param payloadFile csv file containing query params data
     * @return List<Map < String, Object>> of payload
     */
    @Override
    public List<Map<String, Object>> generatePayloadFromCsv(File payloadFile) {
        List<Map<String, Object>> payloadMapList = new ArrayList<>();
        Reader reader = null;
        try {
            reader = new BufferedReader(new FileReader(payloadFile));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
            List<CSVRecord> csvRecordList = csvParser.getRecords();
            CSVRecord headerRecord = csvRecordList.get(0);
            List<String> headerList = new ArrayList<>();
            for (int i = 0; i < headerRecord.size(); i++) {
                headerList.add(headerRecord.get(i));
            }
            for (int i = 1; i < csvRecordList.size(); i++) {
                CSVRecord csvRecord = csvRecordList.get(i);
                Map<String, Object> payloadMap = new HashMap<>();
                for (int j = 0; j < headerRecord.size(); j++) {
                    payloadMap.put(headerList.get(j), csvRecord.get(j));
                }
                payloadMapList.add(payloadMap);
            }
        } catch (IOException e) {
            log.error("Error converting to payload : ", e);
        }
        return payloadMapList;
    }

    /**
     *
     * @param requestString requestString which need to be sent
     * @param isEncrypt encryption flag
     * @param isSign signature flag
     * @param fis .p12 file input stream
     * @param p12Password p12Password
     * @param txnType txnType
     * @param sftpServerConfigDTO SftpServerConfigDTO
     * @return G2pcError will return
     * @throws Exception Exception might get thrown from this method
     */
    @Override
    public G2pcError sendRequestSftp(String requestString, boolean isEncrypt, boolean isSign,
                                     InputStream fis, String encryptedSalt, String p12Password,
                                     String txnType, SftpServerConfigDTO sftpServerConfigDTO,String sendFilename) throws Exception {

        log.info("Is encrypted ? -> " + isEncrypt);
        log.info("Is signed ? -> " + isSign);
        requestString = createSignature(isEncrypt, isSign, requestString, fis, encryptedSalt, p12Password, txnType);
        log.info("Updated Request -> " + requestString);

        Path tempFile = Paths.get(System.getProperty("java.io.tmpdir"), sendFilename);
        Files.createFile(tempFile);
        Files.write(tempFile, requestString.getBytes());

        Boolean status = sftpHandlerService.uploadFileToSftp(sftpServerConfigDTO, tempFile.toString(),
                sftpServerConfigDTO.getRemoteInboundDirectory());
        Files.delete(tempFile);
        G2pcError g2pcError;
        if (Boolean.FALSE.equals(status)) {
            g2pcError = new G2pcError(ExceptionsENUM.ERROR_SERVICE_UNAVAILABLE.toValue(), SftpConstants.UPLOAD_ERROR_MESSAGE);
            log.error(SftpConstants.UPLOAD_ERROR_MESSAGE);
        }else {
            g2pcError = new G2pcError(HeaderStatusENUM.SUCC.toValue(), SftpConstants.UPLOAD_SUCCESS_MESSAGE);
        }
        return g2pcError;
    }
}
