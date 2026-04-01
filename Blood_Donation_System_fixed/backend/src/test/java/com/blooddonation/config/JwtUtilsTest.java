package com.blooddonation.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", "UnitTestJwtSecretKeyForBloodDonation");
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 60000);
    }

    @Test
    void generateTokenFromEmailCreatesTokenWithExpectedSubject() {
        String token = jwtUtils.generateTokenFromEmail("alice@example.com");

        assertTrue(jwtUtils.validateJwtToken(token));
        assertEquals("alice@example.com", jwtUtils.getEmailFromJwtToken(token));
        assertEquals("alice@example.com", jwtUtils.extractUsername(token));
    }

    @Test
    void generateJwtTokenUsesAuthenticationPrincipalUsername() {
        UserDetails principal = new User("bob@example.com", "password", Collections.emptyList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        String token = jwtUtils.generateJwtToken(authentication);

        assertEquals("bob@example.com", jwtUtils.getEmailFromJwtToken(token));
    }

    @Test
    void validateTokenReturnsTrueForMatchingUserAndFalseForMismatchedUser() {
        UserDetails tokenOwner = new User("carol@example.com", "password", Collections.emptyList());
        String token = jwtUtils.generateTokenFromEmail(tokenOwner.getUsername());

        UserDetails matchingUser = new User("carol@example.com", "password", Collections.emptyList());
        UserDetails differentUser = new User("dave@example.com", "password", Collections.emptyList());

        assertTrue(jwtUtils.validateToken(token, matchingUser));
        assertFalse(jwtUtils.validateToken(token, differentUser));
    }

    @Test
    void validateJwtTokenReturnsFalseForMalformedToken() {
        assertFalse(jwtUtils.validateJwtToken("not-a-jwt-token"));
    }
}
