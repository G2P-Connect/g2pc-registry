package g2pc.dc.core.lib;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.dto.common.message.request.RequestDTO;
import g2pc.core.lib.dto.common.message.request.SearchCriteriaDTO;
import g2pc.dc.core.lib.service.RequestBuilderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class G2pcDcCoreLibraryApplicationTests {

	@Autowired
	RequestBuilderService requestBuilderService;

	@Test
	void contextLoads() {
	}
}
