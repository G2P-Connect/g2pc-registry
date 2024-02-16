package g2pc.ref.dc.client;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.scheduling.annotation.EnableScheduling;

@ComponentScan({"g2pc.ref.dc.client", "g2pc.core.lib", "g2pc.dc.core.lib", "g2pc.ref.dc.client"})
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableScheduling
public class G2pcRefDcClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(G2pcRefDcClientApplication.class, args);
    }

    @Value("${spring.second-datasource.url}")
    private String url;

    @Value("${spring.second-datasource.username}")
    private String username;

    @Value("${spring.second-datasource.password}")
    private String password;

    @Value("${spring.second-datasource.driverClassName}")
    private String driverClassName;

    JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }
}
