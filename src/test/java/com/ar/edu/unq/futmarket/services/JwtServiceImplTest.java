package com.ar.edu.unq.futmarket.services;

import com.ar.edu.unq.futmarket.services.impl.JwtServiceImpl;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class JwtServiceImplTest {

    @Autowired
    private JwtServiceImpl jwtService;

    @Test
    void generateToken_returnsNonBlankToken() {
        String token = jwtService.generateToken("alice");
        assertThat(token).isNotBlank();
    }

    @Test
    void generateToken_differentUsers_produceDifferentTokens() {
        String tokenA = jwtService.generateToken("alice");
        String tokenB = jwtService.generateToken("bob");
        assertThat(tokenA).isNotEqualTo(tokenB);
    }

    @Test
    void extractUsername_validToken_returnsUsername() {
        String token = jwtService.generateToken("alice");
        assertThat(jwtService.extractUsername(token)).isEqualTo("alice");
    }

    @Test
    void extractUsername_invalidToken_throwsJwtException() {
        assertThatThrownBy(() -> jwtService.extractUsername("not.a.valid.token"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void isTokenValid_matchingUser_returnsTrue() {
        String token = jwtService.generateToken("alice");
        UserDetails userDetails = User.builder().username("alice").password("").roles("USER").build();
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void isTokenValid_differentUser_returnsFalse() {
        String token = jwtService.generateToken("alice");
        UserDetails bob = User.builder().username("bob").password("").roles("USER").build();
        assertThat(jwtService.isTokenValid(token, bob)).isFalse();
    }

    @Test
    void generateToken_containsThreeParts() {
        String token = jwtService.generateToken("testuser");
        assertThat(token.split("\\.")).hasSize(3);
    }
}
