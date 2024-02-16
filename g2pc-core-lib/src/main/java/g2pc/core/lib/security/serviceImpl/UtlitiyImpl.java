package g2pc.core.lib.security.serviceImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import g2pc.core.lib.constants.CoreConstants;
import g2pc.core.lib.dto.search.message.request.*;
import g2pc.core.lib.exceptions.G2pcError;
import g2pc.core.lib.exceptions.G2pcValidationException;
import g2pc.core.lib.security.service.G2pcUtilityClass;
import g2pc.core.lib.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class UtlitiyImpl implements G2pcUtilityClass {


  @Autowired
  private CommonUtils commonUtils;
    @Override
      public SearchCriteriaDTO getSearchCriteriaDTO(Map<String, Object> queryParamsMap, Map<String, Object> registrySpecificConfigMap, List<SortDTO> sortDTOList, RequestPaginationDTO paginationDTO, ConsentDTO consentDTO, AuthorizeDTO authorizeDTO) {
        SearchCriteriaDTO searchCriteriaDTO = new SearchCriteriaDTO();
        searchCriteriaDTO.setVersion("1.0.0");
        searchCriteriaDTO.setRegType(registrySpecificConfigMap.get("reg_type").toString());
        searchCriteriaDTO.setRegSubType(registrySpecificConfigMap.get("reg_sub_type").toString());
        searchCriteriaDTO.setQueryType(registrySpecificConfigMap.get("query_type").toString());

        QueryDTO queryDTO = new QueryDTO();
        queryDTO.setQueryName(registrySpecificConfigMap.get("query_name").toString());
        queryDTO.setQueryParams(queryParamsMap.values().iterator().hasNext() ? queryParamsMap.values().iterator().next() : "");

        searchCriteriaDTO.setQuery(queryDTO);
        searchCriteriaDTO.setSort(sortDTOList);
        searchCriteriaDTO.setPagination(paginationDTO);
        searchCriteriaDTO.setConsent(consentDTO);
        searchCriteriaDTO.setAuthorize(authorizeDTO);
        return searchCriteriaDTO;
    }

  /**
   *
   * @param inputString inputString to validate
   * @param inputType type of input
   * @throws G2pcValidationException might be thrown
   * @throws JsonProcessingException might be thrown
   */
  @Override
  public void validateResponse(String inputString, String inputType) throws G2pcValidationException, JsonProcessingException {

    ObjectMapper objectMapper = new ObjectMapper();
    InputStream schemaStream = getJsonInputStream(inputType);
    JsonNode jsonNodeMessage = objectMapper.readTree(inputString);
    JsonSchema schemaMessage = null;
    if(schemaStream !=null){
      schemaMessage  = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4).
              getSchema(schemaStream);
    }
    Set<ValidationMessage> errorMessage = schemaMessage.validate(jsonNodeMessage);
    List<G2pcError> errorCombinedMessage= new ArrayList<>();
    for (ValidationMessage error : errorMessage){
      log.info("Validation errors" + error );
      errorCombinedMessage.add(new G2pcError("",error.getMessage()));
    }
    if (!errorMessage.isEmpty()){
      throw new G2pcValidationException(errorCombinedMessage);
    }
  }

  /**
   *
   * @param inputType type of input stream
   * @return
   */
  private InputStream getJsonInputStream(String inputType) {
    if(inputType.equals(CoreConstants.RESPONSE_HEADER)){
      return commonUtils.getResponseHeaderString();
    } else if (inputType.equals(CoreConstants.SEARCH_RESPONSE)){
      return commonUtils.getResponseMessageString();
    } else if (inputType.equals(CoreConstants.STATUS_RESPONSE)){
      return  commonUtils.getStatusResponseMessageString();
    }
    else if(inputType.equals(CoreConstants.REQUEST_HEADER)){
      return commonUtils.getRequestHeaderString();
    } else if (inputType.equals(CoreConstants.SEARCH_REQUEST)){
      return commonUtils.getRequestMessageString();
    } else {
      return  commonUtils.getStatusRequestMessageString();
    }
  }
}
