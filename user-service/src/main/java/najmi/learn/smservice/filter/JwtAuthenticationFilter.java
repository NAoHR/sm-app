package najmi.learn.smservice.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import najmi.learn.smservice.entity.TokenEntity;
import najmi.learn.smservice.entity.UserEntity;
import najmi.learn.smservice.model.BaseResponse;
import najmi.learn.smservice.repo.TokenRepo;
import najmi.learn.smservice.service.JwtService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.lang.model.type.NullType;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenRepo tokenRepo;

    private final RedisTemplate<String, String> template;

    private void loginAgain(@NonNull HttpServletResponse response){
        try{
            BaseResponse<Object> body = BaseResponse.builder()
                    .ok(false)
                    .data(null)
                    .error("Session Expired, please login agai")
                    .message("Unauthorized")
                    .build();
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write(new ObjectMapper().writeValueAsString(body));
        }catch (IOException e){
            log.error("ada error {}", e);
        }
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authorization = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if(authorization == null || !authorization.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authorization.substring(7);
        try{
            userEmail = jwtService.extraxtUsername(jwt);
        }catch (Exception e){
            loginAgain(response);
            return;
        }

        if(userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null){
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
            TokenEntity tokenDb = tokenRepo.findByToken(jwt).orElse(null);

            if(tokenDb == null){
                filterChain.doFilter(request, response);
                return;
            }

            if(tokenDb.isExpired() || tokenDb.isRevoked()){
                filterChain.doFilter(request, response);
                return;
            }
            if(template.opsForValue().get(String.format("user:%s:token", ((UserEntity) userDetails).getId() )) == null){
                template.opsForValue().getAndDelete(String.format("user:%s:token", ((UserEntity) userDetails).getId() ));
                tokenDb.setExpired(true);
                tokenDb.setRevoked(true);
                tokenRepo.save(tokenDb);
                filterChain.doFilter(request, response);
                return;
            }

            if(jwtService.isTokenValid(jwt, userDetails)){
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
                request.setAttribute("X-api-user-id", ((UserEntity) userDetails).getId());
            }
        }

        filterChain.doFilter(request, response);
    }
}
