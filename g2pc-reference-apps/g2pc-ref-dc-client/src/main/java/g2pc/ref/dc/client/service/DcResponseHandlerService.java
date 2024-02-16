package g2pc.ref.dc.client.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import g2pc.core.lib.dto.common.AcknowledgementDTO;
import g2pc.core.lib.dto.search.message.response.ResponseDTO;
import g2pc.core.lib.dto.status.message.response.StatusResponseDTO;
import g2pc.core.lib.exceptions.G2pHttpException;

import java.io.IOException;

public interface DcResponseHandlerService {

    AcknowledgementDTO getResponse(ResponseDTO responseDTO, String outboundFilename, Boolean sunbirdEnabled) throws IOException, G2pHttpException;

    AcknowledgementDTO getStatusResponse(StatusResponseDTO statusResponseDTO) throws IOException, G2pHttpException;
}
