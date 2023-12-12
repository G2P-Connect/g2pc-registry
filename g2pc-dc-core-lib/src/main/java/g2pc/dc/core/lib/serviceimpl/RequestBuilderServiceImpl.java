package g2pc.dc.core.lib.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.config.G2pUnirestHelper;
import g2pc.core.lib.constants.CoreConstants;
import g2pc.core.lib.constants.G2pSecurityConstants;
import g2pc.core.lib.dto.common.cache.CacheDTO;
import g2pc.core.lib.dto.common.header.HeaderDTO;
import g2pc.core.lib.dto.common.header.MetaDTO;
import g2pc.core.lib.dto.common.header.RequestHeaderDTO;
import g2pc.core.lib.dto.common.header.ResponseHeaderDTO;
import g2pc.core.lib.dto.common.message.request.*;
import g2pc.core.lib.dto.common.security.G2pTokenResponse;
import g2pc.core.lib.dto.common.security.TokenExpiryDto;
import g2pc.core.lib.enums.ActionsENUM;
import g2pc.core.lib.enums.ExceptionsENUM;
import g2pc.core.lib.enums.LocalesENUM;
import g2pc.core.lib.exceptionhandler.ErrorResponse;
import g2pc.core.lib.exceptions.G2pHttpException;
import g2pc.core.lib.exceptions.G2pcError;
import g2pc.core.lib.security.service.AsymmetricSignatureService;
import g2pc.core.lib.security.service.G2pEncryptDecrypt;
import g2pc.core.lib.security.service.G2pTokenService;
import g2pc.core.lib.utils.CommonUtils;
import g2pc.dc.core.lib.service.RequestBuilderService;
import kong.unirest.HttpResponse;
import kong.unirest.UnirestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.*;
import java.text.ParseException;

@Service
@Slf4j
public class RequestBuilderServiceImpl implements RequestBuilderService {

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

        SearchCriteriaDTO searchCriteriaDTO = new SearchCriteriaDTO();
        searchCriteriaDTO.setVersion("1.0.0");
        searchCriteriaDTO.setRegType(registrySpecificConfigMap.get("reg_type").toString());
        searchCriteriaDTO.setRegSubType(registrySpecificConfigMap.get("reg_sub_type").toString());
        searchCriteriaDTO.setQueryType(registrySpecificConfigMap.get("query_type").toString());

        QueryDTO queryDTO = new QueryDTO();
        queryDTO.setQueryName(registrySpecificConfigMap.get("query_name").toString());
        queryDTO.setQueryParams(queryParamsMap.values().iterator().hasNext() ? queryParamsMap.values().iterator().next() : "");

        searchCriteriaDTO.setQuery(queryDTO);
        searchCriteriaDTO.setSort(sortDTOList);
        searchCriteriaDTO.setPagination(paginationDTO);
        searchCriteriaDTO.setConsent(consentDTO);
        searchCriteriaDTO.setAuthorize(authorizeDTO);
        return searchCriteriaDTO;
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
     *
     * @return HeaderDTO
     */
    @Override
    public HeaderDTO buildHeader() {
        RequestHeaderDTO requestHeaderDTO = new RequestHeaderDTO();
        requestHeaderDTO.setMessageId(CommonUtils.generateUniqueId("M"));
        requestHeaderDTO.setMessageTs(CommonUtils.getCurrentTimeStamp());
        requestHeaderDTO.setAction(ActionsENUM.SEARCH.toValue());
        requestHeaderDTO.setSenderId("spp.example.org");
        requestHeaderDTO.setReceiverId("pymts.example.org");
        requestHeaderDTO.setTotalCount(21800);
        requestHeaderDTO.setIsMsgEncrypted(false);
        Map<String , Object > metaMap =  new HashMap<>();
        MetaDTO metaDTO = new MetaDTO();
        metaDTO.setData(metaMap);
        requestHeaderDTO.setMeta(metaDTO);
        requestHeaderDTO.setSenderUri("https://spp.example.org/{namespace}/callback/on-search");
        return requestHeaderDTO;
    }

