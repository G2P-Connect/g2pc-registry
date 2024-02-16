package g2pc.ref.mno.regsvc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@ComponentScan({"g2pc.core.lib",
        "g2pc.dp.core.lib",
        "g2pc.ref.mno.regsvc",
        "g2pc.dp.core.lib.service",
        "g2pc.dp.core.lib.repository",
        "g2pc.core.lib.security.serviceImpl"})
@EnableScheduling
public class G2pcRefMnoRegsvcApplication {

    public static void main(String[] args) {
        SpringApplication.run(G2pcRefMnoRegsvcApplication.class, args);
    }

}
