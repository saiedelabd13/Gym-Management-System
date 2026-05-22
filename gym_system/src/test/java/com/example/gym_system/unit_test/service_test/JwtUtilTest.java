package com.example.gym_system.unit_test.service_test;




import com.example.gym_system.entity.User;
import com.example.gym_system.security.JwtUtil;
import com.example.gym_system.unit_test.TestDataBuilder;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtUtil Unit Tests")
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private User    user;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "test-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600000L);

        user = TestDataBuilder.buildUser("testuser", "encoded", User.Role.STAFF);
    }

    @Test
    @DisplayName("✅ should generate valid token")
    void shouldGenerateToken() {
        String token = jwtUtil.generateToken(user);

        assertThat(token).isNotNull().isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // header.payload.signature
    }

    @Test
    @DisplayName("✅ should extract username from token")
    void shouldExtractUsername() {
        String token = jwtUtil.generateToken(user);

        String username = jwtUtil.extractUsername(token);

        assertThat(username).isEqualTo("testuser");
    }

    @Test
    @DisplayName("✅ should validate token successfully")
    void shouldValidateToken() {
        String token = jwtUtil.generateToken(user);

        boolean isValid = jwtUtil.validateToken(token, user);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("❌ should not validate token for different user")
    void shouldNotValidateTokenForDifferentUser() {
        String token = jwtUtil.generateToken(user);
        User otherUser = TestDataBuilder.buildUser("other", "encoded", User.Role.STAFF);

        boolean isValid = jwtUtil.validateToken(token, otherUser);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("❌ should fail validation for expired token")
    void shouldFailForExpiredToken() {
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1000L); // already expired
        String expiredToken = jwtUtil.generateToken(user);

        assertThatThrownBy(() -> jwtUtil.validateToken(expiredToken, user))
                .isInstanceOf(Exception.class);
    }
}
