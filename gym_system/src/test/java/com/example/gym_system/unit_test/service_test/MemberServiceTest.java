package com.example.gym_system.unit_test.service_test;



import com.example.gym_system.DTOs.request.MemberRequest;
import com.example.gym_system.entity.Member;
import com.example.gym_system.exception.DuplicateResourceException;
import com.example.gym_system.exception.ResourceNotFoundException;
import com.example.gym_system.repository.MemberRepository;
import com.example.gym_system.service.MemberService;
import com.example.gym_system.unit_test.TestDataBuilder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberService Unit Tests")
class MemberServiceTest {

    @Mock
    MemberRepository memberRepository;
    @InjectMocks
    MemberService memberService;

    private Member member;
    private MemberRequest memberRequest;

    @BeforeEach
    void setUp() {
        member = TestDataBuilder.buildMember();
        member.setId(1L);

        memberRequest = new MemberRequest();
        memberRequest.setFirstName("Ahmed");
        memberRequest.setLastName("Ali");
        memberRequest.setEmail("ahmed.ali@test.com");
        memberRequest.setPhone("01234567890");
        memberRequest.setDateOfBirth(LocalDate.of(1992, 5, 15));
        memberRequest.setGender(Member.Gender.MALE);
        memberRequest.setAddress("Cairo, Egypt");
    }

    // ── CREATE ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createMember()")
    class CreateMemberTests {

        @Test
        @DisplayName("✅ should create member when email is unique")
        void shouldCreateMember_whenEmailIsUnique() {
            when(memberRepository.existsByEmail(anyString())).thenReturn(false);
            when(memberRepository.save(any(Member.class))).thenReturn(member);

            Member result = memberService.createMember(memberRequest);

            assertThat(result).isNotNull();
            assertThat(result.getFirstName()).isEqualTo("Ahmed");
            assertThat(result.getEmail()).isEqualTo("ahmed.ali@test.com");
            assertThat(result.getStatus()).isEqualTo(Member.MemberStatus.ACTIVE);

            verify(memberRepository).existsByEmail("ahmed.ali@test.com");
            verify(memberRepository).save(any(Member.class));
        }

        @Test
        @DisplayName("❌ should throw DuplicateResourceException when email already exists")
        void shouldThrowDuplicateException_whenEmailExists() {
            when(memberRepository.existsByEmail(anyString())).thenReturn(true);

            assertThatThrownBy(() -> memberService.createMember(memberRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("ahmed.ali@test.com");

            verify(memberRepository, never()).save(any());
        }
    }

    // ── READ ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getAll / getById()")
    class ReadMemberTests {

        @Test
        @DisplayName("✅ should return all members")
        void shouldReturnAllMembers() {
            Member m2 = TestDataBuilder.buildMember("Sara", "Hassan", "sara@test.com");
            m2.setId(2L);
            when(memberRepository.findAll()).thenReturn(List.of(member, m2));

            List<Member> result = memberService.getAllMembers();

            assertThat(result).hasSize(2);
            assertThat(result).extracting(Member::getEmail)
                    .containsExactlyInAnyOrder("ahmed.ali@test.com", "sara@test.com");
        }

        @Test
        @DisplayName("✅ should return member by ID")
        void shouldReturnMemberById() {
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

            Member result = memberService.getMemberById(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getFirstName()).isEqualTo("Ahmed");
        }

        @Test
        @DisplayName("❌ should throw ResourceNotFoundException when member not found")
        void shouldThrowNotFoundException_whenMemberNotFound() {
            when(memberRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> memberService.getMemberById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // ── UPDATE ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateMember()")
    class UpdateMemberTests {

        @Test
        @DisplayName("✅ should update member fields correctly")
        void shouldUpdateMember() {
            memberRequest.setFirstName("Ahmed Updated");
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
            when(memberRepository.existsByEmail(anyString())).thenReturn(false);
            when(memberRepository.save(any(Member.class))).thenAnswer(i -> i.getArgument(0));

            Member result = memberService.updateMember(1L, memberRequest);

            assertThat(result.getFirstName()).isEqualTo("Ahmed Updated");
            verify(memberRepository).save(any(Member.class));
        }

        @Test
        @DisplayName("❌ should throw DuplicateResourceException when new email already used by another member")
        void shouldThrowDuplicate_whenNewEmailTakenByOther() {
            memberRequest.setEmail("other@test.com"); // different email
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
            when(memberRepository.existsByEmail("other@test.com")).thenReturn(true);

            assertThatThrownBy(() -> memberService.updateMember(1L, memberRequest))
                    .isInstanceOf(DuplicateResourceException.class);

            verify(memberRepository, never()).save(any());
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteMember()")
    class DeleteMemberTests {

        @Test
        @DisplayName("✅ should delete member when exists")
        void shouldDeleteMember() {
            when(memberRepository.existsById(1L)).thenReturn(true);
            doNothing().when(memberRepository).deleteById(1L);

            assertThatCode(() -> memberService.deleteMember(1L)).doesNotThrowAnyException();
            verify(memberRepository).deleteById(1L);
        }

        @Test
        @DisplayName("❌ should throw ResourceNotFoundException when member does not exist")
        void shouldThrowNotFound_whenDeletingNonExisting() {
            when(memberRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> memberService.deleteMember(99L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(memberRepository, never()).deleteById(any());
        }
    }

    // ── STATUS ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateStatus()")
    class UpdateStatusTests {

        @Test
        @DisplayName("✅ should suspend member")
        void shouldSuspendMember() {
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
            when(memberRepository.save(any(Member.class))).thenAnswer(i -> i.getArgument(0));

            Member result = memberService.updateStatus(1L, Member.MemberStatus.SUSPENDED);

            assertThat(result.getStatus()).isEqualTo(Member.MemberStatus.SUSPENDED);
        }
    }

    // ── SEARCH ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("✅ searchMembers should delegate to repository with correct query")
    void shouldSearchMembers() {
        when(memberRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("Ahmed","Ahmed"))
                .thenReturn(List.of(member));

        List<Member> result = memberService.searchMembers("Ahmed");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("Ahmed");
    }
}
