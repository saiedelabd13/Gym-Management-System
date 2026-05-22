package com.example.gym_system.unit_test.service_test;




import com.example.gym_system.DTOs.request.GymClassRequest;
import com.example.gym_system.entity.GymClass;
import com.example.gym_system.entity.Member;
import com.example.gym_system.entity.Trainer;
import com.example.gym_system.exception.ResourceNotFoundException;
import com.example.gym_system.repository.GymClassRepository;
import com.example.gym_system.repository.MemberRepository;
import com.example.gym_system.repository.TrainerRepository;
import com.example.gym_system.service.GymClassService;
import com.example.gym_system.unit_test.TestDataBuilder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GymClassService Unit Tests")
class GymClassServiceTest {

    @Mock
    GymClassRepository gymClassRepository;
    @Mock
    TrainerRepository trainerRepository;
    @Mock
    MemberRepository memberRepository;
    @InjectMocks
    GymClassService gymClassService;

    private Trainer trainer;
    private GymClass gymClass;
    private Member member;
    private GymClassRequest classRequest;

    @BeforeEach
    void setUp() {
        trainer = TestDataBuilder.buildTrainer();
        trainer.setId(1L);

        gymClass = TestDataBuilder.buildGymClass(trainer);
        gymClass.setId(1L);
        gymClass.setEnrolledMembers(new ArrayList<>());

        member = TestDataBuilder.buildMember();
        member.setId(1L);

        classRequest = new GymClassRequest();
        classRequest.setName("Morning Yoga");
        classRequest.setDescription("Yoga session");
        classRequest.setStartTime(LocalDateTime.now().plusDays(1).withHour(8));
        classRequest.setEndTime(LocalDateTime.now().plusDays(1).withHour(9));
        classRequest.setCapacity(20);
        classRequest.setTrainerId(1L);
    }

    @Test
    @DisplayName("✅ should create class when trainer exists")
    void shouldCreateClass() {
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer));
        when(gymClassRepository.save(any())).thenReturn(gymClass);

        GymClass result = gymClassService.createClass(classRequest);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Morning Yoga");
        assertThat(result.getCapacity()).isEqualTo(20);
        assertThat(result.getTrainer().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("❌ should throw when trainer not found on create")
    void shouldThrowWhenTrainerNotFound() {
        when(trainerRepository.findById(99L)).thenReturn(Optional.empty());
        classRequest.setTrainerId(99L);

        assertThatThrownBy(() -> gymClassService.createClass(classRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("✅ should return all classes")
    void shouldGetAllClasses() {
        when(gymClassRepository.findAll()).thenReturn(List.of(gymClass));

        List<GymClass> result = gymClassService.getAllClasses();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Morning Yoga");
    }

    @Test
    @DisplayName("✅ should enroll member when class has capacity")
    void shouldEnrollMember() {
        when(gymClassRepository.findById(1L)).thenReturn(Optional.of(gymClass));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(gymClassRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        GymClass result = gymClassService.enrollMember(1L, 1L);

        assertThat(result.getEnrolledCount()).isEqualTo(1);
        assertThat(result.getEnrolledMembers()).contains(member);
    }

    @Test
    @DisplayName("❌ should throw when class is full")
    void shouldThrowWhenClassFull() {
        gymClass.setCapacity(0); // full
        when(gymClassRepository.findById(1L)).thenReturn(Optional.of(gymClass));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> gymClassService.enrollMember(1L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("full");
    }

    @Test
    @DisplayName("✅ should unenroll member and decrease count")
    void shouldUnenrollMember() {
        gymClass.getEnrolledMembers().add(member);
        gymClass.setEnrolledCount(1);
        when(gymClassRepository.findById(1L)).thenReturn(Optional.of(gymClass));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(gymClassRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        GymClass result = gymClassService.unenrollMember(1L, 1L);

        assertThat(result.getEnrolledCount()).isZero();
        assertThat(result.getEnrolledMembers()).doesNotContain(member);
    }

    @Test
    @DisplayName("✅ should return classes by trainer")
    void shouldReturnClassesByTrainer() {
        when(gymClassRepository.findByTrainerId(1L)).thenReturn(List.of(gymClass));

        List<GymClass> result = gymClassService.getClassesByTrainer(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("✅ should delete class")
    void shouldDeleteClass() {
        when(gymClassRepository.existsById(1L)).thenReturn(true);
        doNothing().when(gymClassRepository).deleteById(1L);

        assertThatCode(() -> gymClassService.deleteClass(1L)).doesNotThrowAnyException();
        verify(gymClassRepository).deleteById(1L);
    }
}
