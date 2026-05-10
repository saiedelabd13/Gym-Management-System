package com.example.gym_system.DTOs.request;




import com.example.gym_system.entity.Subscription;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.time.LocalDate;

@Data
public class SubscriptionRequest {
    @NotNull(message = "Member ID is required")
    private Long memberId;

    @NotNull(message = "Plan is required")
    private Subscription.SubscriptionPlan plan;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @Positive(message = "Price must be positive")
    private Double price;
}
