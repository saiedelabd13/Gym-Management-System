package com.example.gym_system.Controllers;

import com.example.gym_system.DTOs.request.SubscriptionRequest;
import com.example.gym_system.entity.Subscription;
import com.example.gym_system.DTOs.response.ApiResponse;
import com.example.gym_system.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<Subscription>> createSubscription(@Valid @RequestBody SubscriptionRequest request) {
        Subscription subscription = subscriptionService.createSubscription(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(subscription, "Subscription created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Subscription>>> getAllSubscriptions() {
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.getAllSubscriptions()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Subscription>> getSubscriptionById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.getSubscriptionById(id)));
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<ApiResponse<List<Subscription>>> getMemberSubscriptions(@PathVariable Long memberId) {
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.getMemberSubscriptions(memberId)));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<Subscription>>> getActiveSubscriptions() {
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.getActiveSubscriptions()));
    }

    @GetMapping("/expired")
    public ResponseEntity<ApiResponse<List<Subscription>>> getExpiredSubscriptions() {
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.getExpiredSubscriptions()));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Subscription>> cancelSubscription(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.cancelSubscription(id), "Subscription cancelled"));
    }
}
