package g2pc.dp.core.lib.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import g2pc.core.lib.dto.common.AcknowledgementDTO;
import g2pc.core.lib.dto.common.header.RequestHeaderDTO;
import g2pc.core.lib.dto.common.message.request.RequestMessageDTO;
import g2pc.core.lib.exceptions.G2pcValidationException;

public interface RequestHandlerService {

    AcknowledgementDTO buildCacheRequest(String requestData, String cacheKey) throws Exception;

    public void validateRequestHeader(RequestHeaderDTO headerDTO) throws G2pcValidationException, JsonProcessingException;

    public void validateRequestMessage(RequestMessageDTO messageDTO) throws G2pcValidationException, JsonProcessingException;
}
