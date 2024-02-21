package g2pc.dc.core.lib;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"g2pc.dc.core.lib","g2pc.core.lib","g2pc.core.lib.security.service"})
public class G2pcDcCoreLibraryApplication {

	public static void main(String[] args) {
		SpringApplication.run(G2pcDcCoreLibraryApplication.class, args);
	}
}
