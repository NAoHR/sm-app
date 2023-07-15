package najmi.learn.smservice.config;

import lombok.RequiredArgsConstructor;
import najmi.learn.smservice.entity.TokenEntity;
import najmi.learn.smservice.repo.TokenRepo;
import najmi.learn.smservice.repo.UserEntityRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {
    private final UserEntityRepository userEntityRepository;
    private final TokenRepo tokenRepo;
    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Bean
    public UserDetailsService userDetailsService(){
        return userEmail -> userEntityRepository.findByEmail(userEmail).orElseThrow(() -> new UsernameNotFoundException("User Not Found"));
    }

    @Bean
    public AuthenticationProvider authenticationProvider(){
        // data access object which is responsible to fetch the user detail and also encode password, dll
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception{
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JedisConnectionFactory redisConnection(){
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(redisHost);
        redisStandaloneConfiguration.setPort(redisPort);
        return new JedisConnectionFactory(redisStandaloneConfiguration);
    }
    @Bean
    @Primary
    public RedisTemplate<String, String> template(JedisConnectionFactory connection){
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connection);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());

        return template;

    }

    @Bean
    public LogoutHandler logoutService(
            ){
        return (
                @NonNull HttpServletRequest request,
                @NonNull HttpServletResponse response,
                @NonNull Authentication authentication
        ) -> {
            final String authorization = request.getHeader("Authorization");
            final String jwt;

            if(authorization != null && authorization.startsWith("Bearer ")){
                jwt = authorization.substring(7);
                TokenEntity stored = tokenRepo.findByToken(jwt).orElse(null);

                if(stored != null){
                    stored.setRevoked(true);
                    stored.setExpired(true);
                    tokenRepo.save(stored);

                }
            }
        };
    }
}
