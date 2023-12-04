package g2pc.ref.farmer.regsvc.service;

import g2pc.core.lib.dto.common.message.request.QueryDTO;
import g2pc.core.lib.dto.common.message.response.DataDTO;
import g2pc.ref.farmer.regsvc.dto.response.RegRecordFarmerDTO;
import g2pc.ref.farmer.regsvc.entity.FarmerInfoEntity;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface FarmerResponseBuilderService {

    RegRecordFarmerDTO getRegRecordFarmerDTO(FarmerInfoEntity farmerInfoEntity);

    List<String> getRegFarmerRecords(List<QueryDTO> queryDTOList) throws IOException;
}
