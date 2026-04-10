package com.ar.edu.unq.futmarket.repositories;

import com.ar.edu.unq.futmarket.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void save_and_findById() {
        User user = new User("alice_save", new BigDecimal("500.00"), false);
        userRepository.saveAndFlush(user);

        User found = userRepository.findById(user.getId()).orElseThrow();
        assertThat(found.getUsername()).isEqualTo("alice_save");
        assertThat(found.getBalance()).isEqualByComparingTo("500.00");
        assertThat(found.isSuperuser()).isFalse();
    }

    @Test
    void findByUsername_returnsUser() {
        userRepository.saveAndFlush(regularUser("bob", BigDecimal.TEN));

        Optional<User> found = userRepository.findByUsername("bob");
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("bob");
    }

    @Test
    void findByUsername_unknownUser_returnsEmpty() {
        assertThat(userRepository.findByUsername("ghost")).isEmpty();
    }

    @Test
    void findBySuperuserTrue_returnsSuperuser() {
        userRepository.saveAndFlush(regularUser("alice_super", BigDecimal.ZERO));
        userRepository.saveAndFlush(superUser());

        Optional<User> found = userRepository.findBySuperuserTrue();
        assertThat(found).isPresent();
        assertThat(found.get().isSuperuser()).isTrue();
    }

    @Test
    void findBySuperuserTrue_noSuperuser_returnsEmpty() {
        userRepository.saveAndFlush(regularUser("alice_no_super", BigDecimal.ZERO));

        assertThat(userRepository.findBySuperuserTrue()).isEmpty();
    }

    @Test
    void username_mustBeUnique() {
        userRepository.saveAndFlush(regularUser("duplicate", BigDecimal.ZERO));

        User another = regularUser("duplicate", BigDecimal.ZERO);
        assertThatThrownBy(() -> userRepository.saveAndFlush(another))
                .isInstanceOf(Exception.class);
    }

    // --- helpers ---

    private User superUser() {
        return new User("SUPERUSER", BigDecimal.ZERO, true);
    }

    private User regularUser(String username, BigDecimal balance) {
        return new User(username, balance, false);
    }
}

