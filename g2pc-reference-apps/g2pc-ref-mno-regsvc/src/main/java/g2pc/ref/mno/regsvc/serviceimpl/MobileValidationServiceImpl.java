package g2pc.ref.mno.regsvc.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import g2pc.core.lib.constants.CoreConstants;
import g2pc.core.lib.constants.G2pSecurityConstants;
import g2pc.core.lib.dto.common.header.RequestHeaderDTO;
import g2pc.core.lib.dto.common.message.request.QueryDTO;
import g2pc.core.lib.dto.common.message.request.RequestDTO;
import g2pc.core.lib.dto.common.message.request.RequestMessageDTO;
import g2pc.core.lib.dto.common.message.request.SearchRequestDTO;
import g2pc.core.lib.enums.ExceptionsENUM;
import g2pc.core.lib.exceptions.G2pHttpException;
import g2pc.core.lib.exceptions.G2pcError;
import g2pc.core.lib.exceptions.G2pcValidationException;
import g2pc.core.lib.security.service.AsymmetricSignatureService;
import g2pc.core.lib.security.service.G2pEncryptDecrypt;
import g2pc.dp.core.lib.service.RequestHandlerService;
import g2pc.ref.mno.regsvc.constants.Constants;
import g2pc.ref.mno.regsvc.dto.request.QueryMobileDTO;
import g2pc.ref.mno.regsvc.dto.request.QueryParamsMobileDTO;
import g2pc.ref.mno.regsvc.service.MobileValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.InputStream;
import java.util.*;
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

    @Value("${crypto.mobile.support_encryption}")
    private boolean isEncrypt;

    @Value("${crypto.mobile.support_signature}")
    private boolean isSign;

    @Autowired
    private AsymmetricSignatureService asymmetricSignatureService;

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
        byte[] json = objectMapper.writeValueAsBytes(requestDTO.getMessage());
        RequestMessageDTO messageDTO =  objectMapper.readValue(json, RequestMessageDTO.class);
        List<SearchRequestDTO> searchRequestList = messageDTO.getSearchRequest();
        for(SearchRequestDTO searchRequestDTO : searchRequestList){
            String queryString = objectMapper.writeValueAsString(searchRequestDTO.getSearchCriteria().getQuery());
            QueryDTO queryMobileDto = objectMapper.readerFor(QueryDTO.class).
                    readValue(queryString);
            validateQueryDto(queryMobileDto);
        }

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

    /**
     * Method to validate signature and encryption of request dto
     * @param metaData
     * @param requestDTO
     * @return
     * @throws Exception
     */
    @Override
    public RequestMessageDTO signatureValidation(Map<String, Object> metaData, RequestDTO requestDTO) throws Exception {
        log.info("Is encrypted ? -> " + isEncrypt);
        log.info("Is signed ? -> " + isSign);
        ObjectMapper objectMapper = new ObjectMapper();
        RequestMessageDTO messageDTO;
        if(isSign){
            if(!metaData.get(CoreConstants.IS_SIGN).equals(true)){
                throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(), Constants.CONFIGURATION_MISMATCH_ERROR));
            }
            if(isEncrypt){
                if(!requestDTO.getHeader().getIsMsgEncrypted()){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(),Constants.CONFIGURATION_MISMATCH_ERROR));
                }

                String requestHeaderString = objectMapper.writeValueAsString(requestDTO.getHeader());
                String requestSignature = requestDTO.getSignature();
                String messageString = requestDTO.getMessage().toString();
                String data = requestHeaderString+messageString;
                if(! asymmetricSignatureService.verifySignature(data.getBytes(), Base64.getDecoder().decode(requestSignature)) ){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_SIGNATURE_INVALID.toValue(), "signature is not valid "));
                }
                if(requestDTO.getHeader().getIsMsgEncrypted()){
                    String deprecatedMessageString;
                    try{
                        deprecatedMessageString= encryptDecrypt.g2pDecrypt(messageString,G2pSecurityConstants.SECRET_KEY);
                    } catch (RuntimeException e ){
                        throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_ENCRYPTION_INVALID.toValue(), "Error in Encryption/Decryption"));
                    }
                    log.info("Decrypted Message string ->"+deprecatedMessageString);
                    messageDTO  = objectMapper.readerFor(RequestMessageDTO.class).
                            readValue(deprecatedMessageString);
                } else {
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(),Constants.CONFIGURATION_MISMATCH_ERROR));
                }
            }else{
                if(requestDTO.getHeader().getIsMsgEncrypted()){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(),Constants.CONFIGURATION_MISMATCH_ERROR));
                }
                byte[] json = objectMapper.writeValueAsBytes(requestDTO.getMessage());
                messageDTO =  objectMapper.readValue(json, RequestMessageDTO.class);
                String requestHeaderString = objectMapper.writeValueAsString(requestDTO.getHeader());
                String requestSignature = requestDTO.getSignature();
                String messageString = objectMapper.writeValueAsString(messageDTO);
                String data = requestHeaderString+messageString;
                log.info("Signature ->"+requestSignature);
                if(! asymmetricSignatureService.verifySignature(data.getBytes(), Base64.getDecoder().decode(requestSignature)) ){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_SIGNATURE_INVALID.toValue(), "signature is not valid "));
                }

            }
        } else {
            if(!metaData.get(CoreConstants.IS_SIGN).equals(false)){
                throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(),Constants.CONFIGURATION_MISMATCH_ERROR));
            }
            if(isEncrypt){
                if(!requestDTO.getHeader().getIsMsgEncrypted()){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(),Constants.CONFIGURATION_MISMATCH_ERROR));
                }
                String messageString = requestDTO.getMessage().toString();
                String deprecatedMessageString;
                try{
                    deprecatedMessageString= encryptDecrypt.g2pDecrypt(messageString,G2pSecurityConstants.SECRET_KEY);
                } catch (RuntimeException e ){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_ENCRYPTION_INVALID.toValue(), "Error in Encryption/Decryption"));
                }
                log.info("Decrypted Message string ->"+deprecatedMessageString);
                messageDTO  = objectMapper.readerFor(RequestMessageDTO.class).
                        readValue(deprecatedMessageString);

            }else{
                if(requestDTO.getHeader().getIsMsgEncrypted()){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(),Constants.CONFIGURATION_MISMATCH_ERROR));
                }
                byte[] json = objectMapper.writeValueAsBytes(requestDTO.getMessage());
                messageDTO =  objectMapper.readValue(json, RequestMessageDTO.class);
            }
        }
        return messageDTO;
    }


}
