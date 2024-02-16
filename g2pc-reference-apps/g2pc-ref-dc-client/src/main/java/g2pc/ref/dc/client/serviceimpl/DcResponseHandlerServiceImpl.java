package g2pc.ref.dc.client.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.dto.common.AcknowledgementDTO;
import g2pc.core.lib.dto.search.message.response.ResponseDTO;
import g2pc.core.lib.dto.status.message.response.StatusResponseDTO;
import g2pc.core.lib.exceptions.G2pHttpException;
import g2pc.core.lib.exceptions.G2pcError;
import g2pc.dc.core.lib.service.TxnTrackerService;
import g2pc.ref.dc.client.constants.Constants;
import g2pc.ref.dc.client.service.DcResponseHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class DcResponseHandlerServiceImpl implements DcResponseHandlerService {

    @Autowired
    private TxnTrackerService txnTrackerService;

    /**
     * @param responseDTO responseDTO
     * @return AcknowledgementDTO
     * @throws JsonProcessingException jsonProcessingException might be thrown
     */
    @Override
    public AcknowledgementDTO getResponse(ResponseDTO responseDTO, String outboundFilename, Boolean sunbirdEnabled) throws IOException, G2pHttpException {
        AcknowledgementDTO acknowledgementDTO = new AcknowledgementDTO();
        ObjectMapper objectMapper = new ObjectMapper();

        G2pcError g2pcError = txnTrackerService.updateTransactionDbAndCache(responseDTO, outboundFilename, sunbirdEnabled);
        log.info("on-search response received from registry : {}", objectMapper.writeValueAsString(responseDTO));
        log.info("on-search database updation response from sunbird - " + g2pcError.getCode());
        if (g2pcError.getCode().equals(HttpStatus.OK.toString())) {
            acknowledgementDTO.setMessage(Constants.ON_SEARCH_RESPONSE_RECEIVED.toString());
            acknowledgementDTO.setStatus(Constants.COMPLETED);

        } else {
            acknowledgementDTO.setMessage(Constants.INVALID_RESPONSE.toString());
            acknowledgementDTO.setStatus(Constants.PENDING);
            throw new G2pHttpException(g2pcError);

        }

        return acknowledgementDTO;
    }

    /**
     * @param statusResponseDTO statusResponseDTO
     * @return AcknowledgementDTO
     * @throws JsonProcessingException jsonProcessingException might be thrown
     */
    @Override
    public AcknowledgementDTO getStatusResponse(StatusResponseDTO statusResponseDTO) throws IOException, G2pHttpException {
        AcknowledgementDTO acknowledgementDTO = new AcknowledgementDTO();
        ObjectMapper objectMapper = new ObjectMapper();

        G2pcError g2pcError = txnTrackerService.updateStatusTransactionDbAndCache(statusResponseDTO);
        log.info("on-status response received from registry : {}", objectMapper.writeValueAsString(statusResponseDTO));
        log.info("on-status database updation response from sunbird - " + g2pcError.getCode());
        if (g2pcError.getCode().equals(HttpStatus.OK.toString())) {
            acknowledgementDTO.setMessage(Constants.ON_STATUS_RESPONSE_RECEIVED.toString());
            acknowledgementDTO.setStatus(Constants.COMPLETED);

        } else {
            acknowledgementDTO.setMessage(Constants.INVALID_RESPONSE.toString());
            acknowledgementDTO.setStatus(Constants.PENDING);
            throw new G2pHttpException(g2pcError);

        }
        return acknowledgementDTO;
    }
}
