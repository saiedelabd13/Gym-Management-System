package com.example.gym_system.unit_test.service_test;




import com.example.gym_system.DTOs.request.PaymentRequest;
import com.example.gym_system.entity.Member;
import com.example.gym_system.entity.Payment;
import com.example.gym_system.entity.Subscription;
import com.example.gym_system.exception.ResourceNotFoundException;
import com.example.gym_system.repository.MemberRepository;
import com.example.gym_system.repository.PaymentRepository;
import com.example.gym_system.repository.SubscriptionRepository;
import com.example.gym_system.service.PaymentService;
import com.example.gym_system.unit_test.TestDataBuilder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Unit Tests")
class PaymentServiceTest {

    @Mock
    PaymentRepository paymentRepository;
    @Mock
    MemberRepository memberRepository;
    @Mock
    SubscriptionRepository subscriptionRepository;
    @InjectMocks
    PaymentService paymentService;

    private Member member;
    private Subscription subscription;
    private Payment payment;
    private PaymentRequest paymentRequest;

    @BeforeEach
    void setUp() {
        member = TestDataBuilder.buildMember();
        member.setId(1L);

        subscription = TestDataBuilder.buildSubscription(member);
        subscription.setId(1L);

        payment = TestDataBuilder.buildPayment(member, subscription);
        payment.setId(1L);

        paymentRequest = new PaymentRequest();
        paymentRequest.setMemberId(1L);
        paymentRequest.setSubscriptionId(1L);
        paymentRequest.setAmount(300.0);
        paymentRequest.setPaymentMethod(Payment.PaymentMethod.CASH);
        paymentRequest.setNotes("Monthly payment");
    }

    @Test
    @DisplayName("✅ should create payment with subscription")
    void shouldCreatePaymentWithSubscription() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));
        when(paymentRepository.save(any())).thenReturn(payment);

        Payment result = paymentService.createPayment(paymentRequest);

        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualTo(300.0);
        assertThat(result.getPaymentMethod()).isEqualTo(Payment.PaymentMethod.CASH);
        assertThat(result.getStatus()).isEqualTo(Payment.PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("✅ should create payment without subscription")
    void shouldCreatePaymentWithoutSubscription() {
        paymentRequest.setSubscriptionId(null);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(paymentRepository.save(any())).thenReturn(payment);

        Payment result = paymentService.createPayment(paymentRequest);

        assertThat(result).isNotNull();
        verify(subscriptionRepository, never()).findById(any());
    }

    @Test
    @DisplayName("❌ should throw when member not found on payment creation")
    void shouldThrowWhenMemberNotFound() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());
        paymentRequest.setMemberId(99L);

        assertThatThrownBy(() -> paymentService.createPayment(paymentRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("✅ should complete payment and set paidAt")
    void shouldCompletePayment() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Payment result = paymentService.completePayment(1L);

        assertThat(result.getStatus()).isEqualTo(Payment.PaymentStatus.COMPLETED);
        assertThat(result.getPaidAt()).isNotNull();
    }

    @Test
    @DisplayName("✅ should refund payment")
    void shouldRefundPayment() {
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Payment result = paymentService.refundPayment(1L);

        assertThat(result.getStatus()).isEqualTo(Payment.PaymentStatus.REFUNDED);
    }

    @Test
    @DisplayName("✅ should return total revenue")
    void shouldReturnTotalRevenue() {
        when(paymentRepository.getTotalRevenue()).thenReturn(5000.0);

        Double revenue = paymentService.getTotalRevenue();

        assertThat(revenue).isEqualTo(5000.0);
    }

    @Test
    @DisplayName("✅ should return 0 when no revenue exists")
    void shouldReturnZeroWhenNoRevenue() {
        when(paymentRepository.getTotalRevenue()).thenReturn(null);

        Double revenue = paymentService.getTotalRevenue();

        assertThat(revenue).isZero();
    }

    @Test
    @DisplayName("✅ should return member payments")
    void shouldReturnMemberPayments() {
        when(paymentRepository.findByMemberId(1L)).thenReturn(List.of(payment));

        List<Payment> result = paymentService.getMemberPayments(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMember().getId()).isEqualTo(1L);
    }
}
