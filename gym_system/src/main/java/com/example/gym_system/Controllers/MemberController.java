package com.example.gym_system.Controllers;

import com.example.gym_system.DTOs.request.MemberRequest;
import com.example.gym_system.entity.Member;
import com.example.gym_system.DTOs.response.ApiResponse;
import com.example.gym_system.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<Member>> createMember(@Valid @RequestBody MemberRequest request) {
        Member member = memberService.createMember(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(member, "Member created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Member>>> getAllMembers() {
        return ResponseEntity.ok(ApiResponse.success(memberService.getAllMembers()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Member>> getMemberById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getMemberById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<Member>> updateMember(@PathVariable Long id,
                                                            @Valid @RequestBody MemberRequest request) {
        return ResponseEntity.ok(ApiResponse.success(memberService.updateMember(id, request), "Member updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Member deleted successfully"));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Member>>> searchMembers(@RequestParam String query) {
        return ResponseEntity.ok(ApiResponse.success(memberService.searchMembers(query)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Member>> updateStatus(@PathVariable Long id,
                                                            @RequestParam Member.MemberStatus status) {
        return ResponseEntity.ok(ApiResponse.success(memberService.updateStatus(id, status), "Status updated"));
    }
}
