package g2pc.dc.core.lib.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import g2pc.core.lib.dto.common.cache.CacheDTO;
import g2pc.core.lib.dto.common.header.HeaderDTO;
import g2pc.core.lib.dto.common.message.request.RequestMessageDTO;
import g2pc.core.lib.dto.common.message.request.SearchCriteriaDTO;
import g2pc.core.lib.dto.common.security.TokenExpiryDto;
import kong.unirest.UnirestException;
import java.io.IOException;
import java.text.ParseException;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RequestBuilderService {

    List<Map<String, Object>> createQueryMap(List<Map<String, Object>> payloadMapList, Set<Map.Entry<String, Object>> entrySet) throws JsonProcessingException;

    SearchCriteriaDTO getSearchCriteriaDTO(Map<String, Object> queryParamsMap, Map<String, Object> registrySpecificConfigMap);

    RequestMessageDTO buildMessage(List<SearchCriteriaDTO> searchCriteriaDTOList);

    HeaderDTO buildHeader() throws JsonProcessingException;

    String buildRequest(List<SearchCriteriaDTO> searchCriteriaDTOList, String transactionId) throws JsonProcessingException;

    Integer sendRequest(String requestString, String uri, String clientId, String clientSecret , String keyClockClientTokenUrl ,   boolean isEncrypt, boolean isSign) throws Exception;

    CacheDTO createCache(String data, String status);

    void saveCache(CacheDTO cacheDTO, String cacheKey) throws JsonProcessingException;

    public void saveToken(String cacheKey , TokenExpiryDto tokenExpiryDto) throws JsonProcessingException;

    public TokenExpiryDto getTokenFromCache(String clientId) throws JsonProcessingException;

    public String getValidatedToken(String keyCloakUrl , String clientId , String clientSecret) throws IOException, UnirestException, ParseException;
}
