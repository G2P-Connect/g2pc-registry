package g2pc.ref.dc.client.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import g2pc.core.lib.dto.common.message.response.MessageDTO;
import g2pc.core.lib.dto.common.message.response.ResponseDTO;
import g2pc.core.lib.exceptions.G2pcValidationException;
import org.springframework.stereotype.Service;

/**
 * The interface Dc validation service.
 */
@Service
public interface DcValidationService {


    /**
     * Validate response dto.
     *
     * @param responseDTO the response dto
     * @throws G2pcValidationException the g 2 pc validation exception
     * @throws JsonProcessingException the json processing exception
     */
    public void validateResponseDto(ResponseDTO responseDTO) throws G2pcValidationException, JsonProcessingException;

    /**
     * Validate reg records.
     *
     * @param messageDTO the message dto
     * @throws G2pcValidationException the g 2 pc validation exception
     * @throws JsonProcessingException the json processing exception
     */
    public void validateRegRecords(MessageDTO messageDTO) throws G2pcValidationException, JsonProcessingException;
}
