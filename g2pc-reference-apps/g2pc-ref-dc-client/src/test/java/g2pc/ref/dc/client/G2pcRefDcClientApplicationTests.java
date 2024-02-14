package g2pc.ref.dc.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.config.G2pUnirestHelper;
import g2pc.dc.core.lib.dto.ResponseDataDto;
import g2pc.dc.core.lib.dto.ResponseTrackerDto;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
class G2pcRefDcClientApplicationTests {

    @Autowired
    G2pUnirestHelper g2pUnirestHelper;

    @Test
    void contextLoads() {
    }


    @Test
    void saveSunbirdResponseEntity() throws JsonProcessingException {
        String response_tracker_uri = "http://localhost:8081/api/v1/Response_Tracker";
        ObjectMapper objectMapper = new ObjectMapper();
        ResponseTrackerDto responseTrackerDto = new ResponseTrackerDto();
        responseTrackerDto.setVersion("version");
        responseTrackerDto.setMessageId("messageID");
        responseTrackerDto.setMessageTs("messageTs");
        responseTrackerDto.setAction("action");
        responseTrackerDto.setSenderId("senderId");
        responseTrackerDto.setReceiverId("receiverID");
        responseTrackerDto.setIsMsgEncrypted(true);
        responseTrackerDto.setTransactionId("TransactionID");
        responseTrackerDto.setRegistryType("regType");
        responseTrackerDto.setProtocol("protocol");
        responseTrackerDto.setPayloadFilename("payloadFilename");
        responseTrackerDto.setInboundFilename("inboundFilename");
        ResponseDataDto responseDataDto = new ResponseDataDto();
        responseDataDto.setReferenceId("searchRequestDTO.getReferenceId()");
        responseDataDto.setTimestamp("searchRequestDTO.getTimestamp()");
        responseDataDto.setVersion("version");
        responseDataDto.setRegType("regtpe");
        responseDataDto.setRegSubType("regsubtype");
        responseDataDto.setStatus("status");
         responseDataDto.setStatusReasonCode("g2pcerrocode");
            String responseTrackerString = objectMapper.writeValueAsString(responseTrackerDto);
        HttpResponse<JsonNode> response = Unirest.post(response_tracker_uri)
                .header("Content-Type", "application/json")
                .body(responseTrackerString)
                .asJson();;


        log.info(response+"");


    }
}
