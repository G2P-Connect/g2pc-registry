package g2pc.dc.core.lib;

import com.fasterxml.jackson.core.JsonProcessingException;
import g2pc.dc.core.lib.service.ResponseHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class ResponseHandlerServiceTest {

    @Autowired
    private ResponseHandlerService responseHandlerService;

    @Test
    void testUpdateCache() throws JsonProcessingException {
        responseHandlerService.updateCache("ns:FARMER_REGISTRY-T912-3365-7126-1762-0583");
        log.info("Cache updated");
    }
}