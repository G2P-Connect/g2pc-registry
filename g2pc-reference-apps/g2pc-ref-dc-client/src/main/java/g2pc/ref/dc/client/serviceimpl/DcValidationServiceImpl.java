package g2pc.ref.dc.client.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import g2pc.core.lib.constants.CoreConstants;
import g2pc.core.lib.constants.G2pSecurityConstants;
import g2pc.core.lib.dto.common.header.ResponseHeaderDTO;
import g2pc.core.lib.dto.common.message.response.DataDTO;
import g2pc.core.lib.dto.common.message.response.ResponseDTO;
import g2pc.core.lib.dto.common.message.response.ResponseMessageDTO;
import g2pc.core.lib.dto.common.message.response.SearchResponseDTO;
import g2pc.core.lib.enums.ExceptionsENUM;
import g2pc.core.lib.exceptions.G2pHttpException;
import g2pc.core.lib.exceptions.G2pcError;
import g2pc.core.lib.exceptions.G2pcValidationException;
import g2pc.core.lib.security.service.AsymmetricSignatureService;
import g2pc.core.lib.security.service.G2pEncryptDecrypt;
import g2pc.dc.core.lib.service.ResponseHandlerService;
import g2pc.ref.dc.client.constants.Constants;
import g2pc.ref.dc.client.service.DcValidationService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;


@Service
@Slf4j
public class DcValidationServiceImpl implements DcValidationService {

    @Autowired
    ResponseHandlerService responseHandlerService;

    @Autowired
    G2pEncryptDecrypt encryptDecrypt;


    @Autowired
    private AsymmetricSignatureService asymmetricSignatureService;

    @Value("${crypto.consumer.support_encryption}")
    private boolean isEncrypt;

    @Value("${crypto.consumer.support_signature}")
    private boolean isSign;

    /**
     * Validate response dto.
     *
     * @param responseDTO the response dto
     * @throws G2pcValidationException the g 2 pc validation exception
     * @throws JsonProcessingException the json processing exception
     */
    @Override
    public void validateResponseDto(ResponseDTO responseDTO) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String headerString = new ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(responseDTO.getHeader());
        ResponseHeaderDTO headerDTO = objectMapper.readerFor(ResponseHeaderDTO.class).
                readValue(headerString);
        responseHandlerService.validateResponseHeader(headerDTO);
        byte[] json = objectMapper.writeValueAsBytes(responseDTO.getMessage());
        ResponseMessageDTO messageDTO  =  objectMapper.readValue(json, ResponseMessageDTO.class);

