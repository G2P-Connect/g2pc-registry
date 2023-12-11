package g2pc.ref.farmer.regsvc.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.dto.common.cache.CacheDTO;
import g2pc.core.lib.dto.common.header.RequestHeaderDTO;
import g2pc.core.lib.dto.common.header.ResponseHeaderDTO;
import g2pc.core.lib.dto.common.message.request.QueryDTO;
import g2pc.core.lib.dto.common.message.request.RequestDTO;
import g2pc.core.lib.dto.common.message.request.RequestMessageDTO;
import g2pc.core.lib.dto.common.message.response.ResponseMessageDTO;
import g2pc.core.lib.dto.common.message.response.SearchResponseDTO;
import g2pc.core.lib.enums.HeaderStatusENUM;
import g2pc.core.lib.exceptions.G2pHttpException;
import g2pc.core.lib.exceptions.G2pcError;
import g2pc.core.lib.utils.CommonUtils;
import g2pc.dp.core.lib.entity.MsgTrackerEntity;
import g2pc.dp.core.lib.repository.MsgTrackerRepository;
import g2pc.dp.core.lib.repository.TxnTrackerRepository;
import g2pc.dp.core.lib.service.RequestHandlerService;
import g2pc.dp.core.lib.service.ResponseBuilderService;
import g2pc.dp.core.lib.service.TxnTrackerDbService;
import g2pc.dp.core.lib.service.TxnTrackerRedisService;
import g2pc.ref.farmer.regsvc.constants.Constants;
import g2pc.ref.farmer.regsvc.service.FarmerResponseBuilderService;
import jakarta.transaction.Transactional;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class Scheduler {

    @Value("${client.api_urls.client_search_api}")
    String onSearchURL;

    @Value("${client.api_urls.client_search_public_api}")
    String onSearchPublicURL;

    @Autowired
    private RequestHandlerService requestHandlerService;

    @Autowired
    private ResponseBuilderService responseBuilderService;

    @Autowired
    private FarmerResponseBuilderService farmerResponseBuilderService;

    @Autowired
    private TxnTrackerRedisService txnTrackerRedisService;

    @Value("${keycloak.data-consumer.client-id}")
    private String dcClientId;

    @Value("${keycloak.data-consumer.client-secret}")
    private String dcClientSecret;

    @Value("${keycloak.data-consumer.url}")
    private String keyClockClientTokenUrl;

    @Autowired
    private MsgTrackerRepository msgTrackerRepository;

    @Autowired
    private TxnTrackerRepository txnTrackerRepository;

    @Autowired
    private TxnTrackerDbService txnTrackerDbService;

    /**
     * This method is a scheduled task that runs every minute.
     * It retrieves data from a Redis cache, processes it, and sends a response.
     * If the status of the data is 'PDNG', it processes the data and updates the status to 'SUCC'.
     * If an exception occurs during the process, it logs the exception message.
     *
     * @throws IOException if an I/O error occurs
     */
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
                    RequestDTO requestDTO = objectMapper.readerFor(RequestDTO.class).readValue(cacheDTO.getData());
                    RequestMessageDTO messageDTO = objectMapper.convertValue(requestDTO.getMessage(), RequestMessageDTO.class);
                    String transactionId = messageDTO.getTransactionId();

                    MsgTrackerEntity msgTrackerEntity = txnTrackerDbService.saveRequestDetails(requestDTO);
                    List<QueryDTO> queryDTOList = msgTrackerEntity.getTxnTrackerEntityList().stream()
                            .map(txnTrackerEntity -> {
                                try {
                                    return objectMapper.readValue(txnTrackerEntity.getQuery(), QueryDTO.class);
                                } catch (JsonProcessingException e) {
                                    return null;
                                }
                            }).toList();
                    List<String> refRecordsStringsList = farmerResponseBuilderService.getRegFarmerRecords(queryDTOList);

                    List<SearchResponseDTO> searchResponseDTOList = txnTrackerDbService.getUpdatedSearchResponseList(
                            requestDTO, refRecordsStringsList);

                    ResponseHeaderDTO headerDTO = responseBuilderService.getResponseHeaderDTO(msgTrackerEntity);

                    ResponseMessageDTO responseMessageDTO = responseBuilderService.buildResponseMessage(transactionId, searchResponseDTOList);
                    String responseString = responseBuilderService.buildResponseString("signature",
                            headerDTO, responseMessageDTO);
                    responseString = CommonUtils.formatString(responseString);
                    log.info("on-search response = {}", responseString);
                    sendResponseDemo(responseString, onSearchPublicURL);
                    //G2pcError g2pcError = responseBuilderService.sendOnSearchResponse(responseString, onSearchURL,dcClientId,dcClientSecret ,keyClockClientTokenUrl);
                    //if(!g2pcError.getCode().equals(HttpStatus.OK.toString())){
                    //  throw new G2pHttpException(g2pcError);
                    //} else {
                    txnTrackerRedisService.updateRequestDetails(cacheKey, HeaderStatusENUM.SUCC.toValue(), cacheDTO);
                    //}
                }
            }
            //} catch (G2pHttpException e) {
            //  log.error("Exception thrown from on-search endpoint" + e.getG2PcError().getMessage());
        } catch (Exception ex) {
            log.error("Exception in responseScheduler: {}", ex.getMessage());
        }
    }

    private void sendResponseDemo(String responseString, String uri) {
        HttpResponse<String> response = Unirest.post(uri)
                .header("Content-Type", "application/json")
                .body(responseString)
                .asString();
        log.info("response send response status = {}", response.getStatus());
    }
}
