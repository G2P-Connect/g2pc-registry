package g2pc.ref.farmer.regsvc;

import g2pc.core.lib.security.service.G2pTokenService;
import g2pc.ref.farmer.regsvc.scheduler.Scheduler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class G2pcRefFarmerRegsvcApplicationTests {

	@Autowired
	private Scheduler scheduler;

	@Autowired
	G2pTokenService g2pTokenService ;

	@Test
	void contextLoads() {
	}

	@Test
	void testResponseScheduler() throws Exception {
		scheduler.responseScheduler();
	}

}
