package g2pc.ref.dc.client.serviceimpl;

import g2pc.core.lib.constants.CoreConstants;
import g2pc.core.lib.dto.common.AcknowledgementDTO;
import g2pc.core.lib.dto.common.header.HeaderDTO;
import g2pc.core.lib.dto.common.header.RequestHeaderDTO;
import g2pc.core.lib.dto.common.header.ResponseHeaderDTO;
import g2pc.core.lib.dto.common.message.request.*;
import g2pc.core.lib.enums.HeaderStatusENUM;
import g2pc.core.lib.exceptions.G2pcError;
import g2pc.core.lib.utils.CommonUtils;
import g2pc.dc.core.lib.service.RequestBuilderService;
import g2pc.dc.core.lib.service.TxnTrackerService;
import g2pc.ref.dc.client.config.RegistryConfig;
import g2pc.ref.dc.client.service.DcRequestBuilderService;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Slf4j
public class DcRequestBuilderServiceImpl implements DcRequestBuilderService {

    @Autowired
    private RequestBuilderService requestBuilderService;

    @Autowired
    RegistryConfig registryConfig;

    @Autowired
    TxnTrackerService txnTrackerService;

    @Autowired
    private ResourceLoader resourceLoader;

    /**
     * Create, save and send a request from payload
     *
     * @param payloadMapList required query params data
     * @return acknowledgement of the request
     */
    @SuppressWarnings("unchecked")
    @Override
    public Map<String, G2pcError> generateRequest(List<Map<String, Object>> payloadMapList) throws Exception {
        Map<String, G2pcError> g2pcErrorMap = new HashMap<>();

        String transactionId = CommonUtils.generateUniqueId("T");

        txnTrackerService.saveInitialTransaction(payloadMapList, transactionId, HeaderStatusENUM.RCVD.toValue());

        List<Map<String, Object>> queryMapList = requestBuilderService.createQueryMap(payloadMapList, registryConfig.getQueryParamsConfig().entrySet());
        for (Map.Entry<String, Object> configEntryMap : registryConfig.getRegistrySpecificConfig().entrySet()) {
            List<Map<String, Object>> queryMapFilteredList = queryMapList.stream()
                    .map(map -> map.entrySet().stream()
                            .filter(entry -> entry.getKey().equals(configEntryMap.getKey()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))).toList();

            Map<String, Object> registrySpecificConfigMap = (Map<String, Object>) registryConfig.getRegistrySpecificConfig().get(configEntryMap.getKey());
            List<SearchCriteriaDTO> searchCriteriaDTOList = new ArrayList<>();
            for (Map<String, Object> queryParamsMap : queryMapFilteredList) {
                SearchCriteriaDTO searchCriteriaDTO = requestBuilderService.getSearchCriteriaDTO(queryParamsMap, registrySpecificConfigMap);
                searchCriteriaDTOList.add(searchCriteriaDTO);
            }

            String requestString = requestBuilderService.buildRequest(searchCriteriaDTOList, transactionId);
            try {
                Resource resource = resourceLoader.getResource(registrySpecificConfigMap.get(CoreConstants.KEY_PATH).toString());
                String encryptedSalt = "";
                InputStream fis = resource.getInputStream();
                G2pcError g2pcError = requestBuilderService.sendRequest(requestString,
                        registrySpecificConfigMap.get(CoreConstants.DP_SEARCH_URL).toString(),
                        registrySpecificConfigMap.get(CoreConstants.KEYCLOAK_CLIENT_ID).toString(),
                        registrySpecificConfigMap.get(CoreConstants.KEYCLOAK_CLIENT_SECRET).toString(),
                        registrySpecificConfigMap.get(CoreConstants.KEYCLOAK_URL).toString(),
                        Boolean.parseBoolean(registrySpecificConfigMap.get(CoreConstants.SUPPORT_ENCRYPTION).toString()),
                        Boolean.parseBoolean(registrySpecificConfigMap.get(CoreConstants.SUPPORT_SIGNATURE).toString()),
                        fis, encryptedSalt,
                        registrySpecificConfigMap.get(CoreConstants.KEY_PASSWORD).toString());
                g2pcErrorMap.put(configEntryMap.getKey(), g2pcError);

                txnTrackerService.saveInitialTransaction(payloadMapList, transactionId, HeaderStatusENUM.RCVD.toValue());
                txnTrackerService.saveRequestTransaction(requestString,
                        registrySpecificConfigMap.get(CoreConstants.REG_TYPE).toString(), transactionId);
                txnTrackerService.saveRequestInDB(requestString, registrySpecificConfigMap.get(CoreConstants.REG_TYPE).toString());
            } catch (Exception e) {
                log.error("Exception in generateRequest: {}", e);
            }
        }
        //TODO: convert returning map to acknowledgementDTO
        return g2pcErrorMap;
    }

    /**
     * Create a payload for request
     *
     * @param payloadFile csv file containing query params data
     * @return acknowledgement of the request
     */
    @Override
    public Map<String, G2pcError> generatePayloadFromCsv(MultipartFile payloadFile) throws Exception {
        Reader reader = new BufferedReader(new InputStreamReader(payloadFile.getInputStream()));
        CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
        List<Map<String, Object>> payloadMapList = getPayloadMapList(csvParser);
        Map<String, G2pcError> acknowledgement = new HashMap<>();
        if (ObjectUtils.isNotEmpty(payloadMapList)) {
            acknowledgement = generateRequest(payloadMapList);
        }
        //TODO: convert returning map to acknowledgementDTO
        return acknowledgement;
    }

    private static List<Map<String, Object>> getPayloadMapList(CSVParser csvParser) {
        List<CSVRecord> csvRecordList = csvParser.getRecords();
        CSVRecord headerRecord = csvRecordList.get(0);
        List<String> headerList = new ArrayList<>();
        for (int i = 0; i < headerRecord.size(); i++) {
            headerList.add(headerRecord.get(i));
        }
        List<Map<String, Object>> payloadMapList = new ArrayList<>();
        for (int i = 1; i < csvRecordList.size(); i++) {
            CSVRecord csvRecord = csvRecordList.get(i);
            Map<String, Object> payloadMap = new HashMap<>();
            for (int j = 0; j < headerRecord.size(); j++) {
                payloadMap.put(headerList.get(j), csvRecord.get(j));
            }
            payloadMapList.add(payloadMap);
        }
        return payloadMapList;
    }

    private void sendRequestDemo(String requestString, String uri) {
        try {
            HttpResponse<String> response = Unirest.post(uri)
                    .header("Content-Type", "application/json")
                    .body(requestString)
                    .asString();
            log.info("request send response status = {}", response.getStatus());
        } catch (Exception ex) {
            log.error("request send error ");
        }
    }
}

