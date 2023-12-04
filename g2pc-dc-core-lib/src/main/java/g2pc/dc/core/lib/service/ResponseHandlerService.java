package g2pc.dc.core.lib.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import g2pc.core.lib.dto.common.header.ResponseHeaderDTO;
import g2pc.core.lib.dto.common.message.response.ResponseMessageDTO;
import g2pc.core.lib.exceptions.G2pcValidationException;

/**
 * The interface Response handler service.
 */
public interface ResponseHandlerService {

    /**
     * Update cache.
     *
     * @param cacheKey the cache key
     * @throws JsonProcessingException the json processing exception
     */
    void updateCache(String cacheKey) throws JsonProcessingException;

    /**
     * Validate response header.
     *
     * @param headerDTO the header dto
     * @throws G2pcValidationException the g 2 pc validation exception
     * @throws JsonProcessingException the json processing exception
     */
    public void validateResponseHeader(ResponseHeaderDTO headerDTO) throws G2pcValidationException, JsonProcessingException;

    /**
     * Validate response message.
     *
     * @param messageDTO the message dto
     * @throws G2pcValidationException the g 2 pc validation exception
     * @throws JsonProcessingException the json processing exception
     */
    public void validateResponseMessage( ResponseMessageDTO messageDTO) throws G2pcValidationException, JsonProcessingException;

}
