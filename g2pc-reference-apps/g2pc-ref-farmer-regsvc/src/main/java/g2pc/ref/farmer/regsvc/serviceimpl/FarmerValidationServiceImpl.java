package g2pc.ref.farmer.regsvc.serviceimpl;

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
import g2pc.ref.farmer.regsvc.constants.Constants;
import g2pc.ref.farmer.regsvc.dto.request.QueryFarmerDTO;
import g2pc.ref.farmer.regsvc.dto.request.QueryParamsFarmerDTO;
import g2pc.ref.farmer.regsvc.service.FarmerValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.SignatureException;
import java.util.*;


/**
 * The type Farmer validation service.
 */
@Service
@Slf4j
public class FarmerValidationServiceImpl implements FarmerValidationService {

    /**
     * The Request handler service.
     */
    @Autowired
    RequestHandlerService requestHandlerService;

    @Value("${crypto.from_dc.support_encryption}")
    private boolean isEncrypt;

    @Value("${crypto.from_dc.support_signature}")
    private boolean isSign;

    @Value("${crypto.from_dc.password}")
    private String p12Password;

    @Autowired
    private AsymmetricSignatureService asymmetricSignatureService;

    @Autowired
    G2pEncryptDecrypt encryptDecrypt;

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${crypto.from_dc.key_path}")
    private String farmer_key_path;

    /**
     * Validate request dto.
     *
     * @param requestDTO the request dto
     * @throws G2pcValidationException     the validation exception
     * @throws JsonProcessingException the json processing exception
     */
    @Override
    public void validateRequestDTO(RequestDTO requestDTO) throws G2pcValidationException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerSubtypes(QueryDTO.class,
                QueryFarmerDTO.class, QueryParamsFarmerDTO.class);
        byte[] json = objectMapper.writeValueAsBytes(requestDTO.getMessage());
        RequestMessageDTO messageDTO =  objectMapper.readValue(json, RequestMessageDTO.class);
        List<SearchRequestDTO> searchRequestList = messageDTO.getSearchRequest();
        for(SearchRequestDTO searchRequestDTO : searchRequestList){
            String queryString = objectMapper.writeValueAsString(searchRequestDTO.getSearchCriteria().getQuery());
            QueryDTO queryFarmerDTO = objectMapper.readerFor(QueryDTO.class).
                    readValue(queryString);
            validateQueryDto(queryFarmerDTO);
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
     * Validate query dto.
     *
     * @param queryFarmerDTO the query farmer dto
     * @throws G2pcValidationException     the validation exception
     * @throws JsonProcessingException the json processing exception
     */
    @Override
    public void validateQueryDto(QueryDTO queryFarmerDTO) throws JsonProcessingException, G2pcValidationException {

        ObjectMapper objectMapper = new ObjectMapper();
        log.info("Query object -> " + queryFarmerDTO);
        String queryString = new ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(queryFarmerDTO);
        log.info("Query String" + queryString);
        InputStream schemaStreamQuery = FarmerValidationServiceImpl.class.getClassLoader()
                .getResourceAsStream("schema/farmerQuerySchema.json");
        JsonNode jsonNode = objectMapper.readTree(queryString);
        JsonSchema schema = null;
        if (schemaStreamQuery != null) {
            schema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4).
                    getSchema(schemaStreamQuery);
        }
        Set<ValidationMessage> errorMessage = schema.validate(jsonNode);
        List<G2pcError> errorcombinedMessage = new ArrayList<>();
        for (ValidationMessage error : errorMessage) {
            log.info("Validation errors" + error);
            errorcombinedMessage.add(new G2pcError("", error.getMessage()));

        }
        if (errorMessage.size() > 0) {
            throw new G2pcValidationException(errorcombinedMessage);
        }
    }

    /**
     * Method to validate signature and encrypted message
     * @param metaData
     * @param requestDTO
     * @return
     * @throws Exception
     */
    @Override
    public RequestMessageDTO signatureValidation(Map<String, Object> metaData, RequestDTO requestDTO) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        RequestMessageDTO messageDTO;
        if(isSign){
            if(!metaData.get(CoreConstants.IS_SIGN).equals(true)){
                throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(), Constants.CONFIGURATION_MISMATCH_ERROR));
            }
            Resource resource = resourceLoader.getResource(farmer_key_path);
            InputStream fis = resource.getInputStream();
            if(isEncrypt){
                if(!requestDTO.getHeader().getIsMsgEncrypted()){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(),Constants.CONFIGURATION_MISMATCH_ERROR));
                }

                String requestHeaderString = objectMapper.writeValueAsString(requestDTO.getHeader());
                String requestSignature = requestDTO.getSignature();
                String messageString = requestDTO.getMessage().toString();
                String data = requestHeaderString+messageString;
                try{if(! asymmetricSignatureService.verifySignature(data.getBytes(), Base64.getDecoder().decode(requestSignature) , fis ,p12Password) ){
                    log.info("Rejecting the on-search request in farmer as signature is not valid");
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_SIGNATURE_INVALID.toValue(), "signature is not valid "));
                }}catch(SignatureException e){
                    log.info("Rejecting the on-search request in farmer as signature is not valid");
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_SIGNATURE_INVALID.toValue(), "signature is not valid ->"+e.getMessage()));
                }
                catch(IOException e){
                    log.info("Rejecting the on-search request in farmer as signature is not valid");
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_SIGNATURE_INVALID.toValue(), e.getMessage()));
                }
                if(requestDTO.getHeader().getIsMsgEncrypted()){
                    String deprecatedMessageString;
                    try{
                        deprecatedMessageString= encryptDecrypt.g2pDecrypt(messageString, G2pSecurityConstants.SECRET_KEY);
                    } catch (RuntimeException e ){
                        log.info("Rejecting the on-search request in farmer as signature is not valid");
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
                try{if(! asymmetricSignatureService.verifySignature(data.getBytes(), Base64.getDecoder().decode(requestSignature) , fis ,p12Password) ){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_SIGNATURE_INVALID.toValue(), "signature is not valid "));
                }}catch(SignatureException e){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_SIGNATURE_INVALID.toValue(), "signature is not valid "));
                }
                catch(IOException e){
                log.info("Rejecting the on-search request in farmer as signature is not valid");
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
                    log.info("Rejecting the on-search request in farmer as signature is not valid");
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
        requestDTO.setMessage(messageDTO);
        return messageDTO;
    }
}

