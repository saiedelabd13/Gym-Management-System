package com.example.gym_system.Controllers;

import com.example.gym_system.DTOs.request.TrainerRequest;
import com.example.gym_system.entity.Trainer;
import com.example.gym_system.DTOs.response.ApiResponse;
import com.example.gym_system.service.TrainerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trainers")
@RequiredArgsConstructor
public class TrainerController {

    private final TrainerService trainerService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Trainer>> createTrainer(@Valid @RequestBody TrainerRequest request) {
        Trainer trainer = trainerService.createTrainer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(trainer, "Trainer created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Trainer>>> getAllTrainers() {
        return ResponseEntity.ok(ApiResponse.success(trainerService.getAllTrainers()));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<Trainer>>> getActiveTrainers() {
        return ResponseEntity.ok(ApiResponse.success(trainerService.getActiveTrainers()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Trainer>> getTrainerById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(trainerService.getTrainerById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Trainer>> updateTrainer(@PathVariable Long id,
                                                              @Valid @RequestBody TrainerRequest request) {
        return ResponseEntity.ok(ApiResponse.success(trainerService.updateTrainer(id, request), "Trainer updated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTrainer(@PathVariable Long id) {
        trainerService.deleteTrainer(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Trainer deleted successfully"));
    }
}
