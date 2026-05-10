package com.example.gym_system.service;


import com.example.gym_system.DTOs.request.PaymentRequest;
import com.example.gym_system.entity.Member;
import com.example.gym_system.entity.Payment;
import com.example.gym_system.entity.Subscription;
import com.example.gym_system.exception.ResourceNotFoundException;
import com.example.gym_system.repository.MemberRepository;
import com.example.gym_system.repository.PaymentRepository;
import com.example.gym_system.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;
    private final SubscriptionRepository subscriptionRepository;

    public Payment createPayment(PaymentRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member", request.getMemberId()));

        Subscription subscription = null;
        if (request.getSubscriptionId() != null) {
            subscription = subscriptionRepository.findById(request.getSubscriptionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subscription", request.getSubscriptionId()));
        }

        Payment payment = Payment.builder()
                .member(member)
                .subscription(subscription)
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .notes(request.getNotes())
                .build();
        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", id));
    }

    @Transactional(readOnly = true)
    public List<Payment> getMemberPayments(Long memberId) {
        return paymentRepository.findByMemberId(memberId);
    }

    public Payment completePayment(Long id) {
        Payment payment = getPaymentById(id);
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setPaidAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    public Payment refundPayment(Long id) {
        Payment payment = getPaymentById(id);
        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public Double getTotalRevenue() {
        Double revenue = paymentRepository.getTotalRevenue();
        return revenue != null ? revenue : 0.0;
    }

    @Transactional(readOnly = true)
    public Double getRevenueByDateRange(LocalDateTime from, LocalDateTime to) {
        Double revenue = paymentRepository.getRevenueByDateRange(from, to);
        return revenue != null ? revenue : 0.0;
    }
}
