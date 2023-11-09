package g2pc.ref.dc.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"g2pc.core.lib","g2pc.dc.core.lib","g2pc.ref.dc.client"})
public class G2pcRefDcClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(G2pcRefDcClientApplication.class, args);
	}

}
