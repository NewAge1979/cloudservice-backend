package pl.chrapatij.backend.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import pl.chrapatij.backend.dto.UserDto;
import pl.chrapatij.backend.entity.Role;
import pl.chrapatij.backend.entity.User;
import pl.chrapatij.backend.exception.userExceptionError400;
import pl.chrapatij.backend.model.TokenResponse;
import pl.chrapatij.backend.service.UserService;

import java.util.Set;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthenticationServiceTest {
    @InjectMocks
    private AuthenticationService authenticationService;
    @Mock
    private UserService userService;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    private final String LOGIN = "test@cloud.ru";
    private final String PASSWORD = "test";
    private final Set<Role> roles = Set.of(Role.builder().code("ROLE_TEST").build());
    private final User user = User.builder().login(LOGIN).password("$2a$12$JZYtojAxDlVDl8XTgR0Ce.1nc0IHqZS2A5zMEuk.tJAQ7aCE.0GJm").roles(roles).build();

    @Test
    void signIn() {
        final UserDto userDto = UserDto.builder().login(LOGIN).password(PASSWORD).build();
        final String TOKEN = UUID.randomUUID().toString();
        given(userService.findByLogin(LOGIN)).willReturn(user);
        given(userService.userDetailsService()).willReturn(userService::findByLogin);
        given(jwtService.createToken(user)).willReturn(TOKEN);
        assertEquals(new TokenResponse(TOKEN), authenticationService.signIn(userDto));
    }

    @Test
    void signInErrorLogin() {
        final String LOGIN_ERROR = "test2@cloud.ru";
        final UserDto userDto = UserDto.builder().login(LOGIN_ERROR).password(PASSWORD).build();
        given(userService.findByLogin(LOGIN_ERROR)).willThrow(UsernameNotFoundException.class);
        given(userService.userDetailsService()).willReturn(userService::findByLogin);
        assertThrows(userExceptionError400.class, () -> authenticationService.signIn(userDto));
    }

    @Test
    void signInErrorPassword() {
        final String PASSWORD_ERROR = "test2";
        final UserDto userDto = UserDto.builder().login(LOGIN).password(PASSWORD_ERROR).build();
        given(userService.findByLogin(LOGIN)).willReturn(user);
        given(userService.userDetailsService()).willReturn(userService::findByLogin);
        given(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userDto.getLogin(), userDto.getPassword())
        )).willThrow(UsernameNotFoundException.class);
        assertThrows(userExceptionError400.class, () -> authenticationService.signIn(userDto));
    }
}