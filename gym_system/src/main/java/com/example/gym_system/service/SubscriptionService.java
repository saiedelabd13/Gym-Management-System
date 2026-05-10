package com.example.gym_system.service;


import com.example.gym_system.DTOs.request.SubscriptionRequest;
import com.example.gym_system.entity.Member;
import com.example.gym_system.entity.Subscription;
import com.example.gym_system.exception.ResourceNotFoundException;
import com.example.gym_system.repository.MemberRepository;
import com.example.gym_system.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final MemberRepository memberRepository;

    public Subscription createSubscription(SubscriptionRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member", request.getMemberId()));

        Subscription subscription = Subscription.builder()
                .member(member)
                .plan(request.getPlan())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .price(request.getPrice())
                .build();
        return subscriptionRepository.save(subscription);
    }

    @Transactional(readOnly = true)
    public List<Subscription> getAllSubscriptions() {
        return subscriptionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Subscription getSubscriptionById(Long id) {
        return subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", id));
    }

    @Transactional(readOnly = true)
    public List<Subscription> getMemberSubscriptions(Long memberId) {
        return subscriptionRepository.findByMemberId(memberId);
    }

    public Subscription cancelSubscription(Long id) {
        Subscription sub = getSubscriptionById(id);
        sub.setStatus(Subscription.SubscriptionStatus.CANCELLED);
        return subscriptionRepository.save(sub);
    }

    @Transactional(readOnly = true)
    public List<Subscription> getExpiredSubscriptions() {
        return subscriptionRepository.findByEndDateBefore(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<Subscription> getActiveSubscriptions() {
        return subscriptionRepository.findByStatus(Subscription.SubscriptionStatus.ACTIVE);
    }
}
