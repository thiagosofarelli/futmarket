package com.ar.edu.unq.futmarket.services;

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
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @BeforeEach
    void setUp() {
        normalUser = User.builder()
                .username("testuser")
                .balance(new BigDecimal("1000.00"))
                .superuser(false)
                .build();
        normalUser.setPassword(passwordEncoder.encode("password123"));
        userRepository.save(normalUser);
    }

    @Test
    void loadUserByUsername_userExists_returnsUserDetails() {
        UserDetails userDetails = authService.loadUserByUsername("testuser");
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("testuser");
    }

    @Test
    void loadUserByUsername_usernameIsNull_throwsUsernameNotFoundException() {
        assertThrows(UsernameNotFoundException.class, () -> {
            authService.loadUserByUsername(null);
        });
    }

    @Test
    void loadUserByUsername_userNotFound_throwsUsernameNotFoundException() {
        assertThatThrownBy(() -> authService.loadUserByUsername("nonexistentuser"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found: nonexistentuser");
    }

    @Test
    void register_usernameDoesNotExist_createsUserAndReturnsToken() {
        String token = authService.register("newuser", "newpassword");

        assertThat(token).isNotBlank();
        User savedUser = userRepository.findByUsername("newuser").orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(passwordEncoder.matches("newpassword", savedUser.getPassword())).isTrue();
        assertThat(savedUser.isSuperuser()).isFalse();
    }

    @Test
    void register_usernameAlreadyExists_throwsUsernameAlreadyExistsException() {
        assertThatThrownBy(() -> authService.register("testuser", "anypassword"))
                .isInstanceOf(UsernameAlreadyExistsException.class);
    }

    @Test
    void login_validCredentials_returnsToken() {
        String token = authService.login("testuser", "password123");

        assertThat(token).isNotBlank();
    }

    @Test
    void login_invalidPassword_throwsInvalidCredentialsException() {
        assertThatThrownBy(() -> authService.login("testuser", "wrongpassword"))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
