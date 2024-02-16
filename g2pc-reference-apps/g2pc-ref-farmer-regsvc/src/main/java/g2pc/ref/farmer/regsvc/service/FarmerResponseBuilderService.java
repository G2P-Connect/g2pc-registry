package g2pc.ref.farmer.regsvc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import g2pc.core.lib.dto.search.message.request.QueryDTO;
import g2pc.ref.farmer.regsvc.dto.response.RegRecordFarmerDTO;
import g2pc.ref.farmer.regsvc.entity.FarmerInfoEntity;
import org.elasticsearch.action.search.SearchResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface FarmerResponseBuilderService {

    RegRecordFarmerDTO getRegRecordFarmerDTOFromDb(FarmerInfoEntity farmerInfoEntity);

    RegRecordFarmerDTO getRegRecordFarmerDTOFromSunbird(SearchResponse searchResponse) throws JsonProcessingException;

    List<String> getRegFarmerRecords(List<QueryDTO> queryDTOList) throws IOException;



}
