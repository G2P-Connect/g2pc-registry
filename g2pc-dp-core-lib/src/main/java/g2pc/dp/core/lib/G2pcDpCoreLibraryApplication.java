package g2pc.dp.core.lib;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"g2pc.core.lib","g2pc.dp.core.lib","g2pc.dp.core.lib.serviceimpl",
				"g2pc.dp.core.lib.repository"})
public class G2pcDpCoreLibraryApplication {

	public static void main(String[] args) {
		SpringApplication.run(G2pcDpCoreLibraryApplication.class, args);
	}

}
