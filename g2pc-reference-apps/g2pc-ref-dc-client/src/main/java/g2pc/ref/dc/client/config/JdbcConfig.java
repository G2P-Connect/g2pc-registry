package g2pc.ref.dc.client.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Service
public class JdbcConfig{

   @Value("${spring.second-datasource.url}")
    private String url;

    @Value("${spring.second-datasource.username}")
    private String username;

    @Value("${spring.second-datasource.password}")
    private String password;

    @Value("${spring.second-datasource.driverClassName}")
    private String driverClassName;

    public JdbcTemplate  getJdbcTemplate() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return new JdbcTemplate(dataSource);
    }
}