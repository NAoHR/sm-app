package najmi.learn.smservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import najmi.learn.smservice.entity.TokenEntity;
import najmi.learn.smservice.entity.UserEntity;
import najmi.learn.smservice.model.BaseResponse;
import najmi.learn.smservice.repo.TokenRepo;
import najmi.learn.smservice.repo.UserEntityRepository;
import najmi.learn.smservice.service.JwtService;
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
import org.springframework.http.HttpStatus;
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
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ApplicationConfig {
    private final UserEntityRepository userEntityRepository;
    private final TokenRepo tokenRepo;

    private final JwtService jwtService;
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

                if(stored != null && (!stored.isExpired() || !stored.isRevoked())){
                    revokeAllUserToken(stored.getUser().getId());
                }
            }
        };
    }

    private void revokeAllUserToken(UUID uuid){
        List<TokenEntity> userTokens = tokenRepo.findAllValidTokenByUser(uuid);
        if(!userTokens.isEmpty()){
            userTokens.forEach(token -> {
                token.setRevoked(true);
                token.setExpired(true);
            });

            tokenRepo.saveAll(userTokens);
        }

    }
}
