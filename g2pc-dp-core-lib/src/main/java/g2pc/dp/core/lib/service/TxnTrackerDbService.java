package g2pc.dp.core.lib.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import g2pc.core.lib.dto.common.message.request.QueryDTO;
import g2pc.core.lib.dto.common.message.request.RequestDTO;
import g2pc.core.lib.dto.common.message.response.DataDTO;
import g2pc.core.lib.dto.common.message.response.SearchResponseDTO;
import g2pc.dp.core.lib.entity.MsgTrackerEntity;
import g2pc.dp.core.lib.entity.TxnTrackerEntity;

import java.io.IOException;
import java.util.List;

public interface TxnTrackerDbService {

    MsgTrackerEntity saveRequestDetails(RequestDTO requestDTO) throws JsonProcessingException ;

    int getRecordCount(Object records);

    List<SearchResponseDTO> getUpdatedSearchResponseList(RequestDTO requestDTO,
                                                         List<String> refRecordsStringsList) throws IOException;

    DataDTO buildData(String regRecordsString, TxnTrackerEntity txnTrackerEntity);

    SearchResponseDTO buildSearchResponse(TxnTrackerEntity txnTrackerEntity, DataDTO dataDTO);
}
