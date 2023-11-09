package g2pc.ref.dc.client.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import g2pc.core.lib.dto.common.header.RequestHeaderDTO;
import g2pc.core.lib.dto.common.header.ResponseHeaderDTO;
import g2pc.core.lib.dto.common.message.response.DataDTO;
import g2pc.core.lib.dto.common.message.response.MessageDTO;
import g2pc.core.lib.dto.common.message.response.ResponseDTO;
import g2pc.core.lib.exceptions.G2pcError;
import g2pc.core.lib.exceptions.G2pcValidationException;
import g2pc.dc.core.lib.service.ResponseHandlerService;
import g2pc.ref.dc.client.service.DcValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Service
@Slf4j
public class DcValidationServiceImpl implements DcValidationService {

    @Autowired
    ResponseHandlerService responseHandlerService;

    @Override
    public void validateResponseDto(ResponseDTO responseDTO) throws G2pcValidationException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String headerString = new ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(responseDTO.getHeader());
        ResponseHeaderDTO headerDTO = objectMapper.readerFor(ResponseHeaderDTO.class).
                readValue(headerString);
        responseHandlerService.validateResponseHeader(headerDTO);

        validateRegRecords(responseDTO.getMessage());

        responseHandlerService.validateResponseMessage(responseDTO.getMessage());



    }

    @Override
    public void validateRegRecords(MessageDTO messageDTO) throws G2pcValidationException, JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        DataDTO dataDTO =  messageDTO.getSearchResponse().getData();

            String regRecordString = new ObjectMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(dataDTO.getRegRecords());
            log.info("MessageString -> " + regRecordString);
            InputStream schemaStream = DcValidationServiceImpl.class.getClassLoader()
                    .getResourceAsStream("schema/RegRecordFarmerSchema.json");
            JsonNode jsonNodeMessage = objectMapper.readTree(regRecordString);
            JsonSchema schemaRegRecord = null;
            if(schemaStream !=null){
                schemaRegRecord  = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4).
                        getSchema(schemaStream);
            }
        Set<ValidationMessage> errorMessage = schemaRegRecord.validate(jsonNodeMessage);
        List<G2pcError> errorCombinedMessage= new ArrayList<>();
        for (ValidationMessage error : errorMessage){
            log.info("Validation errors" + error );
            errorCombinedMessage.add(new G2pcError("",error.getMessage()));

        }
        if (errorMessage.size()>0){
            throw new G2pcValidationException(errorCombinedMessage);
        }
        }
    }