        validateRegRecords(messageDTO);
        responseHandlerService.validateResponseMessage(messageDTO);
    }

    /**
     * Validate reg records.
     *
     * @param messageDTO the message dto
     * @throws G2pcValidationException the g 2 pc validation exception
     * @throws JsonProcessingException the json processing exception
     */
    @Override
    public void validateRegRecords(ResponseMessageDTO messageDTO) throws G2pcValidationException, IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        //TODO: work on commented line
        List<SearchResponseDTO> searchResponseList = messageDTO.getSearchResponse();

        for(SearchResponseDTO searchResponseDTO : searchResponseList){
            DataDTO dataDTO = searchResponseDTO.getData();
            String regRecordString = new ObjectMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(dataDTO.getRegRecords());
            log.info("MessageString -> " + regRecordString);
            InputStream schemaStream;
            if(dataDTO.getRegType().toString().equals("ns:MOBILE_REGISTRY")){
                schemaStream  = DcValidationServiceImpl.class.getClassLoader()
                        .getResourceAsStream("schema/RegRecordMobileSchema.json");
            } else {
                 schemaStream = DcValidationServiceImpl.class.getClassLoader()
                        .getResourceAsStream("schema/RegRecordFarmerSchema.json");
            }
            JsonNode jsonNodeMessage = objectMapper.readTree(regRecordString);
            JsonSchema schemaRegRecord = null;
            if (schemaStream != null) {
                schemaRegRecord = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4).
                        getSchema(schemaStream);
            }
            Set<ValidationMessage> errorMessage = schemaRegRecord.validate(jsonNodeMessage);
            List<G2pcError> errorCombinedMessage = new ArrayList<>();
            for (ValidationMessage error : errorMessage) {
                log.info("Validation errors in Reg records" + error);
                errorCombinedMessage.add(new G2pcError("", error.getMessage()));

            }
            if (errorMessage.size() > 0) {
                throw new G2pcValidationException(errorCombinedMessage);
            }
        }

    }

    /**
     * Method to validate signature and encrypted message
     * @param metaData
     * @param responseDTO
     * @return
     * @throws Exception
     */
    @Override
    public ResponseMessageDTO signatureValidation(Map<String, Object> metaData, ResponseDTO responseDTO) throws Exception {

        log.info("Is encrypted ? -> "+isEncrypt);
        log.info("Is signed ? -> "+isSign);
        ObjectMapper objectMapper = new ObjectMapper();
        ResponseMessageDTO messageDTO;
        if(isSign){
            if(!metaData.get(CoreConstants.IS_SIGN).equals(true)){
                throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(), Constants.CONFIGURATION_MISMATCH_ERROR));
            }
            if(isEncrypt){
                if(!responseDTO.getHeader().getIsMsgEncrypted()){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(), Constants.CONFIGURATION_MISMATCH_ERROR));
                }

                String responseHeaderString = objectMapper.writeValueAsString(responseDTO.getHeader());
                String responseSignature = responseDTO.getSignature();
                String messageString = responseDTO.getMessage().toString();
                String data = responseHeaderString+messageString;
                if(! asymmetricSignatureService.verifySignature(data.getBytes(), Base64.getDecoder().decode(responseSignature)) ){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_SIGNATURE_INVALID.toValue(), "signature is not valid "));
                }
                if(responseDTO.getHeader().getIsMsgEncrypted()){
                    String deprecatedMessageString;
                    try{
                        deprecatedMessageString= encryptDecrypt.g2pDecrypt(messageString, G2pSecurityConstants.SECRET_KEY);
                    } catch (RuntimeException e ){
                        throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_ENCRYPTION_INVALID.toValue(), "Error in Encryption/Decryption"));
                    }
                    log.info("Decrypted Message string ->"+deprecatedMessageString);
                    messageDTO  = objectMapper.readerFor(ResponseMessageDTO.class).
                            readValue(deprecatedMessageString);
                } else {
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(), Constants.CONFIGURATION_MISMATCH_ERROR));
                }
            }else{
                if(responseDTO.getHeader().getIsMsgEncrypted()){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(),Constants.CONFIGURATION_MISMATCH_ERROR));
                }
                byte[] json = objectMapper.writeValueAsBytes(responseDTO.getMessage());
                messageDTO =  objectMapper.readValue(json, ResponseMessageDTO.class);
                String responseHeaderString = objectMapper.writeValueAsString(responseDTO.getHeader());
                String responseSignature = responseDTO.getSignature();
                String messageString = objectMapper.writeValueAsString(messageDTO);
                String data = responseHeaderString+messageString;
                log.info("Signature ->"+responseSignature);
                if(! asymmetricSignatureService.verifySignature(data.getBytes(), Base64.getDecoder().decode(responseSignature)) ){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_SIGNATURE_INVALID.toValue(), "signature is not valid "));
                }

            }
        } else {
            if(!metaData.get(CoreConstants.IS_SIGN).equals(false)){
                throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(),Constants.CONFIGURATION_MISMATCH_ERROR));
            }
            if(isEncrypt){
                if(!responseDTO.getHeader().getIsMsgEncrypted()){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(),Constants.CONFIGURATION_MISMATCH_ERROR));
                }
                String messageString = responseDTO.getMessage().toString();
                String deprecatedMessageString;
                try{
                    deprecatedMessageString= encryptDecrypt.g2pDecrypt(messageString,G2pSecurityConstants.SECRET_KEY);
                } catch (RuntimeException e ){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_ENCRYPTION_INVALID.toValue(),"Error in Encryption/Decryption"));
                }
                log.info("Decrypted Message string ->"+deprecatedMessageString);
                messageDTO  = objectMapper.readerFor(ResponseMessageDTO.class).
                        readValue(deprecatedMessageString);

            }else{
                if(responseDTO.getHeader().getIsMsgEncrypted()){
                    throw new G2pHttpException(new G2pcError(ExceptionsENUM.ERROR_VERSION_NOT_VALID.toValue(),Constants.CONFIGURATION_MISMATCH_ERROR));
                }
                byte[] json = objectMapper.writeValueAsBytes(responseDTO.getMessage());
                messageDTO =  objectMapper.readValue(json, ResponseMessageDTO.class);
            }
        }
        return messageDTO;
    }
}

