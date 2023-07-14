package najmi.learn.smservice.service;

import lombok.RequiredArgsConstructor;
import najmi.learn.smservice.entity.TokenEntity;
import najmi.learn.smservice.entity.UserEntity;
import najmi.learn.smservice.model.AuthenticationResponse;
import najmi.learn.smservice.model.BaseResponse;
import najmi.learn.smservice.model.LoginRequest;
import najmi.learn.smservice.model.RegisterRequest;
import najmi.learn.smservice.repo.TokenRepo;
import najmi.learn.smservice.repo.UserEntityRepository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserEntityRepository userEntityRepository;
    private final TokenRepo tokenRepo;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RedisTemplate<String, String> redisTemplate;
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
        saveUserToken(token, user);
        redisTemplate.opsForValue().set(String.format("user:%s:token", user.getId().toString()), token, Duration.ofDays(1L));

        return BaseResponse.builder()
                .ok(true)
                .error(null)
                .data(new AuthenticationResponse(token))
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
        revokeAllUserToken(user);

        String token = jwtService.generateToken(user);
        saveUserToken(token, user);
        redisTemplate.opsForValue().set(String.format("user:%s:token", user.getId().toString()), token, Duration.ofDays(1L));

        return BaseResponse.builder()
                .ok(true)
                .error(null)
                .data(new AuthenticationResponse(token))
                .message("Login Success")
                .build();
    }

    private void saveUserToken(String token, UserEntity user){
        tokenRepo.save(TokenEntity.builder()
                .token(token)
                .user(user)
                .expired(false)
                .revoked(false)
                .build());
    }

    private void revokeAllUserToken(UserEntity user){
        List<TokenEntity> userTokens = tokenRepo.findAllValidTokenByUser(user.getId());
        if(!userTokens.isEmpty()){
            userTokens.forEach(token -> {
                token.setRevoked(true);
                token.setExpired(true);
            });

            tokenRepo.saveAll(userTokens);
        }

    }
}
