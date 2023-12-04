package g2pc.ref.dc.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;


@ComponentScan({"g2pc.ref.dc.client","g2pc.core.lib","g2pc.dc.core.lib","g2pc.ref.dc.client"})
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class G2pcRefDcClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(G2pcRefDcClientApplication.class, args);
	}

}
