package g2pc.dc.core.lib.service;

import com.fasterxml.jackson.core.JsonProcessingException;
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
}
