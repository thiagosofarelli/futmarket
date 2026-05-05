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
        User user = User.builder()
                .username("alice_save")
                .balance(new BigDecimal("500.00"))
                .superuser(false)
                .build();

        userRepository.saveAndFlush(user);

        User found = userRepository.findById(user.getId()).orElseThrow();
        assertThat(found.getUsername()).isEqualTo("alice_save");
        assertThat(found.getBalance()).isEqualByComparingTo("500.00");
        assertThat(found.isSuperuser()).isFalse();

        assertThat(found.getPortfolio()).isNotNull();
    }

    @Test
    void findByUsername_returnsUser() {
        User bob = User.builder()
                .username("bob")
                .balance(BigDecimal.TEN)
                .build();
        userRepository.saveAndFlush(bob);

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
        userRepository.saveAndFlush(User.builder().username("alice_normal").balance(BigDecimal.ZERO).build());

        userRepository.saveAndFlush(User.builder()
                .username("SUPERUSER_TEST")
                .balance(BigDecimal.ZERO)
                .superuser(true)
                .build());

        Optional<User> found = userRepository.findBySuperuserTrue();
        assertThat(found).isPresent();
        assertThat(found.get().isSuperuser()).isTrue();
        assertThat(found.get().getUsername()).isEqualTo("SUPERUSER_TEST");
    }

    @Test
    void username_mustBeUnique() {
        userRepository.saveAndFlush(User.builder().username("duplicate").balance(BigDecimal.ZERO).build());

        User another = User.builder().username("duplicate").balance(BigDecimal.ZERO).build();

        assertThatThrownBy(() -> userRepository.saveAndFlush(another))
                .isInstanceOf(Exception.class);
    }

    @Test
    void builder_trimsUsernameBeforeSaving() {
        User user = User.builder()
                .username("  leandro  ")
                .balance(BigDecimal.ZERO)
                .build();

        userRepository.saveAndFlush(user);

        Optional<User> found = userRepository.findByUsername("leandro");
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("leandro"); // Verificamos el trim()
    }
}