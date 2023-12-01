package g2pc.ref.mno.regsvc.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import g2pc.core.lib.constants.G2pSecurityConstants;
import g2pc.core.lib.dto.common.header.HeaderDTO;
import g2pc.core.lib.dto.common.header.RequestHeaderDTO;
import g2pc.core.lib.dto.common.header.ResponseHeaderDTO;
import g2pc.core.lib.dto.common.message.request.MessageDTO;
import g2pc.core.lib.dto.common.message.request.QueryDTO;
import g2pc.core.lib.dto.common.message.request.RequestDTO;
import g2pc.core.lib.exceptions.G2pcError;
import g2pc.core.lib.exceptions.G2pcValidationException;
import g2pc.core.lib.security.service.G2pEncryptDecrypt;
import g2pc.dp.core.lib.service.RequestHandlerService;
import g2pc.ref.mno.regsvc.dto.request.QueryMobileDTO;
import g2pc.ref.mno.regsvc.dto.request.QueryParamsMobileDTO;
import g2pc.ref.mno.regsvc.service.MobileValidationService;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @Autowired
    G2pEncryptDecrypt encryptDecrypt;

    @Value("${crypto.support_encryption}")
    private String isEncrypt;

    @Value("${crypto.support_signature}")
    private String isSign;

    /**
     * Method to validate Request dto
     * @param requestDTO the request dto
     * @throws Exception
     */
    @Override
    public void validateRequestDTO(RequestDTO requestDTO) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerSubtypes(QueryDTO.class,
                QueryMobileDTO.class, QueryParamsMobileDTO.class);

        MessageDTO messageDTO = null;
        if(isEncrypt.equals("true")){
            String messageString = requestDTO.getMessage();
            String deprecatedMessageString = encryptDecrypt.g2pDecrypt(messageString,G2pSecurityConstants.SECRET_KEY);
            messageDTO  = objectMapper.readerFor(MessageDTO.class).
                    readValue(deprecatedMessageString);
        }else{
            messageDTO  = objectMapper.readerFor(MessageDTO.class).
                    readValue(requestDTO.getMessage());
        }

        String queryString = objectMapper.writeValueAsString(messageDTO.getSearchRequest().getSearchCriteria().getQuery());

        QueryDTO queryMobileDTO = objectMapper.readerFor(QueryDTO.class).
                readValue(queryString);
       validateQueryDto(queryMobileDTO);

        String headerString = new ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(requestDTO.getHeader());
        RequestHeaderDTO headerDTO = objectMapper.readerFor(RequestHeaderDTO.class).
                readValue(headerString);
        requestHandlerService.validateRequestHeader(headerDTO);
        requestHandlerService.validateRequestMessage(messageDTO);
    }

    /**
     * Method to validate query deto
     * @param queryMobileDTO the query mobile dto
     * @throws G2pcValidationException
     * @throws JsonProcessingException
     */
    @Override
    public void validateQueryDto(QueryDTO queryMobileDTO) throws G2pcValidationException, JsonProcessingException {
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
