package g2pc.ref.dc.client.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.dto.common.AcknowledgementDTO;
import g2pc.core.lib.dto.common.cache.CacheDTO;
import g2pc.core.lib.dto.common.message.request.*;
import g2pc.core.lib.enums.SortOrderEnum;
import g2pc.core.lib.utils.CommonUtils;
import g2pc.dc.core.lib.service.RequestBuilderService;
import g2pc.ref.dc.client.constants.Constants;
import g2pc.ref.dc.client.dto.farmer.request.QueryParamsFarmerDTO;
import g2pc.ref.dc.client.dto.mobile.request.QueryParamsMobileDTO;
import g2pc.ref.dc.client.dto.payload.PayloadDTO;
import g2pc.ref.dc.client.dto.payload.PayloadDataDTO;
import g2pc.ref.dc.client.entity.RegistryTransactionsEntity;
import g2pc.ref.dc.client.repository.RegistryTransactionsRepository;
import g2pc.ref.dc.client.service.DcRequestBuilderService;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class DcRequestBuilderServiceImpl implements DcRequestBuilderService {

    @Value("${registry.api_urls.farmer_search_api}")
    private String farmerSearchURL;

    @Value("${registry.api_urls.mobile_search_api}")
    private String mobileSearchURL;

    @Autowired
    private CommonUtils commonUtils;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RegistryTransactionsRepository registryTransactionsRepository;

    @Autowired
    private RequestBuilderService requestBuilderService;

    @Override
    public List<QueryDTO> createQuery(String payloadString) throws JsonProcessingException {
        PayloadDTO payloadDTO = new ObjectMapper().readValue(payloadString, PayloadDTO.class);
        List<QueryDTO> queryDTOList = new ArrayList<>();
        PayloadDataDTO payloadDataDTO = payloadDTO.getData();
        if (StringUtils.isNotEmpty(payloadDataDTO.getFarmerId())) {
            QueryParamsFarmerDTO queryParamsDTO = new QueryParamsFarmerDTO();
            queryParamsDTO.setFarmerId(Collections.singletonList(payloadDataDTO.getFarmerId()));
            queryParamsDTO.setSeason(payloadDataDTO.getSeason());

            QueryDTO queryDTO = new QueryDTO();
            queryDTO.setQueryName(Constants.PAID_FARMER);
            queryDTO.setQueryParams(queryParamsDTO);
            queryDTOList.add(queryDTO);
        }
        if (StringUtils.isNotEmpty(payloadDataDTO.getMobileNumber())) {
            QueryParamsMobileDTO queryParamsDTO = new QueryParamsMobileDTO();
            queryParamsDTO.setMobileNumber(Collections.singletonList(payloadDataDTO.getMobileNumber()));
            queryParamsDTO.setSeason(payloadDataDTO.getSeason());

            QueryDTO queryDTO = new QueryDTO();
            queryDTO.setQueryName(Constants.IS_REGISTERED);
            queryDTO.setQueryParams(queryParamsDTO);
            queryDTOList.add(queryDTO);
        }
        return queryDTOList;
    }

    @Override
    public AcknowledgementDTO generateRequest(String payloadString) throws JsonProcessingException {
        String requestString = "";
        PayloadDTO payloadDTO = new ObjectMapper().readValue(payloadString, PayloadDTO.class);
        ObjectMapper objectMapper = new ObjectMapper();

        String transactionId = "123456789";
        CacheDTO cacheDTO = requestBuilderService.createCache(objectMapper.writeValueAsString(payloadDTO.getData()), "RECEIVED");
        requestBuilderService.saveCache(cacheDTO, "initial-" + transactionId);

        Optional<RegistryTransactionsEntity> entityOptional = registryTransactionsRepository.getByTransactionId(transactionId);
        if (entityOptional.isEmpty()) {
            RegistryTransactionsEntity entity = new RegistryTransactionsEntity();
            entity.setTransactionId(transactionId);
            registryTransactionsRepository.save(entity);
        }

        List<QueryDTO> queryDTOList = createQuery(objectMapper.writeValueAsString(payloadDTO));
        for (QueryDTO queryDTO : queryDTOList) {
            SearchCriteriaDTO searchCriteriaDTO = getSearchCriteriaDTO(queryDTO, queryDTO.getQueryName());

            requestString = requestBuilderService.buildRequest(searchCriteriaDTO, transactionId);
            if (queryDTO.getQueryName().equals(Constants.PAID_FARMER)) {
                requestBuilderService.sendRequest(requestString, farmerSearchURL);

                cacheDTO = requestBuilderService.createCache(requestString, Constants.PENDING);
                requestBuilderService.saveCache(cacheDTO, Constants.FARMER_CACHE_STRING + transactionId);
            } else if (queryDTO.getQueryName().equals(Constants.IS_REGISTERED)) {
                requestBuilderService.sendRequest(requestString, mobileSearchURL);

                cacheDTO = requestBuilderService.createCache(requestString, Constants.PENDING);
                requestBuilderService.saveCache(cacheDTO, Constants.MOBILE_CACHE_STRING + transactionId);
            }
        }
        AcknowledgementDTO acknowledgementDTO = new AcknowledgementDTO();
        acknowledgementDTO.setMessage(Constants.SEARCH_REQUEST_RECEIVED);
        acknowledgementDTO.setStatus("RECEIVED");
        return acknowledgementDTO;
    }

    @Override
    public SearchCriteriaDTO getSearchCriteriaDTO(QueryDTO queryDTO, String regType) {
        PaginationDTO paginationDTO = new PaginationDTO(10, 1);
        ConsentDTO consentDTO = new ConsentDTO();
        AuthorizeDTO authorizeDTO = new AuthorizeDTO();
        List<SortDTO> sortDTOList = new ArrayList<>();
        SortDTO sortDTO = new SortDTO();
        if (queryDTO.getQueryName().equals("paid_farmer")) {
            sortDTO.setAttributeName("farmer_id");
            sortDTO.setSortOrder(SortOrderEnum.ASC.toValue());
        } else if (queryDTO.getQueryName().equals("is_registered")) {
            sortDTO.setAttributeName("mobile_number");
            sortDTO.setSortOrder(SortOrderEnum.ASC.toValue());
        }
        sortDTOList.add(sortDTO);

        SearchCriteriaDTO searchCriteriaDTO = new SearchCriteriaDTO();
        searchCriteriaDTO.setVersion("1.0.0");
        if (queryDTO.getQueryName().equals("paid_farmer")) {
            searchCriteriaDTO.setRegType("ns:FARMER_REGISTRY");
        } else if (queryDTO.getQueryName().equals("is_registered")) {
            searchCriteriaDTO.setRegType("ns:MOBILE_REGISTRY");
        }
        searchCriteriaDTO.setRegSubType("");
        searchCriteriaDTO.setQueryType("namedQuery");
        searchCriteriaDTO.setQuery(queryDTO);
        searchCriteriaDTO.setSort(sortDTOList);
        searchCriteriaDTO.setPagination(paginationDTO);
        searchCriteriaDTO.setConsent(consentDTO);
        searchCriteriaDTO.setAuthorize(authorizeDTO);
        return searchCriteriaDTO;
    }
}
