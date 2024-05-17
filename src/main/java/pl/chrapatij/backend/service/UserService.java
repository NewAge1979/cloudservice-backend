package pl.chrapatij.backend.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import pl.chrapatij.backend.entity.User;

public interface UserService {
    User findByLogin(String login) throws UsernameNotFoundException;

    UserDetailsService userDetailsService() throws UsernameNotFoundException;
}