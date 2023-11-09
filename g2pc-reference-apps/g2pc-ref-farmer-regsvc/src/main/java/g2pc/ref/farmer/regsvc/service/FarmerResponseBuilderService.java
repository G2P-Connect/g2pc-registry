package g2pc.ref.farmer.regsvc.service;

import g2pc.core.lib.dto.common.message.response.DataDTO;
import g2pc.ref.farmer.regsvc.dto.response.RegRecordFarmerDTO;
import g2pc.ref.farmer.regsvc.entity.FarmerInfoEntity;

import java.io.IOException;
import java.util.List;

public interface FarmerResponseBuilderService {

    List<RegRecordFarmerDTO> getRegRecordFarmerDTO(List<FarmerInfoEntity> farmerInfoList);

    String getRegFarmerRecords(String messageString) throws IOException;

    DataDTO buildData(String regRecordsString) throws IOException;
}
