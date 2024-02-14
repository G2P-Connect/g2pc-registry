package g2pc.dp.core.lib.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import g2pc.core.lib.dto.search.message.request.RequestDTO;
import g2pc.core.lib.dto.search.message.request.ResponseMessageDTO;
import g2pc.core.lib.dto.search.message.response.DataDTO;
import g2pc.core.lib.dto.search.message.response.SearchResponseDTO;
import g2pc.core.lib.dto.status.message.request.StatusRequestDTO;
import g2pc.dp.core.lib.entity.MsgTrackerEntity;
import g2pc.dp.core.lib.entity.TxnTrackerEntity;

import java.io.IOException;
import java.util.List;

public interface TxnTrackerDbService {

    MsgTrackerEntity saveRequestDetails(RequestDTO requestDTO, String protocol, Boolean sunbirdEnabled) throws JsonProcessingException;

    int getRecordCount(Object records);

    List<SearchResponseDTO> getUpdatedSearchResponseList(RequestDTO requestDTO,
                                                         List<String> refRecordsStringsList,
                                                         String protocol,
                                                         Boolean sunbirdEnabled) throws IOException;

    DataDTO buildData(String regRecordsString, TxnTrackerEntity txnTrackerEntity);

    SearchResponseDTO buildSearchResponse(TxnTrackerEntity txnTrackerEntity, DataDTO dataDTO);

    MsgTrackerEntity saveStatusRequestDetails(StatusRequestDTO statusRequestDTO) throws JsonProcessingException;

    void updateStatusResponseDetails(ResponseMessageDTO responseMessageDTO, String transactionId);

    void updateMessageTrackerStatusDb(String transactionId);


}
