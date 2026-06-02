package com.example.gym_system.integration;



import com.example.gym_system.DTOs.request.LoginRequest;
import com.example.gym_system.DTOs.request.MemberRequest;
import com.example.gym_system.entity.Member;
import com.example.gym_system.entity.User;
import com.example.gym_system.repository.MemberRepository;
import com.example.gym_system.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Member API Integration Tests")
class MemberIntegrationTest {

    @Autowired MockMvc         mockMvc;
    @Autowired ObjectMapper    objectMapper;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired PasswordEncoder  passwordEncoder;

    private static String adminToken;
    private static Long   createdMemberId;

    @BeforeEach
    void setUp() throws Exception {
        // Create admin user if not exists
        if (!userRepository.existsByUsername("admin_test")) {
            User admin = User.builder()
                    .username("admin_test")
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin_test@gym.com")
                    .role(User.Role.ADMIN)
                    .build();
            userRepository.save(admin);
        }
        // Get token
        if (adminToken == null) {
            LoginRequest login = new LoginRequest();
            login.setUsername("admin_test");
            login.setPassword("admin123");

            MvcResult result = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(login)))
                    .andExpect(status().isOk())
                    .andReturn();

            String body = result.getResponse().getContentAsString();
            adminToken = objectMapper.readTree(body).path("data").path("token").asText();
        }
    }

    @AfterEach
    void tearDown() {
        memberRepository.deleteAll();
    }

    // ── CREATE ────────────────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("POST /api/members → 201 Created")
    void shouldCreateMember() throws Exception {
        MemberRequest req = buildMemberRequest("Ahmed", "Ali", "ahmed@integration.com");

        MvcResult result = mockMvc.perform(post("/api/members")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").value("Ahmed"))
                .andExpect(jsonPath("$.data.email").value("ahmed@integration.com"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        createdMemberId = objectMapper.readTree(body).path("data").path("id").asLong();
        assertThat(createdMemberId).isPositive();
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/members → 409 Conflict when email duplicate")
    void shouldReturn409WhenDuplicateEmail() throws Exception {
        // First create
        MemberRequest req = buildMemberRequest("Ahmed", "Ali", "duplicate@integration.com");
        mockMvc.perform(post("/api/members")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        // Duplicate
        mockMvc.perform(post("/api/members")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/members → 400 Bad Request when validation fails")
    void shouldReturn400WhenValidationFails() throws Exception {
        MemberRequest req = new MemberRequest(); // empty — missing required fields

        mockMvc.perform(post("/api/members")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").isMap());
    }

    @Test
    @Order(4)
    @DisplayName("POST /api/members → 401 Unauthorized without token")
    void shouldReturn401WithoutToken() throws Exception {
        MemberRequest req = buildMemberRequest("Test", "User", "test@integration.com");

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    // ── READ ──────────────────────────────────────────────────────────────

    @Test
    @Order(5)
    @DisplayName("GET /api/members → 200 with list")
    void shouldGetAllMembers() throws Exception {
        // Seed 2 members
        memberRepository.save(buildMember("Ahmed", "Ali", "m1@test.com"));
        memberRepository.save(buildMember("Sara",  "Hassan", "m2@test.com"));

        mockMvc.perform(get("/api/members")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    @Order(6)
    @DisplayName("GET /api/members/{id} → 200 with member data")
    void shouldGetMemberById() throws Exception {
        Member saved = memberRepository.save(buildMember("Ahmed", "Ali", "byid@test.com"));

        mockMvc.perform(get("/api/members/" + saved.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstName").value("Ahmed"))
                .andExpect(jsonPath("$.data.email").value("byid@test.com"));
    }

    @Test
    @Order(7)
    @DisplayName("GET /api/members/{id} → 404 when member not found")
    void shouldReturn404WhenMemberNotFound() throws Exception {
        mockMvc.perform(get("/api/members/99999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @Order(8)
    @DisplayName("GET /api/members/search?query= → 200 with filtered results")
    void shouldSearchMembers() throws Exception {
        memberRepository.save(buildMember("Khaled", "Omar", "khaled@test.com"));
        memberRepository.save(buildMember("Sara",   "Ali",  "sara@test.com"));

        mockMvc.perform(get("/api/members/search?query=Khaled")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].firstName", hasItem("Khaled")));
    }

    // ── UPDATE ────────────────────────────────────────────────────────────

    @Test
    @Order(9)
    @DisplayName("PUT /api/members/{id} → 200 with updated data")
    void shouldUpdateMember() throws Exception {
        Member saved = memberRepository.save(buildMember("Old", "Name", "old@test.com"));

        MemberRequest update = buildMemberRequest("New", "Name", "old@test.com");

        mockMvc.perform(put("/api/members/" + saved.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstName").value("New"));
    }

    @Test
    @Order(10)
    @DisplayName("PATCH /api/members/{id}/status → 200 status updated")
    void shouldUpdateMemberStatus() throws Exception {
        Member saved = memberRepository.save(buildMember("Test", "Member", "status@test.com"));

        mockMvc.perform(patch("/api/members/" + saved.getId() + "/status?status=SUSPENDED")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUSPENDED"));
    }

    // ── DELETE ────────────────────────────────────────────────────────────

    @Test
    @Order(11)
    @DisplayName("DELETE /api/members/{id} → 200 deleted")
    void shouldDeleteMember() throws Exception {
        Member saved = memberRepository.save(buildMember("Delete", "Me", "delete@test.com"));

        mockMvc.perform(delete("/api/members/" + saved.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertThat(memberRepository.findById(saved.getId())).isEmpty();
    }

    // ── HELPERS ───────────────────────────────────────────────────────────

    private MemberRequest buildMemberRequest(String first, String last, String email) {
        MemberRequest r = new MemberRequest();
        r.setFirstName(first);
        r.setLastName(last);
        r.setEmail(email);
        r.setPhone("01234567890");
        r.setDateOfBirth(LocalDate.of(1992, 5, 15));
        r.setGender(Member.Gender.MALE);
        r.setAddress("Cairo, Egypt");
        return r;
    }

    private Member buildMember(String first, String last, String email) {
        return Member.builder()
                .firstName(first).lastName(last).email(email)
                .phone("01000000000").status(Member.MemberStatus.ACTIVE)
                .build();
    }
}
