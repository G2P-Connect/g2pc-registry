package g2pc.dc.core.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.constants.CoreConstants;
import g2pc.core.lib.dto.common.cache.CacheDTO;
import g2pc.core.lib.dto.common.header.RequestHeaderDTO;
import g2pc.core.lib.dto.common.header.ResponseHeaderDTO;
import g2pc.core.lib.dto.search.message.response.ResponseDTO;
import g2pc.core.lib.enums.HeaderStatusENUM;
import g2pc.core.lib.exceptions.G2pcError;
import g2pc.dc.core.lib.service.TxnTrackerService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@SpringBootTest
class TxnTrackerServiceTests {

    @Autowired
    private TxnTrackerService txnTrackerService;

    @Disabled
    @Test
    void testCreateCache() throws IOException {
        String payloadMapListString = readJsonFile("payloadMap.json");
        ObjectMapper objectMapper = new ObjectMapper();
        CacheDTO result = txnTrackerService.createCache(
                objectMapper.writeValueAsString(payloadMapListString),
                HeaderStatusENUM.PDNG.toValue(), CoreConstants.SEND_PROTOCOL_HTTPS);
        assertNotNull(result);
        log.info("Result : {}", result);
    }

    @Disabled
    @Test
    void testSaveCache() throws IOException {
        String payloadMapListString = readJsonFile("payloadMap.json");
        ObjectMapper objectMapper = new ObjectMapper();
        CacheDTO cacheDTO = txnTrackerService.createCache(
                objectMapper.writeValueAsString(payloadMapListString),
                HeaderStatusENUM.PDNG.toValue(), CoreConstants.SEND_PROTOCOL_HTTPS);
        txnTrackerService.saveCache(cacheDTO, "testKey");
        log.info("Saved in cache");
    }

    @Disabled
    @Test
    void testSaveInitialTransaction() throws IOException {
        String payloadMapListString = readJsonFile("payloadMap.json");
        ObjectMapper objectMapper = new ObjectMapper();
        txnTrackerService.saveInitialTransaction(
                objectMapper.readValue(payloadMapListString, List.class),
                "testTransactionId",
                HeaderStatusENUM.PDNG.toValue(),
                CoreConstants.SEND_PROTOCOL_HTTPS);
        log.info("Saved in cache");
    }

    @Disabled
    @Test
    void testSaveRequestTransaction() throws IOException {
        String requestString = readJsonFile("request.json");
        txnTrackerService.saveRequestTransaction(
                requestString,
                "ns:FARMER_REGISTRY",
                "testTransactionId",
                CoreConstants.SEND_PROTOCOL_HTTPS);
        log.info("Saved in cache");
    }

    @Disabled
    @Test
    void testSaveRequestInDB() throws IOException {
        String requestString = readJsonFile("request.json");
        G2pcError g2pcErrorDb = txnTrackerService.saveRequestInDB(
                requestString,
                "ns:FARMER_REGISTRY",
                CoreConstants.SEND_PROTOCOL_HTTPS,
                new G2pcError(),
                "testPayloadFilename",
                "testInboundFilename",
                false);
        assertNotNull(g2pcErrorDb);
        log.info("Result : {}", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(g2pcErrorDb));
    }

    @Disabled
    @Test
    void testUpdateTransactionDbAndCache() throws IOException {
        String responseString = readJsonFile("response.json");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerSubtypes(RequestHeaderDTO.class,
                ResponseHeaderDTO.class);
        G2pcError g2pcErrorDb = txnTrackerService.updateTransactionDbAndCache(
                objectMapper.readValue(responseString, ResponseDTO.class),
                "testOutboundFilename",
                false);
        assertNotNull(g2pcErrorDb);
        log.info("Result : {}", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(g2pcErrorDb));
    }


    public String readJsonFile(String filename) throws IOException {
        File file = new File("inputfiles/" + filename);
        return new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
    }
}