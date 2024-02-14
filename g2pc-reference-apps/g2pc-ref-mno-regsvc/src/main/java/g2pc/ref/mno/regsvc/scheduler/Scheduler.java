package g2pc.ref.mno.regsvc.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.dto.common.cache.CacheDTO;
import g2pc.core.lib.dto.common.header.RequestHeaderDTO;
import g2pc.core.lib.dto.common.header.ResponseHeaderDTO;
import g2pc.core.lib.dto.search.message.request.QueryDTO;
import g2pc.core.lib.dto.search.message.request.RequestDTO;
import g2pc.core.lib.dto.status.message.request.StatusRequestDTO;
import g2pc.core.lib.dto.status.message.request.StatusRequestMessageDTO;
import g2pc.core.lib.enums.HeaderStatusENUM;
import g2pc.core.lib.exceptions.G2pHttpException;
import g2pc.core.lib.exceptions.G2pcError;
import g2pc.dp.core.lib.entity.MsgTrackerEntity;
import g2pc.dp.core.lib.service.RequestHandlerService;
import g2pc.dp.core.lib.service.ResponseBuilderService;
import g2pc.dp.core.lib.service.TxnTrackerDbService;
import g2pc.dp.core.lib.service.TxnTrackerRedisService;
import g2pc.dp.core.lib.utils.DpCommonUtils;
import g2pc.ref.mno.regsvc.constants.Constants;
import g2pc.ref.mno.regsvc.service.MobileResponseBuilderService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.ResourceLoader;

@Slf4j
@Service
public class Scheduler {

    @Value("${sunbird.enabled}")
    private Boolean sunbirdEnabled;

    @Value("${client.api_urls.client_search_api}")
    String onSearchURL;

    @Value("${client.api_urls.client_status_api}")
    String onStatusURL;

    @Autowired
    private RequestHandlerService requestHandlerService;

    @Autowired
    private ResponseBuilderService responseBuilderService;

    @Autowired
    private ResourceLoader resourceLoader;
    @Autowired
    private MobileResponseBuilderService mobileResponseBuilderService;

    @Value("${keycloak.from-dc.client-id}")
    private String dcClientId;

    @Value("${keycloak.from-dc.client-secret}")
    private String dcClientSecret;

    @Value("${keycloak.from-dc.url}")
    private String keyClockClientTokenUrl;

    @Autowired
    private TxnTrackerRedisService txnTrackerRedisService;

    @Autowired
    private TxnTrackerDbService txnTrackerDbService;

    @Value("${crypto.to_dc.id}")
    private String dpId;

    @Value("${crypto.to_dc.key_path}")
    private String farmerKeyPath;

    @Autowired
    private DpCommonUtils dpCommonUtils;

    /**
     * This method is a scheduled task that runs every minute.
     * It retrieves data from a Redis cache, processes it, and sends a response.
     * If the status of the data is 'PDNG', it processes the data and updates the status to 'SUCC'.
     * If an exception occurs during the process, it logs the exception message.
     *
     * @throws IOException if an I/O error occurs
     */
    @SuppressWarnings("unchecked")
    @Scheduled(cron = "0 */1 * ? * *")// runs every 1 min.
    @Transactional
    public void responseScheduler() throws IOException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerSubtypes(RequestHeaderDTO.class, ResponseHeaderDTO.class);

            List<String> cacheKeysList = txnTrackerRedisService.getCacheKeys(Constants.CACHE_KEY_SEARCH_STRING);
            for (String cacheKey : cacheKeysList) {
                String requestData = txnTrackerRedisService.getRequestData(cacheKey);
                CacheDTO cacheDTO = objectMapper.readerFor(CacheDTO.class).readValue(requestData);
                if (cacheDTO.getStatus().equals(HeaderStatusENUM.PDNG.toValue())) {
                    String protocol = cacheDTO.getProtocol();
                    RequestDTO requestDTO = objectMapper.readerFor(RequestDTO.class).readValue(cacheDTO.getData());
                    MsgTrackerEntity msgTrackerEntity = txnTrackerDbService.saveRequestDetails(requestDTO, protocol, sunbirdEnabled);
                    List<QueryDTO> queryDTOList = msgTrackerEntity.getTxnTrackerEntityList().stream()
                            .map(txnTrackerEntity -> {
                                try {
                                    return objectMapper.readValue(txnTrackerEntity.getQuery(), QueryDTO.class);
                                } catch (JsonProcessingException e) {
                                    return null;
                                }
                            }).toList();
                    List<String> refRecordsStringsList = mobileResponseBuilderService.getRegMobileRecords(queryDTOList);
                    G2pcError g2pcError = responseBuilderService.buildOnSearchScheduler(refRecordsStringsList, cacheDTO, sunbirdEnabled);
                    log.info("on-search database updation response from sunbird - " + g2pcError.getCode());
                    if (!g2pcError.getCode().equals(HttpStatus.OK.toString())) {
                        throw new G2pHttpException(g2pcError);
                    } else {
                        txnTrackerRedisService.updateRequestDetails(cacheKey, HeaderStatusENUM.SUCC.toValue(), cacheDTO);
                    }
                }
            }
            List<String> statusCacheKeysList = txnTrackerRedisService.getCacheKeys(Constants.STATUS_CACHE_KEY_SEARCH_STRING);
            for (String cacheKey : statusCacheKeysList) {
                String requestData = txnTrackerRedisService.getRequestData(cacheKey);
                CacheDTO cacheDTO = objectMapper.readerFor(CacheDTO.class).readValue(requestData);
                if (cacheDTO.getStatus().equals(HeaderStatusENUM.PDNG.toValue())) {
                    StatusRequestDTO statusRequestDTO = objectMapper.readerFor(StatusRequestDTO.class).readValue(cacheDTO.getData());
                    StatusRequestMessageDTO statusRequestMessageDTO = objectMapper.convertValue(statusRequestDTO.getMessage(), StatusRequestMessageDTO.class);
                    G2pcError g2pcError = responseBuilderService.buildOnStatusScheduler(cacheDTO);
                    log.info("on-status database updation response from sunbird - " + g2pcError.getCode());
                    if (!g2pcError.getCode().equals(HttpStatus.OK.toString())) {
                        throw new G2pHttpException(g2pcError);
                    } else {
                        txnTrackerDbService.updateMessageTrackerStatusDb(statusRequestMessageDTO.getTransactionId());
                        txnTrackerRedisService.updateRequestDetails(cacheKey, HeaderStatusENUM.SUCC.toValue(), cacheDTO);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Exception in responseScheduler: {}", ex.getMessage());
        }
    }
}