package pl.chrapatij.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.chrapatij.backend.dto.UserDto;
import pl.chrapatij.backend.model.TokenResponse;
import pl.chrapatij.backend.security.AuthenticationService;

@RestController
@RequestMapping("/cloud")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    ResponseEntity<TokenResponse> signIn(@RequestBody @Valid UserDto userDto) {
        log.info("*".repeat(250));
        log.info("Endpoint: cloud/login. Method: POST.");
        return ResponseEntity.ok(authenticationService.signIn(userDto));
    }

    @PostMapping("/logout")
    ResponseEntity<Void> signOut(HttpServletRequest request, HttpServletResponse response) {
        log.info("*".repeat(250));
        log.info("Endpoint: cloud/logout");
        authenticationService.signOut(request, response);
        return ResponseEntity.ok().build();
    }
}