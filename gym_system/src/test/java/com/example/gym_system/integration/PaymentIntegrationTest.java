package com.example.gym_system.integration;



import com.example.gym_system.DTOs.request.LoginRequest;
import com.example.gym_system.DTOs.request.PaymentRequest;
import com.example.gym_system.entity.Member;
import com.example.gym_system.entity.Payment;
import com.example.gym_system.entity.Subscription;
import com.example.gym_system.entity.User;
import com.example.gym_system.repository.MemberRepository;
import com.example.gym_system.repository.PaymentRepository;
import com.example.gym_system.repository.SubscriptionRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Payment API Integration Tests")
class PaymentIntegrationTest {

    @Autowired MockMvc              mockMvc;
    @Autowired ObjectMapper         objectMapper;
    @Autowired
    PaymentRepository paymentRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    SubscriptionRepository subscriptionRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired PasswordEncoder      passwordEncoder;

    private static String adminToken;
    private Member member;
    private Subscription subscription;

    @BeforeEach
    void setUp() throws Exception {
        paymentRepository.deleteAll();
        subscriptionRepository.deleteAll();
        memberRepository.deleteAll();

        // Seed member and subscription
        member = memberRepository.save(Member.builder()
                .firstName("Ahmed").lastName("Ali")
                .email("pay_member@test.com").phone("01234567890")
                .status(Member.MemberStatus.ACTIVE).build());

        subscription = subscriptionRepository.save(Subscription.builder()
                .member(member)
                .plan(Subscription.SubscriptionPlan.MONTHLY)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .price(300.0)
                .status(Subscription.SubscriptionStatus.ACTIVE)
                .build());

        // Admin token
        if (adminToken == null) {
            if (!userRepository.existsByUsername("pay_admin")) {
                userRepository.save(User.builder()
                        .username("pay_admin")
                        .password(passwordEncoder.encode("admin123"))
                        .email("pay_admin@gym.com")
                        .role(User.Role.ADMIN).build());
            }
            LoginRequest login = new LoginRequest();
            login.setUsername("pay_admin");
            login.setPassword("admin123");

            MvcResult result = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(login)))
                    .andExpect(status().isOk()).andReturn();

            adminToken = objectMapper.readTree(result.getResponse().getContentAsString())
                    .path("data").path("token").asText();
        }
    }

    @Test
    @Order(1)
    @DisplayName("POST /api/payments → 201 Created with subscription")
    void shouldCreatePayment() throws Exception {
        PaymentRequest req = new PaymentRequest();
        req.setMemberId(member.getId());
        req.setSubscriptionId(subscription.getId());
        req.setAmount(300.0);
        req.setPaymentMethod(Payment.PaymentMethod.CASH);
        req.setNotes("Monthly payment");

        mockMvc.perform(post("/api/payments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.amount").value(300.0))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.paymentMethod").value("CASH"));
    }

    @Test
    @Order(2)
    @DisplayName("PATCH /api/payments/{id}/complete → 200 status=COMPLETED")
    void shouldCompletePayment() throws Exception {
        Payment payment = paymentRepository.save(Payment.builder()
                .member(member).subscription(subscription)
                .amount(300.0).paymentMethod(Payment.PaymentMethod.CASH)
                .status(Payment.PaymentStatus.PENDING).build());

        mockMvc.perform(patch("/api/payments/" + payment.getId() + "/complete")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.paidAt").isNotEmpty());
    }

    @Test
    @Order(3)
    @DisplayName("PATCH /api/payments/{id}/refund → 200 status=REFUNDED")
    void shouldRefundPayment() throws Exception {
        Payment payment = paymentRepository.save(Payment.builder()
                .member(member).subscription(subscription)
                .amount(300.0).paymentMethod(Payment.PaymentMethod.CREDIT_CARD)
                .status(Payment.PaymentStatus.COMPLETED).build());

        mockMvc.perform(patch("/api/payments/" + payment.getId() + "/refund")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REFUNDED"));
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/payments/revenue/total → 200 with revenue sum")
    void shouldGetTotalRevenue() throws Exception {
        paymentRepository.save(Payment.builder()
                .member(member).amount(500.0)
                .paymentMethod(Payment.PaymentMethod.CASH)
                .status(Payment.PaymentStatus.COMPLETED).build());

        mockMvc.perform(get("/api/payments/revenue/total")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    @Order(5)
    @DisplayName("GET /api/payments/member/{id} → 200 with member payments")
    void shouldGetMemberPayments() throws Exception {
        paymentRepository.save(Payment.builder()
                .member(member).amount(300.0)
                .paymentMethod(Payment.PaymentMethod.ONLINE)
                .status(Payment.PaymentStatus.PENDING).build());

        mockMvc.perform(get("/api/payments/member/" + member.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
        System.out.println("Member payments retrieved successfully");
        System.out.println("mission in all is done ");
    }
}
