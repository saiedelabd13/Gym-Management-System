package com.example.gym_system.Controllers;


import com.example.gym_system.DTOs.request.GymClassRequest;
import com.example.gym_system.entity.GymClass;
import com.example.gym_system.DTOs.response.ApiResponse;
import com.example.gym_system.service.GymClassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class GymClassController {

    private final GymClassService gymClassService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<GymClass>> createClass(@Valid @RequestBody GymClassRequest request) {
        GymClass gymClass = gymClassService.createClass(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(gymClass, "Class created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GymClass>>> getAllClasses() {
        return ResponseEntity.ok(ApiResponse.success(gymClassService.getAllClasses()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GymClass>> getClassById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(gymClassService.getClassById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<GymClass>> updateClass(@PathVariable Long id,
                                                             @Valid @RequestBody GymClassRequest request) {
        return ResponseEntity.ok(ApiResponse.success(gymClassService.updateClass(id, request), "Class updated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteClass(@PathVariable Long id) {
        gymClassService.deleteClass(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Class deleted successfully"));
    }

    @PostMapping("/{classId}/enroll/{memberId}")
    public ResponseEntity<ApiResponse<GymClass>> enrollMember(@PathVariable Long classId,
                                                              @PathVariable Long memberId) {
        return ResponseEntity.ok(ApiResponse.success(gymClassService.enrollMember(classId, memberId), "Member enrolled"));
    }

    @DeleteMapping("/{classId}/unenroll/{memberId}")
    public ResponseEntity<ApiResponse<GymClass>> unenrollMember(@PathVariable Long classId,
                                                                @PathVariable Long memberId) {
        return ResponseEntity.ok(ApiResponse.success(gymClassService.unenrollMember(classId, memberId), "Member unenrolled"));
    }

    @GetMapping("/trainer/{trainerId}")
    public ResponseEntity<ApiResponse<List<GymClass>>> getClassesByTrainer(@PathVariable Long trainerId) {
        return ResponseEntity.ok(ApiResponse.success(gymClassService.getClassesByTrainer(trainerId)));
    }
}
