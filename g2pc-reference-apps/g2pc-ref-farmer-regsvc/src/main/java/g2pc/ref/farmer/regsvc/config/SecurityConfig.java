package g2pc.ref.farmer.regsvc.config;

import g2pc.core.lib.security.G2pTokenVerification;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
public class SecurityConfig {
    @Bean
    public G2pTokenVerification JWTTokenFilter() {
        return new G2pTokenVerification();
    }

    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeRequests(authorizeRequests ->
                        authorizeRequests
                                .anyRequest().permitAll()
                                .and()
                                .addFilterBefore(JWTTokenFilter(), BasicAuthenticationFilter.class)
                )
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf().disable();

        return http.build();
    }
}