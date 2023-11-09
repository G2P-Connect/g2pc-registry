package g2pc.dc.core.lib.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.dto.common.AcknowledgementDTO;
import g2pc.core.lib.dto.common.cache.CacheDTO;
import g2pc.core.lib.dto.common.header.HeaderDTO;
import g2pc.core.lib.dto.common.header.RequestHeaderDTO;
import g2pc.core.lib.dto.common.message.request.*;
import g2pc.core.lib.enums.SortOrderEnum;
import g2pc.core.lib.utils.CommonUtils;
import g2pc.dc.core.lib.constants.DcConstants;
import g2pc.dc.core.lib.service.RequestBuilderService;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class RequestBuilderServiceImpl implements RequestBuilderService {

    @Autowired
    private CommonUtils commonUtils;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public MessageDTO buildMessage(SearchCriteriaDTO searchCriteriaDTO) {
        SearchRequestDTO searchRequestDTO = new SearchRequestDTO();
        searchRequestDTO.setReferenceId("txn98765");
        searchRequestDTO.setTimestamp("2022-12-04T17:20:07-04:00");
        searchRequestDTO.setLocale("en");
        searchRequestDTO.setSearchCriteria(searchCriteriaDTO);

        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setTransactionId("123456789");
        messageDTO.setSearchRequest(searchRequestDTO);

        return messageDTO;
    }

    @Override
    public HeaderDTO buildHeader() {
        RequestHeaderDTO requestHeaderDTO = new RequestHeaderDTO();
        requestHeaderDTO.setMessageId("123");
        requestHeaderDTO.setMessageTs("2022-12-04T17:20:07-04:00");
        requestHeaderDTO.setAction("search");
        requestHeaderDTO.setSenderId("spp.example.org");
        requestHeaderDTO.setReceiverId("pymts.example.org");
        requestHeaderDTO.setTotalCount(21800);
        requestHeaderDTO.setIsMsgEncrypted(false);
        requestHeaderDTO.setMeta(null);
        requestHeaderDTO.setSenderUri("https://spp.example.org/{namespace}/callback/on-search");
        return requestHeaderDTO;
    }

    @Override
    public String buildRequest(SearchCriteriaDTO searchCriteriaDTO, String transactionId) throws JsonProcessingException {

        MessageDTO messageDTO = buildMessage(searchCriteriaDTO);
        messageDTO.setTransactionId(transactionId);

        HeaderDTO headerDTO = buildHeader();

        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setSignature("new signature to be generated for request");
        requestDTO.setHeader(headerDTO);
        requestDTO.setMessage(messageDTO);

        return new ObjectMapper().writeValueAsString(requestDTO);
    }

    @Override
    public void sendRequest(String requestString, String uri) {
        log.info("Save requests to DPs");
        HttpResponse<String> response = Unirest.post(uri)
                .header("Content-Type", "application/json")
                .body(requestString)
                .asString();
        log.info("request send response status = {}", response.getStatus());
    }

    @Override
    public CacheDTO createCache(String data, String status) {
        log.info("Save requests in cache with status pending");
        CacheDTO cacheDTO = new CacheDTO();
        cacheDTO.setData(data);
        cacheDTO.setStatus(status);
        cacheDTO.setCreatedDate(commonUtils.getCurrentTimeStamp());
        cacheDTO.setLastUpdatedDate(commonUtils.getCurrentTimeStamp());
        return cacheDTO;
    }

    @Override
    public void saveCache(CacheDTO cacheDTO, String cacheKey) throws JsonProcessingException {
        ValueOperations<String, String> val = redisTemplate.opsForValue();
        val.set(cacheKey, new ObjectMapper().writeValueAsString(cacheDTO));
    }
}
