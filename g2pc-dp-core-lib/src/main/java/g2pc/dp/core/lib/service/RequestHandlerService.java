package g2pc.dp.core.lib.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import g2pc.core.lib.dto.common.AcknowledgementDTO;

public interface RequestHandlerService {

    AcknowledgementDTO buildCacheRequest(String requestData, String cacheKey, String protocol, Boolean sunbirdEnabled) throws Exception;


    public AcknowledgementDTO buildCacheStatusRequest(String statusRequestData, String cacheKey,String protocol) throws JsonProcessingException;


}
