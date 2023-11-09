package g2pc.ref.farmer.regsvc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan({"g2pc.core.lib","g2pc.dp.core.lib","g2pc.ref.farmer.regsvc","g2pc.dp.core.lib.service"})
@EnableScheduling
public class G2pcRefFarmerRegsvcApplication {

	public static void main(String[] args) {
		SpringApplication.run(G2pcRefFarmerRegsvcApplication.class, args);
	}

}
