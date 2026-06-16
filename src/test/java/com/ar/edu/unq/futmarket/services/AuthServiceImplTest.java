package com.ar.edu.unq.futmarket.services;

import com.ar.edu.unq.futmarket.controllers.request.LoginRequest;
import com.ar.edu.unq.futmarket.controllers.request.RegisterRequest;
import com.ar.edu.unq.futmarket.controllers.response.AuthResponse;
import com.ar.edu.unq.futmarket.exception.InvalidCredentialsException;
import com.ar.edu.unq.futmarket.exception.UsernameAlreadyExistsException;
import com.ar.edu.unq.futmarket.model.User;
import com.ar.edu.unq.futmarket.repositories.UserRepository;
import com.ar.edu.unq.futmarket.services.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class AuthServiceImplTest {

    @Autowired
    private AuthServiceImpl authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User normalUser;
    private User superUser;

    @BeforeEach
    void setUp() {
        normalUser = User.builder()
                .username("testuser")
                .balance(new BigDecimal("100.00"))
                .superuser(false)
                .build();
        normalUser.setPassword(passwordEncoder.encode("password123"));
        userRepository.save(normalUser);

        superUser = User.builder()
                .username("testsuperuser")
                .balance(BigDecimal.ZERO)
                .superuser(true)
                .build();
        superUser.setPassword(passwordEncoder.encode("superpassword"));
        userRepository.save(superUser);
    }

    @Test
    void loadUserByUsername_userFound_returnsUserDetailsWithUserRole() {
        UserDetails userDetails = authService.loadUserByUsername("testuser");
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("testuser");
        assertThat(passwordEncoder.matches("password123", userDetails.getPassword())).isTrue();
        assertThat(userDetails.getAuthorities()).anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
    }

    @Test
    void loadUserByUsername_superuserFound_returnsUserDetailsWithSuperuserRole() {
        UserDetails userDetails = authService.loadUserByUsername("testsuperuser");
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("testsuperuser");
        assertThat(passwordEncoder.matches("superpassword", userDetails.getPassword())).isTrue();
        assertThat(userDetails.getAuthorities()).anyMatch(a -> a.getAuthority().equals("ROLE_SUPERUSER"));
    }

    @Test
    void loadUserByUsername_userWithNullPassword_returnsUserDetailsWithEmptyPassword() {
        User nullPasswordUser = User.builder()
                .username("nullpassuser")
                .balance(BigDecimal.ZERO)
                .superuser(false)
                .build();
        nullPasswordUser.setPassword(null);
        userRepository.save(nullPasswordUser);

        UserDetails userDetails = authService.loadUserByUsername("nullpassuser");
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getPassword()).isEqualTo("");
    }

    @Test
    void loadUserByUsername_userNotFound_throwsUsernameNotFoundException() {
        assertThatThrownBy(() -> authService.loadUserByUsername("nonexistentuser"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found: nonexistentuser");
    }

    @Test
    void register_usernameDoesNotExist_createsUserAndReturnsToken() {
        RegisterRequest registerRequest = new RegisterRequest("newuser", "newpassword");
        AuthResponse response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isNotBlank();

        User savedUser = userRepository.findByUsername("newuser").orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(passwordEncoder.matches("newpassword", savedUser.getPassword())).isTrue();
        assertThat(savedUser.isSuperuser()).isFalse();
    }

    @Test
    void register_usernameAlreadyExists_throwsUsernameAlreadyExistsException() {
        RegisterRequest registerRequest = new RegisterRequest("testuser", "anypassword");
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(UsernameAlreadyExistsException.class);
    }

    @Test
    void login_validCredentials_returnsToken() {
        LoginRequest loginRequest = new LoginRequest("testuser", "password123");
        AuthResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isNotBlank();
    }

    @Test
    void login_invalidPassword_throwsInvalidCredentialsException() {
        LoginRequest loginRequest = new LoginRequest("testuser", "wrongpassword");
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
