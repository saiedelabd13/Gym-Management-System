package com.example.gym_system.unit_test.service_test;


import com.example.gym_system.DTOs.request.LoginRequest;
import com.example.gym_system.DTOs.request.RegisterRequest;
import com.example.gym_system.DTOs.response.AuthResponse;
import com.example.gym_system.entity.User;
import com.example.gym_system.exception.DuplicateResourceException;
import com.example.gym_system.repository.UserRepository;
import com.example.gym_system.security.AuthService;
import com.example.gym_system.security.JwtUtil;
import com.example.gym_system.unit_test.TestDataBuilder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock PasswordEncoder       passwordEncoder;
    @Mock
    JwtUtil jwtUtil;
    @Mock AuthenticationManager authenticationManager;
    @InjectMocks
    AuthService authService;

    private User           user;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        user = TestDataBuilder.buildUser("admin", "encoded_password", User.Role.ADMIN);
        user.setId(1L);

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("admin");
        registerRequest.setPassword("admin123");
        registerRequest.setEmail("admin@test.com");
        registerRequest.setRole(User.Role.ADMIN);

        loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("admin123");
    }

    @Nested
    @DisplayName("register()")
    class RegisterTests {

        @Test
        @DisplayName("✅ should register user and return token")
        void shouldRegisterUser() {
            when(userRepository.existsByUsername("admin")).thenReturn(false);
            when(userRepository.existsByEmail("admin@test.com")).thenReturn(false);
            when(passwordEncoder.encode("admin123")).thenReturn("encoded_password");
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(jwtUtil.generateToken(any())).thenReturn("mock.jwt.token");

            AuthResponse response = authService.register(registerRequest);

            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo("mock.jwt.token");
            assertThat(response.getUsername()).isEqualTo("admin");
            assertThat(response.getRole()).isEqualTo("ADMIN");
            assertThat(response.getTokenType()).isEqualTo("Bearer");

            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("❌ should throw DuplicateResourceException when username taken")
        void shouldThrowWhenUsernameTaken() {
            when(userRepository.existsByUsername("admin")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(registerRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("admin");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("❌ should throw DuplicateResourceException when email taken")
        void shouldThrowWhenEmailTaken() {
            when(userRepository.existsByUsername("admin")).thenReturn(false);
            when(userRepository.existsByEmail("admin@test.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(registerRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("admin@test.com");
        }

        @Test
        @DisplayName("✅ should default to STAFF role when role not provided")
        void shouldDefaultToStaffRole() {
            registerRequest.setRole(null);
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(any())).thenReturn("encoded");
            when(jwtUtil.generateToken(any())).thenReturn("token");

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            when(userRepository.save(captor.capture())).thenReturn(user);

            authService.register(registerRequest);

            assertThat(captor.getValue().getRole()).isEqualTo(User.Role.STAFF);
        }
    }

    @Nested
    @DisplayName("login()")
    class LoginTests {

        @Test
        @DisplayName("✅ should login and return token")
        void shouldLoginAndReturnToken() {
            Authentication auth = mock(Authentication.class);
            when(auth.getPrincipal()).thenReturn(user);
            when(authenticationManager.authenticate(any())).thenReturn(auth);
            when(jwtUtil.generateToken(user)).thenReturn("login.jwt.token");

            AuthResponse response = authService.login(loginRequest);

            assertThat(response.getToken()).isEqualTo("login.jwt.token");
            assertThat(response.getUsername()).isEqualTo("admin");
        }

        @Test
        @DisplayName("❌ should throw BadCredentialsException when credentials invalid")
        void shouldThrowBadCredentials() {
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(BadCredentialsException.class);
        }
    }
}
