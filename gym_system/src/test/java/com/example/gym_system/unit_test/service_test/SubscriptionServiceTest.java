package com.example.gym_system.unit_test.service_test;



import com.example.gym_system.DTOs.request.SubscriptionRequest;
import com.example.gym_system.entity.Member;
import com.example.gym_system.entity.Subscription;
import com.example.gym_system.exception.ResourceNotFoundException;
import com.example.gym_system.repository.MemberRepository;
import com.example.gym_system.repository.SubscriptionRepository;
import com.example.gym_system.service.SubscriptionService;
import com.example.gym_system.unit_test.TestDataBuilder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionService Unit Tests")
class SubscriptionServiceTest {

    @Mock
    SubscriptionRepository subscriptionRepository;
    @Mock
    MemberRepository memberRepository;
    @InjectMocks
    SubscriptionService subscriptionService;

    private Member member;
    private Subscription subscription;
    private SubscriptionRequest subscriptionRequest;

    @BeforeEach
    void setUp() {
        member = TestDataBuilder.buildMember();
        member.setId(1L);

        subscription = TestDataBuilder.buildSubscription(member);
        subscription.setId(1L);

        subscriptionRequest = new SubscriptionRequest();
        subscriptionRequest.setMemberId(1L);
        subscriptionRequest.setPlan(Subscription.SubscriptionPlan.MONTHLY);
        subscriptionRequest.setStartDate(LocalDate.now());
        subscriptionRequest.setEndDate(LocalDate.now().plusMonths(1));
        subscriptionRequest.setPrice(300.0);
    }

    @Test
    @DisplayName("✅ should create subscription for existing member")
    void shouldCreateSubscription() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(subscriptionRepository.save(any())).thenReturn(subscription);

        Subscription result = subscriptionService.createSubscription(subscriptionRequest);

        assertThat(result).isNotNull();
        assertThat(result.getPlan()).isEqualTo(Subscription.SubscriptionPlan.MONTHLY);
        assertThat(result.getPrice()).isEqualTo(300.0);
        assertThat(result.getStatus()).isEqualTo(Subscription.SubscriptionStatus.ACTIVE);
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    @DisplayName("❌ should throw ResourceNotFoundException when member not found on create")
    void shouldThrowWhenMemberNotFound() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());
        subscriptionRequest.setMemberId(99L);

        assertThatThrownBy(() -> subscriptionService.createSubscription(subscriptionRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("✅ should return all subscriptions")
    void shouldGetAllSubscriptions() {
        when(subscriptionRepository.findAll()).thenReturn(List.of(subscription));

        List<Subscription> result = subscriptionService.getAllSubscriptions();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("✅ should return active subscriptions")
    void shouldGetActiveSubscriptions() {
        when(subscriptionRepository.findByStatus(Subscription.SubscriptionStatus.ACTIVE))
                .thenReturn(List.of(subscription));

        List<Subscription> result = subscriptionService.getActiveSubscriptions();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(Subscription.SubscriptionStatus.ACTIVE);
    }

    @Test
    @DisplayName("✅ should return expired subscriptions")
    void shouldGetExpiredSubscriptions() {
        subscription.setStatus(Subscription.SubscriptionStatus.EXPIRED);
        subscription.setEndDate(LocalDate.now().minusDays(10));
        when(subscriptionRepository.findByEndDateBefore(any(LocalDate.class)))
                .thenReturn(List.of(subscription));

        List<Subscription> result = subscriptionService.getExpiredSubscriptions();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("✅ should return member subscriptions")
    void shouldGetMemberSubscriptions() {
        when(subscriptionRepository.findByMemberId(1L)).thenReturn(List.of(subscription));

        List<Subscription> result = subscriptionService.getMemberSubscriptions(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMember().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("✅ should cancel subscription")
    void shouldCancelSubscription() {
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));
        when(subscriptionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Subscription result = subscriptionService.cancelSubscription(1L);

        assertThat(result.getStatus()).isEqualTo(Subscription.SubscriptionStatus.CANCELLED);
    }

    @Test
    @DisplayName("❌ should throw when cancelling non-existing subscription")
    void shouldThrowWhenCancellingNotFound() {
        when(subscriptionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.cancelSubscription(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