    /**
     * Create whole request from message and header
     *
     * @param searchCriteriaDTOList required
     * @param transactionId         required
     * @return request
     */
    @Override
    public String buildRequest(List<SearchCriteriaDTO> searchCriteriaDTOList, String transactionId) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        RequestMessageDTO messageDTO = buildMessage(searchCriteriaDTOList);
        messageDTO.setTransactionId(transactionId);

        HeaderDTO headerDTO = buildHeader();

        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setSignature("new signature to be generated for request");
        requestDTO.setHeader(headerDTO);
        requestDTO.setMessage(messageDTO);

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(requestDTO);
    }

    @Override
    public Integer sendRequest(String requestString, String uri, String clientId, String clientSecret, String keyClockClientTokenUrl , boolean isEncrypt, boolean isSign) throws Exception {
        Boolean isStatusOk = true;
        log.info("Is encrypted ? -> "+isEncrypt);
        log.info("Is signed ? -> "+isSign);
        ObjectMapper objectMapper = new ObjectMapper();
        requestString = createSignature(isEncrypt ,isSign,requestString);
        String jwtToken = getValidatedToken(keyClockClientTokenUrl, clientId, clientSecret);
        log.info("Updated Request -> "+requestString);
        HttpResponse<String> response = g2pUnirestHelper.g2pPost(uri)
                .header("Content-Type", "application/json")
                .header("Authorization", jwtToken)
                .body(requestString)
                .asString();
        if (response.getStatus() == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            G2pcError g2pcError = new G2pcError(ExceptionsENUM.ERROR_SERVICE_UNAVAILABLE.toValue(), response.getBody());
            log.info("Exception is thrown by search endpoint", response.getStatus());
            throw new G2pHttpException(g2pcError);
        } else if (response.getStatus() == HttpStatus.UNAUTHORIZED.value()) {
            ErrorResponse errorResponse = objectMapper.readerFor(ErrorResponse.class).
                    readValue(response.getBody());
            log.info("Exception is thrown by search endpoint", response.getStatus());
            throw new G2pHttpException(errorResponse.getG2PcError());
        } else if (response.getStatus() == HttpStatus.BAD_REQUEST.value()) {
            G2pcError g2pcError = new G2pcError(ExceptionsENUM.ERROR_BAD_REQUEST.toValue(), response.getBody());
            log.info("Exception is thrown by search endpoint", response.getStatus());
            throw new G2pHttpException(g2pcError);
        } else if (response.getStatus() != HttpStatus.OK.value()) {
            ErrorResponse errorResponse = objectMapper.readerFor(ErrorResponse.class).
                    readValue(response.getBody());
            log.info("Exception is thrown by search endpoint", response.getStatus());
            throw new G2pHttpException(errorResponse.getG2PcError());
        }

        log.info("request send response status = {}", response.getStatus());
        return response.getStatus();
    }

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

    @Override
    public void saveCache(CacheDTO cacheDTO, String cacheKey) throws JsonProcessingException {
        ValueOperations<String, String> val = redisTemplate.opsForValue();
        val.set(cacheKey, new ObjectMapper().writeValueAsString(cacheDTO));
    }

    /**
     * Method to save token in cache
     *
     * @param cacheKey
     * @param tokenExpiryDto
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
     * @param clientId
     * @return
     * @throws JsonProcessingException
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
     * @param keyCloakUrl
     * @param clientId
     * @param clientSecret
     * @return
     * @throws IOException
     * @throws UnirestException
     * @throws ParseException
     */
    @Override
    public String getValidatedToken(String keyCloakUrl, String clientId, String clientSecret) throws IOException, UnirestException, ParseException {
        TokenExpiryDto tokenExpiryDto = getTokenFromCache(clientId);

        String jwtToken = "";
        if (g2pTokenService.isTokenExpired(tokenExpiryDto)) {
            G2pTokenResponse tokenResponse = g2pTokenService.getToken(keyCloakUrl, clientId, clientSecret);
            jwtToken = tokenResponse.getAccess_token();
            saveToken(clientId, g2pTokenService.createTokenExpiryDto(tokenResponse));
        } else {
            jwtToken = tokenExpiryDto.getToken();
        }
        return jwtToken;
    }

    /**
     * This method is used to create signature and add appropriate message in request body according to configurations.
     * @param isEncrypt flag of encryption
     * @param isSign flag of signature
     * @param requestString created request in string format
     * @return
     * @throws Exception exception is thrown general because there more than 6 exception being thrown by this method.
     */
    private String createSignature( boolean isEncrypt , boolean isSign, String requestString) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerSubtypes(RequestHeaderDTO.class,
                ResponseHeaderDTO.class, HeaderDTO.class);
        RequestDTO requestDTO = objectMapper.readerFor(RequestDTO.class).
                readValue(requestString);
        byte[] json = objectMapper.writeValueAsBytes(requestDTO.getMessage());
        RequestMessageDTO messageDTO =  objectMapper.readValue(json, RequestMessageDTO.class);
        RequestHeaderDTO requestHeaderDTO = (RequestHeaderDTO) requestDTO.getHeader();
        String requestHeaderString = objectMapper.writeValueAsString(requestHeaderDTO);
        String messageString = objectMapper.writeValueAsString(messageDTO);
        String signature = null;

        if(isSign){
            if(isEncrypt){
                String encryptedMessageString = encryptDecrypt.g2pEncrypt(messageString,G2pSecurityConstants.SECRET_KEY);
                requestDTO.setMessage(encryptedMessageString);
                requestDTO.getHeader().setIsMsgEncrypted(true);
                Map<String , Object> meta= (Map<String, Object>) requestDTO.getHeader().getMeta().getData();
                meta.put(CoreConstants.IS_SIGN,true);
                requestDTO.getHeader().getMeta().setData(meta);
                requestHeaderString = objectMapper.writeValueAsString(requestDTO.getHeader());
                byte[] asymmetricSignature = asymmetricSignatureService.sign( "salt"+requestHeaderString+encryptedMessageString);
                signature = Base64.getEncoder().encodeToString(asymmetricSignature);
                log.info("Encrypted message ->"+encryptedMessageString);
                log.info("Hashed Signature ->"+signature);
            }else{
                requestDTO.getHeader().setIsMsgEncrypted(false);
                Map<String , Object> meta= (Map<String, Object>) requestDTO.getHeader().getMeta().getData();
                meta.put(CoreConstants.IS_SIGN,true);
                requestDTO.getHeader().getMeta().setData(meta);
                requestHeaderString = objectMapper.writeValueAsString(requestDTO.getHeader());
                byte[] asymmetricSignature = asymmetricSignatureService.sign( requestHeaderString+messageString);
                signature = Base64.getEncoder().encodeToString(asymmetricSignature);
                log.info("Hashed Signature ->"+signature);
            }
        } else {
            if(isEncrypt){
                String encryptedMessageString = encryptDecrypt.g2pEncrypt(messageString,G2pSecurityConstants.SECRET_KEY);
                requestDTO.setMessage(encryptedMessageString);
                requestDTO.getHeader().setIsMsgEncrypted(true);
                Map<String , Object> meta= (Map<String, Object>) requestDTO.getHeader().getMeta().getData();
                meta.put(CoreConstants.IS_SIGN,false);
                requestDTO.getHeader().getMeta().setData(meta);
                log.info("Encrypted message ->"+encryptedMessageString);
            }else{
                requestDTO.getHeader().setIsMsgEncrypted(false);
                Map<String , Object> meta= (Map<String, Object>) requestDTO.getHeader().getMeta().getData();
                meta.put(CoreConstants.IS_SIGN,false);
                requestDTO.getHeader().getMeta().setData(meta);
            }
        }
        requestDTO.setSignature(signature);
        requestString = objectMapper.writeValueAsString(requestDTO);
        return requestString;
    }
}
