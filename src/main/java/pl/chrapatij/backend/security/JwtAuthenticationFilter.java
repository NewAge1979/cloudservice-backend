package pl.chrapatij.backend.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import pl.chrapatij.backend.exception.userExceptionError400;
import pl.chrapatij.backend.exception.userExceptionError401;
import pl.chrapatij.backend.exception.userExceptionError500;
import pl.chrapatij.backend.service.UserService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    public static final String AUTHORIZATION_HEADER = "auth-token";
    public static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserService userService;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain
    ) throws ServletException, IOException {
        var authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.isNotEmpty(authHeader) && StringUtils.startsWith(authHeader, BEARER_PREFIX)) {
            log.debug("*".repeat(250));
            log.debug("Authorized request");
            try {
                var token = authHeader.substring(BEARER_PREFIX.length());
                var login = jwtService.getLogin(token);

                if (StringUtils.isNotEmpty(login) && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userService.userDetailsService().loadUserByUsername(login);
                    if (jwtService.tokenIsValid(token, userDetails)) {
                        SecurityContext context = SecurityContextHolder.createEmptyContext();
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                login, null, userDetails.getAuthorities()
                        );
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        context.setAuthentication(authentication);
                        SecurityContextHolder.setContext(context);
                    }
                }
                filterChain.doFilter(request, response);
            } catch(userExceptionError400 | userExceptionError401 | userExceptionError500 e) {
                handlerExceptionResolver.resolveException(request, response, null, e);
            } catch (JwtException e) {
                handlerExceptionResolver.resolveException(request, response, null, new userExceptionError500(e.getMessage()));
            }
        } else {
            // Авторизация
            log.debug("*".repeat(250));
            log.debug("Unauthorized request");
            filterChain.doFilter(request, response);
        }
    }
}