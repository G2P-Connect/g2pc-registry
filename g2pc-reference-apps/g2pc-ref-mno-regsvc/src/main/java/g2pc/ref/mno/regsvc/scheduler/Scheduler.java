package g2pc.ref.mno.regsvc.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.dto.common.cache.CacheDTO;
import g2pc.core.lib.dto.common.header.RequestHeaderDTO;
import g2pc.core.lib.dto.common.message.request.RequestDTO;
import g2pc.core.lib.dto.common.message.response.DataDTO;
import g2pc.core.lib.exceptions.G2pHttpException;
import g2pc.core.lib.exceptions.G2pcError;
import g2pc.dp.core.lib.constants.DpConstants;
import g2pc.dp.core.lib.service.RequestHandlerService;
import g2pc.dp.core.lib.service.ResponseBuilderService;
import g2pc.ref.mno.regsvc.constants.Constants;
import g2pc.ref.mno.regsvc.service.MobileResponseBuilderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class Scheduler {

    @Value("${client.api_urls.client_search_api}")
    String onSearchURL;

    @Autowired
    private RequestHandlerService requestHandlerService;

    @Autowired
    private ResponseBuilderService responseBuilderService;

    @Autowired
    private MobileResponseBuilderService mobileResponseBuilderService;

    @Value("${keycloak.data-consumer.client-id}")
    private String dcClientId;

    @Value("${keycloak.data-consumer.client-secret}")
    private String dcClientSecret;

    @Value("${keycloak.realm}")
    private String keycloakRealm;

    @Value("${keycloak.url}")
    private String keycloakURL;

    @Value("${keycloak.data-consumer.url}")
    private String keyClockClientTokenUrl;

    /**
     * Response scheduler
     */
   // @Scheduled(cron = "0 */1 * ? * *")// runs every 1 min.
    public void responseScheduler() throws IOException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerSubtypes(RequestHeaderDTO.class);

            List<String> cacheKeysList = requestHandlerService.getCacheKeys(Constants.CACHE_KEY_SEARCH_STRING);
            // TODO:  this logic has to be handled to get single cacheKey
            String cacheKey = cacheKeysList.get(0);
            String requestData = requestHandlerService.getRequestData(cacheKey);
            CacheDTO cacheDTO = objectMapper.readerFor(CacheDTO.class).readValue(requestData);

            RequestDTO requestDTO = objectMapper.readerFor(RequestDTO.class).readValue(cacheDTO.getData());

            String refRecordsString = mobileResponseBuilderService.getRegMobileRecords(
                    objectMapper.writeValueAsString(requestDTO.getMessage()));

            DataDTO dataDTO = mobileResponseBuilderService.buildData(refRecordsString);

            String responseString = responseBuilderService.buildResponseString(Constants.CACHE_KEY_SEARCH_STRING, dataDTO);
            log.info("Scheduler responseString : {}", responseString);

            responseBuilderService.sendOnSearchResponse(responseString, onSearchURL,dcClientId,dcClientSecret ,keyClockClientTokenUrl);


            responseBuilderService.updateRequestStatus(cacheKey, DpConstants.COMPLETED, cacheDTO);
        } catch (Exception ex) {
            log.error("Scheduler error : {}", ex.getMessage());
        }
    }
}
