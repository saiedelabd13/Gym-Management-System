package com.example.gym_system.unit_test.repository_test;

import com.example.gym_system.entity.Member;
import com.example.gym_system.entity.Payment;
import com.example.gym_system.entity.Subscription;
import com.example.gym_system.repository.MemberRepository;
import com.example.gym_system.repository.PaymentRepository;
import com.example.gym_system.repository.SubscriptionRepository;
import com.example.gym_system.unit_test.TestDataBuilder;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("PaymentRepository Slice Tests")
class PaymentRepositoryTest {

    @Autowired
    PaymentRepository paymentRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    SubscriptionRepository subscriptionRepository;

    private Member member;
    private Subscription subscription;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        subscriptionRepository.deleteAll();
        memberRepository.deleteAll();

        member = memberRepository.save(TestDataBuilder.buildMember());
        subscription = subscriptionRepository.save(TestDataBuilder.buildSubscription(member));
    }

    @Test
    @DisplayName("✅ should find payments by member ID")
    void shouldFindByMemberId() {
        paymentRepository.save(TestDataBuilder.buildPayment(member, subscription));
        paymentRepository.save(TestDataBuilder.buildPayment(member, subscription));

        List<Payment> result = paymentRepository.findByMemberId(member.getId());

        assertThat(result).hasSize(2);
        result.forEach(p -> assertThat(p.getMember().getId()).isEqualTo(member.getId()));
    }

    @Test
    @DisplayName("✅ should find payments by status COMPLETED")
    void shouldFindByStatus() {
        Payment pending   = TestDataBuilder.buildPayment(member, subscription);
        Payment completed = TestDataBuilder.buildPayment(member, subscription);
        completed.setStatus(Payment.PaymentStatus.COMPLETED);

        paymentRepository.save(pending);
        paymentRepository.save(completed);

        List<Payment> result = paymentRepository.findByStatus(Payment.PaymentStatus.COMPLETED);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(Payment.PaymentStatus.COMPLETED);
    }

    @Test
    @DisplayName("✅ getTotalRevenue should sum all COMPLETED payments")
    void shouldCalculateTotalRevenue() {
        Payment p1 = TestDataBuilder.buildPayment(member, subscription);
        p1.setAmount(500.0);
        p1.setStatus(Payment.PaymentStatus.COMPLETED);

        Payment p2 = TestDataBuilder.buildPayment(member, subscription);
        p2.setAmount(300.0);
        p2.setStatus(Payment.PaymentStatus.COMPLETED);

        Payment p3 = TestDataBuilder.buildPayment(member, subscription);
        p3.setAmount(200.0);
        p3.setStatus(Payment.PaymentStatus.PENDING); // should NOT be counted

        paymentRepository.save(p1);
        paymentRepository.save(p2);
        paymentRepository.save(p3);

        Double revenue = paymentRepository.getTotalRevenue();

        assertThat(revenue).isEqualTo(800.0);
    }

    @Test
    @DisplayName("✅ getTotalRevenue returns null when no completed payments")
    void shouldReturnNullWhenNoRevenue() {
        Payment p = TestDataBuilder.buildPayment(member, subscription);
        p.setStatus(Payment.PaymentStatus.PENDING);
        paymentRepository.save(p);

        Double revenue = paymentRepository.getTotalRevenue();

        assertThat(revenue).isNull();
    }

    @Test
    @DisplayName("✅ getRevenueByDateRange should filter by date range")
    void shouldGetRevenueByDateRange() {
        Payment p = TestDataBuilder.buildPayment(member, subscription);
        p.setAmount(400.0);
        p.setStatus(Payment.PaymentStatus.COMPLETED);
        paymentRepository.save(p);

        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to   = LocalDateTime.now().plusDays(1);

        Double revenue = paymentRepository.getRevenueByDateRange(from, to);

        assertThat(revenue).isEqualTo(400.0);
    }
}
