package g2pc.ref.mno.regsvc.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import g2pc.core.lib.dto.common.header.RequestHeaderDTO;
import g2pc.core.lib.dto.common.message.request.RequestDTO;
import g2pc.core.lib.exceptions.G2pcError;
import g2pc.core.lib.exceptions.G2pcValidationException;
import g2pc.dp.core.lib.service.RequestHandlerService;
import g2pc.ref.mno.regsvc.dto.request.QueryMobileDTO;
import g2pc.ref.mno.regsvc.service.MobileValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * The type Mobile validation service.
 */
@Service
@Slf4j
public class MobileValidationServiceImpl implements MobileValidationService {

    /**
     * The Request handler service.
     */
    @Autowired
    RequestHandlerService requestHandlerService;
    @Override
    public void validateRequestDTO(RequestDTO requestDTO) throws G2pcValidationException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String queryString = new ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(requestDTO.getMessage().getSearchRequest().getSearchCriteria().getQuery());
        QueryMobileDTO queryMobileDTO = objectMapper.readerFor(QueryMobileDTO.class).
                readValue(queryString);
        validateQueryDto(queryMobileDTO);

        String headerString = new ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(requestDTO.getHeader());
        RequestHeaderDTO headerDTO = objectMapper.readerFor(RequestHeaderDTO.class).
                readValue(headerString);
        requestHandlerService.validateRequestHeader(headerDTO);
        requestHandlerService.validateRequestMessage(requestDTO.getMessage());
    }

    @Override
    public void validateQueryDto(QueryMobileDTO queryMobileDTO) throws G2pcValidationException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        log.info("Query object -> "+queryMobileDTO);
        String queryString = new ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(queryMobileDTO);
        log.info("Query String" + queryString);
        InputStream schemaStreamQuery = MobileValidationServiceImpl.class.getClassLoader()
                .getResourceAsStream("schema/mobileQuerySchema.json");
        JsonNode jsonNode = objectMapper.readTree(queryString);
        JsonSchema schema = null;
        if(schemaStreamQuery !=null){
            schema  = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4).
                    getSchema(schemaStreamQuery);
        }
        Set<ValidationMessage> errorMessage = schema.validate(jsonNode);
        List<G2pcError> errorcombinedMessage= new ArrayList<>();
        for (ValidationMessage error : errorMessage){
            log.info("Validation errors" + error );
            errorcombinedMessage.add(new G2pcError("",error.getMessage()));

        }
        if (errorMessage.size()>0){
            throw new G2pcValidationException(errorcombinedMessage);
        }
    }
}
