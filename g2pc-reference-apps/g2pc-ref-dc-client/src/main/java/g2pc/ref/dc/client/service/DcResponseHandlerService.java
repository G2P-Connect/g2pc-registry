package g2pc.ref.dc.client.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import g2pc.core.lib.dto.common.AcknowledgementDTO;
import g2pc.core.lib.dto.common.message.response.ResponseDTO;
import g2pc.core.lib.dto.common.message.response.SearchResponseDTO;

public interface DcResponseHandlerService {

    void updateTransactionDbAndCache(String transactionId, SearchResponseDTO searchResponseDTO) throws JsonProcessingException;

    AcknowledgementDTO getResponse(ResponseDTO responseDTO) throws JsonProcessingException;
}
