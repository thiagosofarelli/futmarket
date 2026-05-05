package com.ar.edu.unq.futmarket.services;

import com.ar.edu.unq.futmarket.exception.SuperUserNotFoundException;
import com.ar.edu.unq.futmarket.exception.UserNotFoundException;
import com.ar.edu.unq.futmarket.model.User;
import com.ar.edu.unq.futmarket.repositories.UserRepository;
import com.ar.edu.unq.futmarket.services.impl.UserServiceImpl;
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
class UserServiceImplTest {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    private User alice;
    private User superuser;

    @BeforeEach
    void setUp() {
        alice = userRepository.save(User.builder()
                .username("alice")
                .balance(new BigDecimal("500.00"))
                .superuser(false)
                .build());
        superuser = userRepository.save(User.builder()
                .username("SUPERUSER")
                .balance(BigDecimal.ZERO)
                .superuser(true)
                .build());
    }

    @Test
    void findById_found_returnsUser() {
        User found = userService.findById(alice.getId());
        assertThat(found.getUsername()).isEqualTo("alice");
    }

    @Test
    void findById_notFound_throwsUserNotFoundException() {
        assertThatThrownBy(() -> userService.findById(-1L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void findByUsername_found_returnsUser() {
        User found = userService.findByUsername("alice");
        assertThat(found.getId()).isEqualTo(alice.getId());
    }

    @Test
    void findByUsername_notFound_throwsUserNotFoundException() {
        assertThatThrownBy(() -> userService.findByUsername("unknown"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void findSuperuser_returnsSuperuser() {
        User found = userService.findSuperuser();
        assertThat(found.isSuperuser()).isTrue();
        assertThat(found.getUsername()).isEqualTo("SUPERUSER");
    }

    @Test
    void findSuperuser_noSuperuser_throwsSuperUserNotFoundException() {
        userRepository.delete(superuser);
        assertThatThrownBy(() -> userService.findSuperuser())
                .isInstanceOf(SuperUserNotFoundException.class);
    }
}
