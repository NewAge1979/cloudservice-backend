package pl.chrapatij.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import pl.chrapatij.backend.dto.UserDto;
import pl.chrapatij.backend.exception.userExceptionError400;
import pl.chrapatij.backend.model.TokenResponse;
import pl.chrapatij.backend.service.UserService;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public TokenResponse signIn(UserDto userDto) {
        log.info("Sign In: {}", userDto);

        UserDetails user;
        try {
            user = userService.userDetailsService().loadUserByUsername(userDto.getLogin());
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userDto.getLogin(), userDto.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new userExceptionError400(String.format("Incorrect password for user %s.", userDto.getLogin()));
        } catch (UsernameNotFoundException e) {
            throw new userExceptionError400(String.format("User %s not found.", userDto.getLogin()));
        }

        var token = jwtService.createToken(user);
        log.info("Created token: {}", token);
        return new TokenResponse(token);
    }

    public void signOut(HttpServletRequest request, HttpServletResponse response) {
        log.info("Sign Out");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
    }
}