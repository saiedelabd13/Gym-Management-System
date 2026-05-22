package com.example.gym_system.unit_test;

import com.example.gym_system.entity.*;
import java.time.LocalDate;
import java.time.LocalDateTime;


public class TestDataBuilder {

    // ── Member ──────────────────────────────────────────────────────────
    public static Member buildMember() {
        return Member.builder()
                .firstName("Ahmed")
                .lastName("Ali")
                .email("ahmed.ali@test.com")
                .phone("01234567890")
                .dateOfBirth(LocalDate.of(1992, 5, 15))
                .gender(Member.Gender.MALE)
                .address("Cairo, Egypt")
                .status(Member.MemberStatus.ACTIVE)
                .build();
    }

    public static Member buildMember(String firstName, String lastName, String email) {
        return Member.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .phone("01000000000")
                .status(Member.MemberStatus.ACTIVE)
                .build();
    }

    // ── Trainer ─────────────────────────────────────────────────────────
    public static Trainer buildTrainer() {
        return Trainer.builder()
                .firstName("Mohamed")
                .lastName("Hassan")
                .email("trainer@test.com")
                .phone("01098765432")
                .specialization("Weightlifting")
                .bio("10 years experience")
                .salaryPerHour(200.0)
                .status(Trainer.TrainerStatus.ACTIVE)
                .build();
    }

    public static Trainer buildTrainer(String email, String specialization) {
        return Trainer.builder()
                .firstName("Trainer")
                .lastName("Test")
                .email(email)
                .phone("01000000001")
                .specialization(specialization)
                .status(Trainer.TrainerStatus.ACTIVE)
                .build();
    }

    // ── GymClass ────────────────────────────────────────────────────────
    public static GymClass buildGymClass(Trainer trainer) {
        return GymClass.builder()
                .name("Morning Yoga")
                .description("Relaxing yoga session")
                .startTime(LocalDateTime.now().plusDays(1).withHour(8).withMinute(0))
                .endTime(LocalDateTime.now().plusDays(1).withHour(9).withMinute(0))
                .capacity(20)
                .enrolledCount(0)
                .status(GymClass.ClassStatus.SCHEDULED)
                .trainer(trainer)
                .build();
    }

    // ── Subscription ────────────────────────────────────────────────────
    public static Subscription buildSubscription(Member member) {
        return Subscription.builder()
                .member(member)
                .plan(Subscription.SubscriptionPlan.MONTHLY)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .price(300.0)
                .status(Subscription.SubscriptionStatus.ACTIVE)
                .build();
    }

    // ── Payment ─────────────────────────────────────────────────────────
    public static Payment buildPayment(Member member, Subscription subscription) {
        return Payment.builder()
                .member(member)
                .subscription(subscription)
                .amount(300.0)
                .paymentMethod(Payment.PaymentMethod.CASH)
                .status(Payment.PaymentStatus.PENDING)
                .notes("Test payment")
                .build();
    }

    // ── User ────────────────────────────────────────────────────────────
    public static User buildUser(String username, String encodedPassword, User.Role role) {
        return User.builder()
                .username(username)
                .password(encodedPassword)
                .email(username + "@test.com")
                .role(role)
                .enabled(true)
                .build();
    }
}
