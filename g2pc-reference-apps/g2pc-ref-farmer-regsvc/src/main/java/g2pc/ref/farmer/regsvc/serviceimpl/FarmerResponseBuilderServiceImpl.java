package g2pc.ref.farmer.regsvc.serviceimpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.dto.common.header.RequestHeaderDTO;
import g2pc.core.lib.dto.common.header.ResponseHeaderDTO;
import g2pc.core.lib.dto.common.message.request.MessageDTO;
import g2pc.core.lib.dto.common.message.response.DataDTO;
import g2pc.ref.farmer.regsvc.dto.request.QueryParamsFarmerDTO;
import g2pc.ref.farmer.regsvc.dto.response.RegRecordFarmerDTO;
import g2pc.ref.farmer.regsvc.entity.FarmerInfoEntity;
import g2pc.ref.farmer.regsvc.repository.FarmerInfoRepository;
import g2pc.ref.farmer.regsvc.service.FarmerResponseBuilderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class FarmerResponseBuilderServiceImpl implements FarmerResponseBuilderService {

    @Autowired
    private FarmerInfoRepository farmerInfoRepository;

    /**
     * Get farmer records information from DB
     *
     * @param farmerInfoList required
     * @return List of farmer records
     */
    @Override
    public List<RegRecordFarmerDTO> getRegRecordFarmerDTO(List<FarmerInfoEntity> farmerInfoList) {
        List<RegRecordFarmerDTO> regRecordFarmerDTOList = new ArrayList<>();
        for (FarmerInfoEntity farmerInfoEntity : farmerInfoList) {
            RegRecordFarmerDTO dto = new RegRecordFarmerDTO();
            dto.setFarmerId(farmerInfoEntity.getFarmerId());
            dto.setFarmerName(farmerInfoEntity.getFarmerName());
            dto.setSeason(farmerInfoEntity.getSeason());
            dto.setPaymentStatus(farmerInfoEntity.getPaymentStatus());
            dto.setPaymentDate(farmerInfoEntity.getPaymentDate());
            dto.setPaymentAmount(farmerInfoEntity.getPaymentAmount());
            regRecordFarmerDTOList.add(dto);
        }
        return regRecordFarmerDTOList;
    }

    /**
     * Get farmer records information string
     *
     * @param messageString required
     * @return String of farmer records
     */
    @Override
    public String getRegFarmerRecords(String messageString) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        //remove only use while testing
        objectMapper.registerSubtypes(RequestHeaderDTO.class,
                ResponseHeaderDTO.class);

        MessageDTO messageDTO = objectMapper.readerFor(MessageDTO.class).readValue(messageString);
        String queryParams = objectMapper.writeValueAsString(messageDTO.getSearchRequest().getSearchCriteria().getQuery().getQueryParams());
        QueryParamsFarmerDTO queryParamsFarmerDTO = objectMapper.readValue(queryParams, QueryParamsFarmerDTO.class);

        List<String> farmerIds = queryParamsFarmerDTO.getFarmerId();
        String season = queryParamsFarmerDTO.getSeason();

        List<RegRecordFarmerDTO> regRecordFarmerDTOList = new ArrayList<>();
        Optional<List<FarmerInfoEntity>> optionalList = farmerInfoRepository.
                findBySeasonAndFarmerIdIn(season, farmerIds);
        if (optionalList.isPresent()) {
            regRecordFarmerDTOList = getRegRecordFarmerDTO(optionalList.get());
        }

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(regRecordFarmerDTOList);
    }

    @Override
    public DataDTO buildData(String regRecordsString) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<RegRecordFarmerDTO> regRecordFarmerDTOList = objectMapper.readerFor(List.class).
                readValue(regRecordsString);

        DataDTO dataDTO = new DataDTO();
        dataDTO.setVersion("1.0.0");
        dataDTO.setRegType("ns:FARMER_REGISTRY");
        dataDTO.setRegSubType("");
        dataDTO.setRegRecordType("ns:FARMER_REGISTRY:FARMER");
        dataDTO.setRegRecords(regRecordFarmerDTOList);
        return dataDTO;
    }
}
