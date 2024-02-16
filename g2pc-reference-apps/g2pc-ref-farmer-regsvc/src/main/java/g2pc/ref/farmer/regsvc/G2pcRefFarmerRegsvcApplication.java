package g2pc.ref.farmer.regsvc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
@ComponentScan({"g2pc.ref.farmer.regsvc","g2pc.core.lib","g2pc.dp.core.lib","g2pc.ref.farmer.regsvc",
		"g2pc.ref.farmer.regsvc.auth.service","g2pc.ref.farmer.regsvc.auth.serviceImpl",
		"org.springframework.security.config.annotation.web.builders.HttpSecurity",
		"g2pc.dp.core.lib.service","g2pc.ref.farmer.regsvc.auth.controller"})
@EnableScheduling
@EnableWebSecurity
public class G2pcRefFarmerRegsvcApplication {

	public static void main(String[] args) {
		SpringApplication.run(G2pcRefFarmerRegsvcApplication.class, args);
	}

}