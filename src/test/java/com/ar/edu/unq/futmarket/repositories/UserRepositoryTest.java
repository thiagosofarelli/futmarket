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
        User user = regularUser("alice", new BigDecimal("500.00"));
        userRepository.save(user);

        User found = userRepository.findById(user.getId()).orElseThrow();
        assertThat(found.getUsername()).isEqualTo("alice");
        assertThat(found.getBalance()).isEqualByComparingTo("500.00");
        assertThat(found.isSuperuser()).isFalse();
    }

    @Test
    void findByUsername_returnsUser() {
        userRepository.save(regularUser("bob", BigDecimal.TEN));

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
        userRepository.save(regularUser("alice", BigDecimal.ZERO));
        userRepository.save(superUser());

        Optional<User> found = userRepository.findBySuperuserTrue();
        assertThat(found).isPresent();
        assertThat(found.get().isSuperuser()).isTrue();
    }

    @Test
    void findBySuperuserTrue_noSuperuser_returnsEmpty() {
        userRepository.save(regularUser("alice", BigDecimal.ZERO));

        assertThat(userRepository.findBySuperuserTrue()).isEmpty();
    }

    @Test
    void username_mustBeUnique() {
        userRepository.save(regularUser("duplicate", BigDecimal.ZERO));

        User another = regularUser("duplicate", BigDecimal.ZERO);
        assertThatThrownBy(() -> userRepository.saveAndFlush(another))
                .isInstanceOf(Exception.class);
    }

    // --- helpers ---

    private User regularUser(String username, BigDecimal balance) {
        User u = new User();
        u.setUsername(username);
        u.setBalance(balance);
        return u;
    }

    private User superUser() {
        User u = new User();
        u.setUsername("SUPERUSER");
        u.setBalance(BigDecimal.ZERO);
        u.setSuperuser(true);
        return u;
    }
}

