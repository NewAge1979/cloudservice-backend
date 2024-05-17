package pl.chrapatij.backend.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import pl.chrapatij.backend.entity.Role;
import pl.chrapatij.backend.entity.User;
import pl.chrapatij.backend.repository.UserRepository;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceImplTest {
    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private UserRepository userRepository;
    private final String LOGIN = "test@cloud.ru";
    private final String PASSWORD = "test";
    private final String LOGIN_ERROR = "test2@gloud.ru";
    private final Set<Role> roles = Set.of(Role.builder().code("ROLE_TEST").build());
    private final User user = User.builder().login(LOGIN).password(PASSWORD).roles(roles).build();

    @Test
    void findByLogin() {
        when(userRepository.findByLogin(LOGIN)).thenReturn(Optional.of(user));
        User testUser = userService.findByLogin(LOGIN);
        assertEquals(user, testUser);
    }

    @Test
    void findByLoginError() {
        when(userRepository.findByLogin(LOGIN_ERROR)).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userService.findByLogin(LOGIN_ERROR));
    }

    @Test
    void userDetailsService() {
        given(userRepository.findByLogin(LOGIN)).willReturn(Optional.of(user));
        UserDetailsService userDetailsService = userService.userDetailsService();
        assertEquals(user, userDetailsService.loadUserByUsername(LOGIN));
    }

    @Test
    void userDetailsServiceError() {
        given(userRepository.findByLogin(LOGIN_ERROR)).willReturn(Optional.empty());
        UserDetailsService userDetailsService = userService.userDetailsService();
        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(LOGIN_ERROR));
    }
}