package com.example.gym_system.unit_test.repository_test;

import com.example.gym_system.entity.Member;
import com.example.gym_system.repository.MemberRepository;
import com.example.gym_system.unit_test.TestDataBuilder;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("MemberRepository Slice Tests")
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("✅ should save and find member by ID")
    void shouldSaveAndFindById() {
        Member saved = memberRepository.save(TestDataBuilder.buildMember());

        Optional<Member> found = memberRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("ahmed.ali@test.com");
    }

    @Test
    @DisplayName("✅ should find member by email")
    void shouldFindByEmail() {
        memberRepository.save(TestDataBuilder.buildMember());

        Optional<Member> found = memberRepository.findByEmail("ahmed.ali@test.com");

        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Ahmed");
    }

    @Test
    @DisplayName("✅ existsByEmail returns true when email exists")
    void shouldReturnTrueWhenEmailExists() {
        memberRepository.save(TestDataBuilder.buildMember());

        assertThat(memberRepository.existsByEmail("ahmed.ali@test.com")).isTrue();
    }

    @Test
    @DisplayName("✅ existsByEmail returns false when email not exists")
    void shouldReturnFalseWhenEmailNotExists() {
        assertThat(memberRepository.existsByEmail("ghost@test.com")).isFalse();
    }

    @Test
    @DisplayName("✅ should find members by status ACTIVE")
    void shouldFindByStatus() {
        Member active   = TestDataBuilder.buildMember("Active",   "User", "active@test.com");
        Member inactive = TestDataBuilder.buildMember("Inactive", "User", "inactive@test.com");
        inactive.setStatus(Member.MemberStatus.INACTIVE);

        memberRepository.save(active);
        memberRepository.save(inactive);

        List<Member> activeMembers = memberRepository.findByStatus(Member.MemberStatus.ACTIVE);

        assertThat(activeMembers).hasSize(1);
        assertThat(activeMembers.get(0).getEmail()).isEqualTo("active@test.com");
    }

    @Test
    @DisplayName("✅ should search members by first name (case-insensitive)")
    void shouldSearchByFirstName() {
        memberRepository.save(TestDataBuilder.buildMember("Ahmed",  "Ali",    "ahmed@test.com"));
        memberRepository.save(TestDataBuilder.buildMember("Mohamed","Hassan", "moh@test.com"));

        List<Member> results = memberRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("ahmed", "ahmed");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getFirstName()).isEqualTo("Ahmed");
    }

    @Test
    @DisplayName("✅ should search members by last name (case-insensitive)")
    void shouldSearchByLastName() {
        memberRepository.save(TestDataBuilder.buildMember("Ahmed", "Ali",    "a@test.com"));
        memberRepository.save(TestDataBuilder.buildMember("Sara",  "Hassan", "s@test.com"));

        List<Member> results = memberRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("HASSAN", "HASSAN");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getLastName()).isEqualTo("Hassan");
    }

    @Test
    @DisplayName("✅ should enforce unique email constraint")
    void shouldEnforceUniqueEmail() {
        memberRepository.save(TestDataBuilder.buildMember());

        Member duplicate = TestDataBuilder.buildMember("Other", "User", "ahmed.ali@test.com");

        assertThatThrownBy(() -> {
            memberRepository.save(duplicate);
            memberRepository.flush();
        }).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("✅ should delete member by ID")
    void shouldDeleteMember() {
        Member saved = memberRepository.save(TestDataBuilder.buildMember());

        memberRepository.deleteById(saved.getId());

        assertThat(memberRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    @DisplayName("✅ should update member fields")
    void shouldUpdateMember() {
        Member saved = memberRepository.save(TestDataBuilder.buildMember());
        saved.setFirstName("Updated");
        saved.setAddress("Alexandria");

        Member updated = memberRepository.save(saved);

        assertThat(updated.getFirstName()).isEqualTo("Updated");
        assertThat(updated.getAddress()).isEqualTo("Alexandria");
    }
}
