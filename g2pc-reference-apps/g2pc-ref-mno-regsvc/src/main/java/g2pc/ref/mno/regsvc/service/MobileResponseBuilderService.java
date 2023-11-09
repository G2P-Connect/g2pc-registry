package g2pc.ref.mno.regsvc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import g2pc.core.lib.dto.common.message.response.DataDTO;

import java.io.IOException;

public interface MobileResponseBuilderService {

    String getRegMobileRecords(String messageString) throws JsonProcessingException;

    DataDTO buildData(String regRecordsString) throws IOException;
}
