package g2pc.dp.core.lib.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.dto.common.cache.CacheDTO;
import g2pc.core.lib.dto.common.header.ResponseHeaderDTO;
import g2pc.core.lib.dto.common.message.response.*;
import g2pc.core.lib.enums.AlgorithmENUM;
import g2pc.core.lib.enums.HeaderStatusENUM;
import g2pc.core.lib.utils.CommonUtils;
import g2pc.dp.core.lib.constants.DpConstants;
import g2pc.dp.core.lib.service.RequestHandlerService;
import g2pc.dp.core.lib.service.ResponseBuilderService;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ResponseBuilderServiceImpl implements ResponseBuilderService {

    @Autowired
    private RequestHandlerService requestService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private CommonUtils commonUtils;

    @Override
    public String buildResponseMessage(DataDTO dataDTO) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        PaginationDTO paginationDTO = new PaginationDTO();
        paginationDTO.setPageSize(10);
        paginationDTO.setPageNumber(1);
        paginationDTO.setTotalCount(100);

        SearchResponseDTO searchResponseDTO = new SearchResponseDTO();
        searchResponseDTO.setReferenceId("12345678901234567890");
        searchResponseDTO.setTimestamp("2022-12-04T17:20:07-04:00");
        searchResponseDTO.setStatus(HeaderStatusENUM.SUCC.toValue());
        searchResponseDTO.setStatusReasonCode("");
        searchResponseDTO.setStatusReasonMessage("");
        searchResponseDTO.setData(dataDTO);
        searchResponseDTO.setPagination(paginationDTO);
        searchResponseDTO.setLocale("en");

        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setTransactionId("123456789");
        messageDTO.setCorrelationId("9876543210");
        messageDTO.setSearchResponse(searchResponseDTO);

        return objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(messageDTO);
    }

    @Override
    public String buildResponseHeader(String headerInfoString, String messageString) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        ResponseHeaderDTO headerDTO = objectMapper.readerFor(ResponseHeaderDTO.class).
                readValue(headerInfoString);
        return new ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(headerDTO);
    }

    @Override
    public String getResponse(String headerInfoString, String messageString, String algorithm) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        ResponseHeaderDTO responseHeaderDTO = objectMapper.readerFor(ResponseHeaderDTO.class).
                readValue(headerInfoString);

        MessageDTO messageDTO = objectMapper.readerFor(MessageDTO.class).
                readValue(messageString);

        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setSignature("new signature to be generated for response");
        responseDTO.setHeader(responseHeaderDTO);
        responseDTO.setMessage(messageDTO);

        return objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(responseDTO);
    }

    @Override
    public String buildResponseString(String cacheKeySearchString,DataDTO dataDTO) throws JsonProcessingException {
        String responseString = "";

        String message = buildResponseMessage(dataDTO);

        ResponseHeaderDTO responseHeaderDTO = getResponseHeaderDTO();

        String headerInfoString = new ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(responseHeaderDTO);

        String header = buildResponseHeader(headerInfoString, message);
        responseString = getResponse(header, message, AlgorithmENUM.ED25519.toValue());
        return responseString;
    }

    /**
     * Get response header
     *
     * @return ResponseHeaderDTO currently using static header data
     */
    private static ResponseHeaderDTO getResponseHeaderDTO() {
        ResponseHeaderDTO responseHeaderDTO = new ResponseHeaderDTO();
        responseHeaderDTO.setMessageId("123");
        responseHeaderDTO.setMessageTs("2022-12-04T17:20:07-04:00");
        responseHeaderDTO.setAction("on-search");
        responseHeaderDTO.setSenderId("spp.example.org");
        responseHeaderDTO.setReceiverId("pymts.example.org");
        responseHeaderDTO.setTotalCount(21800);
        responseHeaderDTO.setIsMsgEncrypted(false);
        responseHeaderDTO.setMeta(null);
        responseHeaderDTO.setStatus(HeaderStatusENUM.SUCC.toValue());
        responseHeaderDTO.setStatusReasonCode("");
        responseHeaderDTO.setStatusReasonMessage("");
        responseHeaderDTO.setCompletedCount(50);
        return responseHeaderDTO;
    }

    @Override
    public void sendOnSearchResponse(String responseString, String uri) {
        log.info("Send on-search response");
        HttpResponse<String> response = Unirest.post(uri)
                .header("Content-Type", "application/json")
                .body(responseString)
                .asString();
        log.info("on-search response status = {}", response.getStatus());
    }

    @Override
    public void updateRequestStatus(String cacheKey, String status, CacheDTO cacheDTO) throws JsonProcessingException {
        log.info("Updated cache status");
       cacheDTO.setStatus(status);
        cacheDTO.setLastUpdatedDate(commonUtils.getCurrentTimeStamp());

        ValueOperations<String, String> val = redisTemplate.opsForValue();
        val.set(cacheKey, new ObjectMapper().writeValueAsString(cacheDTO));
    }
}
