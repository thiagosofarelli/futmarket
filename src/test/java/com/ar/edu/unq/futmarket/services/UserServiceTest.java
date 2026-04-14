package com.ar.edu.unq.futmarket.services;

import com.ar.edu.unq.futmarket.model.User;
import com.ar.edu.unq.futmarket.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private User alice;
    private User superuser;

    @BeforeEach
    void setUp() {
        alice     = userRepository.save(new User("alice", new BigDecimal("500.00"), false));
        superuser = userRepository.save(new User("SUPERUSER", BigDecimal.ZERO, true));
    }

    @Test
    void findById_found_returnsUser() {
        User found = userService.findById(alice.getId());
        assertThat(found.getUsername()).isEqualTo("alice");
    }

    @Test
    void findById_notFound_throwsEntityNotFoundException() {
        assertThatThrownBy(() -> userService.findById(-1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("-1");
    }

    @Test
    void findByUsername_found_returnsUser() {
        User found = userService.findByUsername("alice");
        assertThat(found.getId()).isEqualTo(alice.getId());
    }

    @Test
    void findByUsername_notFound_throwsEntityNotFoundException() {
        assertThatThrownBy(() -> userService.findByUsername("unknown"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void findSuperuser_returnsSuperuser() {
        User found = userService.findSuperuser();
        assertThat(found.isSuperuser()).isTrue();
        assertThat(found.getUsername()).isEqualTo("SUPERUSER");
    }

    @Test
    void findSuperuser_noSuperuser_throwsEntityNotFoundException() {
        userRepository.delete(superuser);
        assertThatThrownBy(() -> userService.findSuperuser())
                .isInstanceOf(EntityNotFoundException.class);
    }
}
