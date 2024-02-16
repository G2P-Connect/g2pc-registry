package g2pc.dp.core.lib.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.dto.common.AcknowledgementDTO;
import g2pc.core.lib.dto.common.cache.CacheDTO;
import g2pc.core.lib.dto.common.header.RequestHeaderDTO;
import g2pc.core.lib.dto.common.header.ResponseHeaderDTO;
import g2pc.core.lib.dto.search.message.request.RequestDTO;
import g2pc.core.lib.dto.status.message.request.StatusRequestDTO;
import g2pc.core.lib.enums.HeaderStatusENUM;
import g2pc.core.lib.utils.CommonUtils;
import g2pc.dp.core.lib.constants.DpConstants;
import g2pc.dp.core.lib.service.RequestHandlerService;
import g2pc.dp.core.lib.service.TxnTrackerDbService;
import g2pc.dp.core.lib.service.TxnTrackerRedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RequestHandlerServiceImpl implements RequestHandlerService {

    @Autowired
    private CommonUtils commonUtils;

    @Autowired
    private TxnTrackerRedisService txnTrackerRedisService;

    @Autowired
    private TxnTrackerDbService txnTrackerDbService;

    /**
     * Build a request to save in Redis cache
     * @param requestData requestData to be stored in redis cache
     * @param cacheKey cacheKey for which data to be stored
     * @return Acknowledgement
     */
    @Override
    public AcknowledgementDTO buildCacheRequest(String requestData, String cacheKey, String protocol, Boolean sunbirdEnabled) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerSubtypes(RequestHeaderDTO.class, ResponseHeaderDTO.class);
        log.info("Request saved in cache with status pending");
        CacheDTO cacheDTO = new CacheDTO();
        cacheDTO.setData(requestData);
        cacheDTO.setStatus(HeaderStatusENUM.PDNG.toValue());
        cacheDTO.setProtocol(protocol);
        cacheDTO.setCreatedDate(CommonUtils.getCurrentTimeStamp());
        cacheDTO.setLastUpdatedDate(CommonUtils.getCurrentTimeStamp());
        RequestDTO requestDTO = objectMapper.readerFor(RequestDTO.class).readValue(cacheDTO.getData());
        txnTrackerRedisService.saveRequestDetails(cacheDTO, cacheKey);
        txnTrackerDbService.saveRequestDetails(requestDTO, protocol,sunbirdEnabled);
        AcknowledgementDTO acknowledgementDTO = new AcknowledgementDTO();
        acknowledgementDTO.setMessage(DpConstants.SEARCH_REQUEST_RECEIVED);
        acknowledgementDTO.setStatus(HeaderStatusENUM.RCVD.toValue());
        return acknowledgementDTO;
    }



    /**
     * @param statusRequestData request data to store
     * @param cacheKey cacheKey for which data storing
     * @param protocol protocol to store in cache
     * @return AcknowledgementDTO
     * @throws JsonProcessingException jsonProcessingException might be thrown
     */
    @Override
    public AcknowledgementDTO buildCacheStatusRequest(String statusRequestData, String cacheKey, String protocol) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerSubtypes(RequestHeaderDTO.class, ResponseHeaderDTO.class);
        log.info("Request saved in cache with status pending");
        CacheDTO cacheDTO = new CacheDTO();
        cacheDTO.setData(statusRequestData);
        cacheDTO.setStatus(HeaderStatusENUM.PDNG.toValue());
        cacheDTO.setProtocol(protocol);
        cacheDTO.setCreatedDate(CommonUtils.getCurrentTimeStamp());
        cacheDTO.setLastUpdatedDate(CommonUtils.getCurrentTimeStamp());
        StatusRequestDTO requestDTO = objectMapper.readerFor(StatusRequestDTO.class).readValue(cacheDTO.getData());
        txnTrackerRedisService.saveRequestDetails(cacheDTO, cacheKey);
        txnTrackerDbService.saveStatusRequestDetails(requestDTO);
        AcknowledgementDTO acknowledgementDTO = new AcknowledgementDTO();
        acknowledgementDTO.setMessage(DpConstants.SEARCH_REQUEST_RECEIVED);
        acknowledgementDTO.setStatus(HeaderStatusENUM.RCVD.toValue());

        return acknowledgementDTO;
    }

}
