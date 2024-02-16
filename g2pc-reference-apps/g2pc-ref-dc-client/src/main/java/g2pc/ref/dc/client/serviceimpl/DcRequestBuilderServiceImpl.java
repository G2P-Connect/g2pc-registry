package g2pc.ref.dc.client.serviceimpl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.constants.CoreConstants;
import g2pc.core.lib.constants.SftpConstants;
import g2pc.core.lib.dto.common.AcknowledgementDTO;
import g2pc.core.lib.dto.search.message.request.SearchCriteriaDTO;
import g2pc.core.lib.dto.sftp.SftpServerConfigDTO;
import g2pc.core.lib.dto.status.message.request.TxnStatusRequestDTO;
import g2pc.core.lib.enums.ActionsENUM;
import g2pc.core.lib.enums.ExceptionsENUM;
import g2pc.core.lib.enums.HeaderStatusENUM;
import g2pc.core.lib.exceptions.G2pcError;
import g2pc.core.lib.service.ElasticsearchService;
import g2pc.core.lib.utils.CommonUtils;
import g2pc.dc.core.lib.constants.DcConstants;
import g2pc.dc.core.lib.dto.ResponseTrackerDto;
import g2pc.dc.core.lib.entity.ResponseTrackerEntity;
import g2pc.dc.core.lib.repository.ResponseTrackerRepository;
import g2pc.dc.core.lib.service.RequestBuilderService;
import g2pc.dc.core.lib.service.TxnTrackerService;
import g2pc.ref.dc.client.config.RegistryConfig;
import g2pc.ref.dc.client.constants.Constants;
import g2pc.ref.dc.client.service.DcRequestBuilderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.elasticsearch.action.search.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.core.io.Resource;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DcRequestBuilderServiceImpl implements DcRequestBuilderService {

    @Value("${sunbird.enabled}")
    private Boolean sunbirdEnabled;

    @Autowired
    private RequestBuilderService requestBuilderService;

    @Autowired
    RegistryConfig registryConfig;

    @Autowired
    TxnTrackerService txnTrackerService;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    ResponseTrackerRepository responseTrackerRepository;

    @Autowired
    private ElasticsearchService elasticsearchService;

    /**
     * Create, save and send a request from payload
     *
     * @param payloadMapList required query params data
     * @return acknowledgement of the request
     */
    @SuppressWarnings("unchecked")
    @Override
    public AcknowledgementDTO generateRequest(List<Map<String, Object>> payloadMapList, String protocol,
                                              String isSignEncrypt, String payloadFilename, String inboundFilename) throws Exception {
        Map<String, G2pcError> g2pcErrorMap = new HashMap<>();

        List<Map<String, Object>> queryMapList = requestBuilderService.createQueryMap(payloadMapList, registryConfig.getQueryParamsConfig().entrySet());

        for (Map.Entry<String, Object> configEntryMap : registryConfig.getRegistrySpecificConfig(isSignEncrypt).entrySet()) {

            List<Map<String, Object>> queryMapFilteredList = queryMapList.stream()
                    .map(map -> map.entrySet().stream()
                            .filter(entry -> entry.getKey().equals(configEntryMap.getKey()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))).toList();

            Map<String, Object> registrySpecificConfigMap = (Map<String, Object>) registryConfig.getRegistrySpecificConfig(isSignEncrypt).get(configEntryMap.getKey());
            List<SearchCriteriaDTO> searchCriteriaDTOList = new ArrayList<>();
            for (Map<String, Object> queryParamsMap : queryMapFilteredList) {
                SearchCriteriaDTO searchCriteriaDTO = requestBuilderService.getSearchCriteriaDTO(queryParamsMap, registrySpecificConfigMap);
                searchCriteriaDTOList.add(searchCriteriaDTO);
            }

            String transactionId = CommonUtils.generateUniqueId("T");
            String requestString = requestBuilderService.buildRequest(searchCriteriaDTOList, transactionId, ActionsENUM.SEARCH);
            String encryptedSalt = "";
            G2pcError g2pcError = new G2pcError();
            switch (isSignEncrypt) {
                case "0":
                    break;
                case "1":
                    encryptedSalt = "salt";
                case "2":
                    break;
            }
            try {
                if (protocol.equals(CoreConstants.SEND_PROTOCOL_HTTPS)) {
                    Resource resource = resourceLoader.getResource(registrySpecificConfigMap.get(CoreConstants.KEY_PATH).toString());

                    InputStream fis = resource.getInputStream();
                    g2pcError = requestBuilderService.sendRequest(requestString,
                            registrySpecificConfigMap.get(CoreConstants.DP_SEARCH_URL).toString(),
                            registrySpecificConfigMap.get(CoreConstants.KEYCLOAK_CLIENT_ID).toString(),
                            registrySpecificConfigMap.get(CoreConstants.KEYCLOAK_CLIENT_SECRET).toString(),
                            registrySpecificConfigMap.get(CoreConstants.KEYCLOAK_URL).toString(),
                            Boolean.parseBoolean(registrySpecificConfigMap.get(CoreConstants.SUPPORT_ENCRYPTION).toString()),
                            Boolean.parseBoolean(registrySpecificConfigMap.get(CoreConstants.SUPPORT_SIGNATURE).toString()),
                            fis, encryptedSalt,
                            registrySpecificConfigMap.get(CoreConstants.KEY_PASSWORD).toString(), CoreConstants.SEARCH_TXN_TYPE);
                    g2pcErrorMap.put(configEntryMap.getKey(), g2pcError);
                    log.info("DP_SEARCH_URL = {}", registrySpecificConfigMap.get(CoreConstants.DP_SEARCH_URL).toString());
                } else if (protocol.equals(CoreConstants.SEND_PROTOCOL_SFTP)) {
                    SftpServerConfigDTO sftpServerConfigDTO = new SftpServerConfigDTO();
                    sftpServerConfigDTO.setUser(registrySpecificConfigMap.get(SftpConstants.SFTP_USER).toString());
                    sftpServerConfigDTO.setHost(registrySpecificConfigMap.get(SftpConstants.SFTP_HOST).toString());
                    sftpServerConfigDTO.setPort(Integer.parseInt(registrySpecificConfigMap.get(SftpConstants.SFTP_PORT).toString()));
                    sftpServerConfigDTO.setPassword(registrySpecificConfigMap.get(SftpConstants.SFTP_PASSWORD).toString());
                    sftpServerConfigDTO.setStrictHostKeyChecking(registrySpecificConfigMap.get(SftpConstants.SFTP_SESSION_CONFIG).toString());
                    sftpServerConfigDTO.setRemoteInboundDirectory(registrySpecificConfigMap.get(SftpConstants.SFTP_REMOTE_INBOUND_DIRECTORY).toString());

                    Resource resource = resourceLoader.getResource(registrySpecificConfigMap.get(CoreConstants.KEY_PATH).toString());
                    InputStream fis = resource.getInputStream();
                    inboundFilename = UUID.randomUUID() + ".json";
                    g2pcError = requestBuilderService.sendRequestSftp(requestString,
                            Boolean.parseBoolean(registrySpecificConfigMap.get(CoreConstants.SUPPORT_ENCRYPTION).toString()),
                            Boolean.parseBoolean(registrySpecificConfigMap.get(CoreConstants.SUPPORT_SIGNATURE).toString()),
                            fis, encryptedSalt,
                            registrySpecificConfigMap.get(CoreConstants.KEY_PASSWORD).toString(), CoreConstants.SEARCH_TXN_TYPE,
                            sftpServerConfigDTO, inboundFilename);
                    g2pcErrorMap.put(configEntryMap.getKey(), g2pcError);
                    if (g2pcError != null && g2pcError.getCode().contains("err")) {
                        log.info("Uploaded failed for : {}", registrySpecificConfigMap.get(CoreConstants.REG_TYPE).toString());
                        throw new Exception("Uploaded failed for : " + registrySpecificConfigMap.get(CoreConstants.REG_TYPE).toString());
                    } else {
                        log.info("Uploaded to inbound of : {}", registrySpecificConfigMap.get(CoreConstants.REG_TYPE).toString());
                    }
                }
                txnTrackerService.saveInitialTransaction(payloadMapList, transactionId, HeaderStatusENUM.RCVD.toValue(), protocol);
                txnTrackerService.saveRequestTransaction(requestString,
                        registrySpecificConfigMap.get(CoreConstants.REG_TYPE).toString(), transactionId, protocol);
               G2pcError g2pcErrorDb= txnTrackerService.saveRequestInDB(requestString,
                        registrySpecificConfigMap.get(CoreConstants.REG_TYPE).toString(), protocol, g2pcError,
                        payloadFilename, inboundFilename,sunbirdEnabled);
               g2pcErrorMap.put(configEntryMap.getKey(),g2pcErrorDb);
            } catch (Exception e) {
                log.error(Constants.GENERATE_REQUEST_ERROR_MESSAGE + ": {}", e.getMessage());
            }
        }
        AcknowledgementDTO acknowledgementDTO = new AcknowledgementDTO();
        acknowledgementDTO.setMessage(g2pcErrorMap);
        acknowledgementDTO.setStatus(HeaderStatusENUM.RCVD.toValue());
        return acknowledgementDTO;
    }

    @SuppressWarnings("unchecked")
    @Override
    public AcknowledgementDTO generateStatusRequest(String transactionID, String transactionType, String protocol) throws Exception {
        AcknowledgementDTO acknowledgementDTO = new AcknowledgementDTO();
        String statusRequestTransactionId = CommonUtils.generateUniqueId("T");
        ObjectMapper objectMapper = new ObjectMapper();
        String encryptedSalt = "";
        Map<String, String> fieldValues = new HashMap<>();
        fieldValues.put("transaction_id.keyword",transactionID);
        SearchResponse responseTrackerSearchResponse = elasticsearchService.exactSearch("response_tracker", fieldValues);
        if (responseTrackerSearchResponse.getHits().getHits().length > 0) {

            String responseTrackerDtoString = responseTrackerSearchResponse.getHits().getHits()[0].getSourceAsString();
            ResponseTrackerDto responseTrackerDto  = objectMapper.readerFor(ResponseTrackerDto.class).
                    readValue(responseTrackerDtoString);
            String registryType = responseTrackerDto.getRegistryType().substring(3).toLowerCase();
            Map<String, Object> registrySpecificConfigMap = (Map<String, Object>) registryConfig.getRegistrySpecificConfig("").get(registryType);
            TxnStatusRequestDTO txnStatusRequestDTO = requestBuilderService.buildTransactionRequest(transactionID, transactionType);
            String statusRequestString = requestBuilderService.buildStatusRequest(txnStatusRequestDTO, statusRequestTransactionId, ActionsENUM.STATUS);
            G2pcError g2pcError = null;
            try {
                Resource resource = resourceLoader.getResource(registrySpecificConfigMap.get(CoreConstants.KEY_PATH).toString());
                InputStream fis = resource.getInputStream();
                g2pcError = requestBuilderService.sendRequest(statusRequestString,
                        registrySpecificConfigMap.get(CoreConstants.DP_STATUS_URL).toString(),
                        registrySpecificConfigMap.get(CoreConstants.KEYCLOAK_CLIENT_ID).toString(),
                        registrySpecificConfigMap.get(CoreConstants.KEYCLOAK_CLIENT_SECRET).toString(),
                        registrySpecificConfigMap.get(CoreConstants.KEYCLOAK_URL).toString(),
                        Boolean.parseBoolean(registrySpecificConfigMap.get(CoreConstants.SUPPORT_ENCRYPTION).toString()),
                        Boolean.parseBoolean(registrySpecificConfigMap.get(CoreConstants.SUPPORT_SIGNATURE).toString()),
                        fis, encryptedSalt,
                        registrySpecificConfigMap.get(CoreConstants.KEY_PASSWORD).toString(), "status");
                log.info("" + g2pcError);
            } catch (Exception e) {
                log.error(Constants.GENERATE_REQUEST_ERROR_MESSAGE, e);
            }
            txnTrackerService.saveInitialStatusTransaction(transactionType, statusRequestTransactionId, HeaderStatusENUM.RCVD.toValue(), protocol);
            txnTrackerService.saveRequestTransaction(statusRequestString,
                    registrySpecificConfigMap.get(CoreConstants.REG_TYPE).toString(), statusRequestTransactionId, protocol);
            txnTrackerService.saveRequestInStatusDB(statusRequestString, registrySpecificConfigMap.get(CoreConstants.REG_TYPE).toString());
            acknowledgementDTO.setMessage(g2pcError);
            acknowledgementDTO.setStatus(HeaderStatusENUM.RCVD.toValue());
        } else {
            G2pcError g2pcError = new G2pcError();
            g2pcError.setCode(ExceptionsENUM.ERROR_REQUEST_NOT_FOUND.toValue());
            g2pcError.setMessage("Data for transaction id " + transactionID + "is not found");
            acknowledgementDTO.setMessage(g2pcError);
        }
        return acknowledgementDTO;
    }


    public String demoTestEncryptionSignature(File payloadFile) throws IOException {
        Reader reader = null;
        reader = new BufferedReader(new FileReader(payloadFile));
        CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
        List<CSVRecord> csvRecordList = csvParser.getRecords();
        CSVRecord headerRecord = (CSVRecord) csvRecordList.get(0);

        return headerRecord.get(headerRecord.size() - 1);

    }
}

