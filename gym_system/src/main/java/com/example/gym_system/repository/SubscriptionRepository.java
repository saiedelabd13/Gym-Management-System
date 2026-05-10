package com.example.gym_system.repository;


import com.example.gym_system.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByMemberId(Long memberId);
    List<Subscription> findByStatus(Subscription.SubscriptionStatus status);
    List<Subscription> findByEndDateBefore(LocalDate date);
    List<Subscription> findByMemberIdAndStatus(Long memberId, Subscription.SubscriptionStatus status);
}
