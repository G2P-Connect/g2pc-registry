package g2pc.ref.mno.regsvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import g2pc.ref.mno.regsvc.scheduler.Scheduler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class G2pcRefMnoRegsvcApplicationTests {

	@Autowired
	private Scheduler scheduler;

	@Test
	void contextLoads() {
	}

	@Test
	void testResponseScheduler() throws IOException {
		scheduler.responseScheduler();
	}



}
