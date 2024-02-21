package g2pc.dc.core.lib.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import g2pc.core.lib.dto.common.cache.CacheDTO;
import g2pc.core.lib.dto.common.header.HeaderDTO;
import g2pc.core.lib.dto.common.security.TokenExpiryDto;
import g2pc.core.lib.dto.search.message.request.RequestMessageDTO;
import g2pc.core.lib.dto.search.message.request.SearchCriteriaDTO;
import g2pc.core.lib.dto.sftp.SftpServerConfigDTO;
import g2pc.core.lib.dto.status.message.request.StatusRequestMessageDTO;
import g2pc.core.lib.dto.status.message.request.TxnStatusRequestDTO;
import g2pc.core.lib.enums.ActionsENUM;
import g2pc.core.lib.exceptions.G2pcError;
import kong.unirest.UnirestException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RequestBuilderService {

    public String createSignature(boolean isEncrypt, boolean isSign,
                                  String requestString, InputStream fis, String encryptionSalt,
                                  String p12Password, String txnType) throws Exception;


    List<Map<String, Object>> createQueryMap(List<Map<String, Object>> payloadMapList, Set<Map.Entry<String, Object>> entrySet) throws JsonProcessingException;

    SearchCriteriaDTO getSearchCriteriaDTO(Map<String, Object> queryParamsMap, Map<String, Object> registrySpecificConfigMap);

    RequestMessageDTO buildMessage(List<SearchCriteriaDTO> searchCriteriaDTOList);

    HeaderDTO buildHeader(ActionsENUM txnType) throws JsonProcessingException;

    String buildRequest(List<SearchCriteriaDTO> searchCriteriaDTOList, String transactionId, ActionsENUM txnType) throws JsonProcessingException;

    G2pcError sendRequest(String requestString, String uri, String clientId, String clientSecret,
                          String keyClockClientTokenUrl, boolean isEncrypt, boolean isSign, InputStream fis,
                          String encryptedSalt, String p12Password, String txnType) throws Exception;

    public void saveToken(String cacheKey, TokenExpiryDto tokenExpiryDto) throws JsonProcessingException;

    public TokenExpiryDto getTokenFromCache(String clientId) throws JsonProcessingException;

    public String getValidatedToken(String keyCloakUrl, String clientId, String clientSecret) throws IOException, UnirestException, ParseException;


    TxnStatusRequestDTO buildTransactionRequest(String transactionID, String transactionType);

    String buildStatusRequest(TxnStatusRequestDTO txnStatusRequestDTO, String transactionId, ActionsENUM txnType) throws JsonProcessingException;

    StatusRequestMessageDTO buildStatusRequestMessage(TxnStatusRequestDTO txnStatusRequestDTO);

    List<Map<String, Object>> generatePayloadFromCsv(File payloadFile);

    G2pcError sendRequestSftp(String requestString, boolean isEncrypt, boolean isSign, InputStream fis,
                              String encryptedSalt, String p12Password, String txnType,
                              SftpServerConfigDTO sftpServerConfigDTO,String sendFilename) throws Exception;
}
