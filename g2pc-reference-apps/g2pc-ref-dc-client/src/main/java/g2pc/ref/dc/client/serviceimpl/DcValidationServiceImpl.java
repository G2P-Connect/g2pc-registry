package g2pc.ref.dc.client.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import g2pc.core.lib.constants.G2pSecurityConstants;
import g2pc.core.lib.dto.common.header.ResponseHeaderDTO;
import g2pc.core.lib.dto.common.message.response.DataDTO;
import g2pc.core.lib.dto.common.message.response.ResponseDTO;
import g2pc.core.lib.dto.common.message.response.ResponseMessageDTO;
import g2pc.core.lib.exceptions.G2pcError;
import g2pc.core.lib.exceptions.G2pcValidationException;
import g2pc.core.lib.security.service.G2pEncryptDecrypt;
import g2pc.dc.core.lib.service.ResponseHandlerService;
import g2pc.ref.dc.client.service.DcValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.xml.crypto.Data;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Service
@Slf4j
public class DcValidationServiceImpl implements DcValidationService {

    @Autowired
    ResponseHandlerService responseHandlerService;

    @Autowired
    G2pEncryptDecrypt encryptDecrypt;

    @Value("${crypto.support_encryption}")
    private String isEncrypt;

    @Value("${crypto.support_signature}")
    private String isSign;

    @Override
    public void validateResponseDto(ResponseDTO responseDTO) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String headerString = new ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(responseDTO.getHeader());
        ResponseHeaderDTO headerDTO = objectMapper.readerFor(ResponseHeaderDTO.class).
                readValue(headerString);
        responseHandlerService.validateResponseHeader(headerDTO);
        ResponseMessageDTO messageDTO = null;
        if (isEncrypt.equals("true")) {
            String messageString = objectMapper.convertValue(responseDTO.getMessage(), String.class);
            String deprecatedMessageString = encryptDecrypt.g2pDecrypt(messageString, G2pSecurityConstants.SECRET_KEY);
            messageDTO = objectMapper.readerFor(ResponseMessageDTO.class).
                    readValue(deprecatedMessageString);
        } else {
            messageDTO = objectMapper.convertValue(responseDTO.getMessage(), ResponseMessageDTO.class);
        }
        validateRegRecords(messageDTO);
        responseHandlerService.validateResponseMessage(messageDTO);
    }

    @Override
    public void validateRegRecords(ResponseMessageDTO messageDTO) throws G2pcValidationException, JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        //TODO: work on commented line
        //DataDTO dataDTO =  messageDTO.getSearchResponse().getData();
        DataDTO dataDTO = new DataDTO();
        String regRecordString = new ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(dataDTO.getRegRecords());
        log.info("MessageString -> " + regRecordString);
        InputStream schemaStream = DcValidationServiceImpl.class.getClassLoader()
                .getResourceAsStream("schema/RegRecordFarmerSchema.json");
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

