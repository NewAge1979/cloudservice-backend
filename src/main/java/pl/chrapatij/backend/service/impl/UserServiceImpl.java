package pl.chrapatij.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.chrapatij.backend.entity.User;
import pl.chrapatij.backend.repository.UserRepository;
import pl.chrapatij.backend.service.UserService;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User findByLogin(String login) throws UsernameNotFoundException {
        return userRepository.findByLogin(login).orElseThrow(
                () -> new UsernameNotFoundException(String.format("User %s not found.", login))
        );
    }

    @Override
    public UserDetailsService userDetailsService() throws UsernameNotFoundException {
        return this::findByLogin;
    }

}