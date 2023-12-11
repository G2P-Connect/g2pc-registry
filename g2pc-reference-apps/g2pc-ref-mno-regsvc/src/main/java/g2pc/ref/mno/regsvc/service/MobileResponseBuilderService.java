package g2pc.ref.mno.regsvc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import g2pc.core.lib.dto.common.message.request.QueryDTO;
import g2pc.core.lib.dto.common.message.response.DataDTO;

import java.io.IOException;
import java.util.List;

public interface MobileResponseBuilderService {

    List<String> getRegMobileRecords(List<QueryDTO> queryDTOList) throws IOException;
}
