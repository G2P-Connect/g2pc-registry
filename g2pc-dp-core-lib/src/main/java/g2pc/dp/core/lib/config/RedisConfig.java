package g2pc.dp.core.lib.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;


@Configuration
@Slf4j
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    String host;

    @Value("${spring.data.redis.port}")
    int port;

    @Value("${spring.data.redis.password:}")
    String password;

    public RedisConnectionFactory redisConnectionFactory() {

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(10);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);

        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(host, port);

        log.info("Redis Connection Details  Host: {}, Port: {}", host, port);

        configuration.setHostName(host);
        if (password != null && !password.trim().isEmpty()) {
            log.info("Redis Password Set: {}", password);
            configuration.setPassword(password);
        }
        configuration.setPort(port);

        JedisConnectionFactory connectionFactory = new JedisConnectionFactory(configuration);
        connectionFactory.setPoolConfig(poolConfig);

        connectionFactory.afterPropertiesSet();

        return connectionFactory;
    }

    @Bean
    RedisTemplate<String, String> redisTemplate() {
        final RedisTemplate<String, String> template = new RedisTemplate<>();
        log.info("Redis Template for String...");
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericToStringSerializer<>(String.class));
        template.setValueSerializer(new GenericToStringSerializer<>(String.class));
        return template;
    }


}