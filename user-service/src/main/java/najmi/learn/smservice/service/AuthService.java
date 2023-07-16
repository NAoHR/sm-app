package najmi.learn.smservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import najmi.learn.smservice.entity.TokenEntity;
import najmi.learn.smservice.entity.UserEntity;
import najmi.learn.smservice.model.AuthenticationResponse;
import najmi.learn.smservice.model.BaseResponse;
import najmi.learn.smservice.model.LoginRequest;
import najmi.learn.smservice.model.RegisterRequest;
import najmi.learn.smservice.repo.TokenRepo;
import najmi.learn.smservice.repo.UserEntityRepository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserEntityRepository userEntityRepository;
    private final TokenRepo tokenRepo;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RedisTemplate<String, String> redisTemplate;
    private static String access = "ACCESS";
    private static String refresh = "REFRESH";
    public BaseResponse<Object> register(RegisterRequest request) {
        if(userEntityRepository.existsByEmail(request.getEmail())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email Already Exists");

        var user = UserEntity
                .builder()
                .id(UUID.randomUUID())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userEntityRepository.save(user);

        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        saveUserToken(token, user, access);
        saveUserToken(refreshToken, user, refresh);

        redisTemplate.opsForValue().set(String.format("user:%s:token", user.getId().toString()), token, Duration.ofDays(1L));

        return BaseResponse.builder()
                .ok(true)
                .error(null)
                .data(AuthenticationResponse.builder()
                        .accessToken(token)
                        .refreshToken(refreshToken)
                        .build())
                .message("Register Success")
                .build();

    }

    public BaseResponse<Object> login(LoginRequest request) {
        try{
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    request.getUsername(), request.getPassword()
            ));
        } catch (AuthenticationException e){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username or password wrong");
        }

        UserEntity user = userEntityRepository.findByEmail(request.getUsername()).orElseThrow(() -> new UsernameNotFoundException("ga ada bre"));
        revokeAllUserToken(user, access);
        revokeAllUserToken(user, refresh);

        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        saveUserToken(token, user, access);
        saveUserToken(refreshToken, user, refresh);

        redisTemplate.opsForValue().set(String.format("user:%s:token", user.getId().toString()), token, Duration.ofDays(1L));

        return BaseResponse.builder()
                .ok(true)
                .error(null)
                .data(AuthenticationResponse.builder()
                        .accessToken(token)
                        .refreshToken(refreshToken)
                        .build())
                .message("Login Success")
                .build();
    }


    public BaseResponse<Object> refresh(HttpServletRequest request){
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        String refreshToken;
        String username;
        if(authorization == null || !authorization.startsWith("Bearer ")) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login dulu bre");

        refreshToken = authorization.substring(7);
        username = jwtService.extraxtUsername(refreshToken);

        if(username != null){
            UserEntity user = userEntityRepository.findByEmail(username).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login dulu bre"));
            if(jwtService.isTokenValid(refreshToken, user)){

                String accessToken = jwtService.generateToken(user);

                revokeAllUserToken(user, access);
                saveUserToken(accessToken, user, access);

                return BaseResponse.builder()
                        .message("Refreshed")
                        .data(
                                AuthenticationResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .build()
                        )
                        .error(null)
                        .build();
            }
        }
        log.info("user name found null: potential secret key breach");
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login dulu bre");
    }

    private void saveUserToken(String token, UserEntity user, String type){
        tokenRepo.save(TokenEntity.builder()
                .token(token)
                .user(user)
                .expired(false)
                .revoked(false)
                .tokenType(type)
                .build());
    }

    private void revokeAllUserToken(UserEntity user, String type){
        List<TokenEntity> userTokens = tokenRepo.findAllValidTokenByUserAndType(user.getId(),type);
        if(!userTokens.isEmpty()){
            userTokens.forEach(token -> {
                token.setRevoked(true);
                token.setExpired(true);
            });

            tokenRepo.saveAll(userTokens);
        }

    }
}
