package g2pc.ref.farmer.regsvc.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.dto.search.message.request.QueryDTO;
import g2pc.core.lib.service.ElasticsearchService;
import g2pc.ref.farmer.regsvc.dto.request.QueryParamsFarmerDTO;
import g2pc.ref.farmer.regsvc.dto.response.RegRecordFarmerDTO;
import g2pc.ref.farmer.regsvc.entity.FarmerInfoEntity;
import g2pc.ref.farmer.regsvc.repository.FarmerInfoRepository;
import g2pc.ref.farmer.regsvc.service.FarmerResponseBuilderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
@Slf4j
public class FarmerResponseBuilderServiceImpl implements FarmerResponseBuilderService {

    @Value("${sunbird.enabled}")
    private Boolean sunbirdEnabled;

    @Autowired
    private FarmerInfoRepository farmerInfoRepository;

    @Autowired
    private ElasticsearchService elasticsearchService;

    /**
     * Get farmer records information from DB
     *
     * @param farmerInfoEntity required
     * @return Farmer records
     */
    @Override
    public RegRecordFarmerDTO getRegRecordFarmerDTOFromDb(FarmerInfoEntity farmerInfoEntity) {
        RegRecordFarmerDTO dto = new RegRecordFarmerDTO();
        dto.setFarmerId(farmerInfoEntity.getFarmerId());
        dto.setFarmerName(farmerInfoEntity.getFarmerName());
        dto.setSeason(farmerInfoEntity.getSeason());
        dto.setPaymentStatus(farmerInfoEntity.getPaymentStatus());
        dto.setPaymentDate(farmerInfoEntity.getPaymentDate());
        dto.setPaymentAmount(farmerInfoEntity.getPaymentAmount());
        return dto;
    }

    /**
     * Get farmer records information from Sunbird
     *
     * @param searchResponse required
     * @return Farmer records
     */
    @Override
    public RegRecordFarmerDTO getRegRecordFarmerDTOFromSunbird(SearchResponse searchResponse) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String sourceAsString = searchResponse.getHits().getHits()[0].getSourceAsString();
        return objectMapper.readValue(sourceAsString, RegRecordFarmerDTO.class);
    }

    /**
     * Get farmer records information string
     *
     * @param queryDTOList required
     * @return List of farmer records
     */
    @Override
    public List<String> getRegFarmerRecords(List<QueryDTO> queryDTOList) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> regFarmerRecordsList = new ArrayList<>();
        for (QueryDTO queryDTO : queryDTOList) {
            String queryParams = objectMapper.writeValueAsString(queryDTO.getQueryParams());
            QueryParamsFarmerDTO queryParamsFarmerDTO = objectMapper.readValue(queryParams, QueryParamsFarmerDTO.class);
            String farmerId = queryParamsFarmerDTO.getFarmerId();
            String season = queryParamsFarmerDTO.getSeason();
            if (Boolean.TRUE.equals(sunbirdEnabled)) {
                log.info("Record fetched from sunbird");
                Map<String, String> fieldValues = new HashMap<>();
                fieldValues.put("farmer_id.keyword", farmerId);
                fieldValues.put("season.keyword", season);
                SearchResponse response = elasticsearchService.exactSearch("farmer_info", fieldValues);
                if (response.getHits().getHits().length > 0) {
                    RegRecordFarmerDTO regRecordFarmerDTO = getRegRecordFarmerDTOFromSunbird(response);
                    regFarmerRecordsList.add(objectMapper.writeValueAsString(regRecordFarmerDTO));
                } else {
                    regFarmerRecordsList.add(StringUtils.EMPTY);
                }
            } else {
                log.info("Record fetched from postgres");
                Optional<FarmerInfoEntity> optional = farmerInfoRepository.findBySeasonAndFarmerId(season, farmerId);
                if (optional.isPresent()) {
                    RegRecordFarmerDTO regRecordFarmerDTO = getRegRecordFarmerDTOFromDb(optional.get());
                    regFarmerRecordsList.add(objectMapper.writeValueAsString(regRecordFarmerDTO));
                } else {
                    regFarmerRecordsList.add(StringUtils.EMPTY);
                }
            }
        }
        return regFarmerRecordsList;
    }
}
