package g2pc.core.lib.security.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import g2pc.core.lib.dto.search.message.request.*;
import g2pc.core.lib.exceptions.G2pcValidationException;

import java.util.List;
import java.util.Map;

public interface G2pcUtilityClass {

    SearchCriteriaDTO getSearchCriteriaDTO(Map<String, Object> queryParamsMap, Map<String, Object> registrySpecificConfigMap,
                                           List<SortDTO> sortDTOList, RequestPaginationDTO paginationDTO, ConsentDTO consentDTO, AuthorizeDTO authorizeDTO) ;

    public void validateResponse(String inputString , String inputType) throws G2pcValidationException, JsonProcessingException;
    }
