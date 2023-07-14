package najmi.learn.smservice.filter;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import najmi.learn.smservice.entity.TokenEntity;
import najmi.learn.smservice.entity.UserEntity;
import najmi.learn.smservice.repo.TokenRepo;
import najmi.learn.smservice.service.JwtService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

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
        userEmail = jwtService.extraxtUsername(jwt);
        if(userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null){
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
            TokenEntity tokenDb = tokenRepo.findByToken(jwt).orElse(null);

            if(tokenDb == null){
                filterChain.doFilter(request, response);
                return;
            }

            if(tokenDb.isExpired() || tokenDb.isRevoked()){
                System.out.println("weww");
                filterChain.doFilter(request, response);
                return;
            }
            if(template.opsForValue().get(String.format("user:%s:token", ((UserEntity) userDetails).getId() )) == null){
                System.out.println("wadidaw");
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
