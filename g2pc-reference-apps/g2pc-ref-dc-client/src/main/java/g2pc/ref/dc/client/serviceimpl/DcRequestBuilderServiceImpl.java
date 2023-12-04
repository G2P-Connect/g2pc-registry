package g2pc.ref.dc.client.serviceimpl;

import g2pc.core.lib.constants.CoreConstants;
import g2pc.core.lib.dto.common.AcknowledgementDTO;
import g2pc.core.lib.dto.common.message.request.*;
import g2pc.core.lib.enums.HeaderStatusENUM;
import g2pc.core.lib.utils.CommonUtils;
import g2pc.dc.core.lib.service.RequestBuilderService;
import g2pc.dc.core.lib.service.TxnTrackerService;
import g2pc.ref.dc.client.config.RegistryConfig;
import g2pc.ref.dc.client.constants.Constants;
import g2pc.ref.dc.client.entity.RegistryTransactionsEntity;
import g2pc.ref.dc.client.repository.RegistryTransactionsRepository;
import g2pc.ref.dc.client.service.DcRequestBuilderService;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DcRequestBuilderServiceImpl implements DcRequestBuilderService {

    @Autowired
    private RegistryTransactionsRepository registryTransactionsRepository;

    @Autowired
    private RequestBuilderService requestBuilderService;

    @Autowired
    RegistryConfig registryConfig;

    @Autowired
    TxnTrackerService txnTrackerService;

    /**
     * Create initial transaction in DB
     *
     * @param transactionId unique transaction id
     */
    @Override
    public void createInitialTransactionInDB(String transactionId) {
        Optional<RegistryTransactionsEntity> entityOptional = registryTransactionsRepository.getByTransactionId(transactionId);
        if (entityOptional.isEmpty()) {
            RegistryTransactionsEntity entity = new RegistryTransactionsEntity();
            entity.setTransactionId(transactionId);
            registryTransactionsRepository.save(entity);
        }
    }

    /**
     * Create, save and send a request from payload
     *
     * @param payloadMapList required query params data
     * @return acknowledgement of the request
     */
    @SuppressWarnings("unchecked")
    @Override
    public AcknowledgementDTO generateRequest(List<Map<String, Object>> payloadMapList) throws Exception {
        AcknowledgementDTO acknowledgementDTO = new AcknowledgementDTO();
        acknowledgementDTO.setMessage(Constants.SEARCH_REQUEST_RECEIVED);
        acknowledgementDTO.setStatus(HeaderStatusENUM.RCVD.toValue());

        String transactionId = CommonUtils.generateUniqueId("T");

        txnTrackerService.saveInitialTransaction(payloadMapList, transactionId, HeaderStatusENUM.RCVD.toValue());

        createInitialTransactionInDB(transactionId);

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
            txnTrackerService.saveRequestTransaction(requestString,
                    registrySpecificConfigMap.get(CoreConstants.REG_TYPE).toString(), transactionId);
            log.info("requestString = {}", requestString);
            //sendRequestDemo(requestString, registrySpecificConfigMap.get("url").toString());
           /* requestBuilderService.sendRequest(requestString,
                    registrySpecificConfigMap.get(CoreConstants.DP_SEARCH_URL).toString(),
                    registrySpecificConfigMap.get(CoreConstants.CLIENT_ID).toString(),
                    registrySpecificConfigMap.get(CoreConstants.CLIENT_SECRET).toString(),
                    registrySpecificConfigMap.get(CoreConstants.KEYCLOAK_URL).toString());*/
        }
        return acknowledgementDTO;
    }

    /**
     * Create a payload for request
     *
     * @param payloadFile csv file containing query params data
     * @return acknowledgement of the request
     */
    @Override
    public AcknowledgementDTO generatePayloadFromCsv(MultipartFile payloadFile) throws Exception {
        AcknowledgementDTO acknowledgementDTO = new AcknowledgementDTO();
        acknowledgementDTO.setMessage(Constants.SEARCH_REQUEST_RECEIVED);
        acknowledgementDTO.setStatus("RECEIVED");

        Reader reader = new BufferedReader(new InputStreamReader(payloadFile.getInputStream()));
        CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
        List<Map<String, Object>> payloadMapList = getPayloadMapList(csvParser);
        acknowledgementDTO = generateRequest(payloadMapList);
        return acknowledgementDTO;
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
        HttpResponse<String> response = Unirest.post(uri)
                .body(requestString)
                .asString();
        log.info("request send response status = {}", response.getStatus());
    }
}

