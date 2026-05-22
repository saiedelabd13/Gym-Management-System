package com.example.gym_system.unit_test.service_test;



import com.example.gym_system.DTOs.request.TrainerRequest;
import com.example.gym_system.entity.Trainer;
import com.example.gym_system.exception.DuplicateResourceException;
import com.example.gym_system.exception.ResourceNotFoundException;
import com.example.gym_system.repository.TrainerRepository;
import com.example.gym_system.service.TrainerService;
import com.example.gym_system.unit_test.TestDataBuilder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainerService Unit Tests")
class TrainerServiceTest {

    @Mock
    TrainerRepository trainerRepository;
    @InjectMocks
    TrainerService trainerService;

    private Trainer trainer;
    private TrainerRequest trainerRequest;

    @BeforeEach
    void setUp() {
        trainer = TestDataBuilder.buildTrainer();
        trainer.setId(1L);

        trainerRequest = new TrainerRequest();
        trainerRequest.setFirstName("Mohamed");
        trainerRequest.setLastName("Hassan");
        trainerRequest.setEmail("trainer@test.com");
        trainerRequest.setPhone("01098765432");
        trainerRequest.setSpecialization("Weightlifting");
        trainerRequest.setBio("10 years experience");
        trainerRequest.setSalaryPerHour(200.0);
    }

    @Test
    @DisplayName("✅ should create trainer when email is unique")
    void shouldCreateTrainer() {
        when(trainerRepository.existsByEmail(anyString())).thenReturn(false);
        when(trainerRepository.save(any())).thenReturn(trainer);

        Trainer result = trainerService.createTrainer(trainerRequest);

        assertThat(result).isNotNull();
        assertThat(result.getSpecialization()).isEqualTo("Weightlifting");
        assertThat(result.getSalaryPerHour()).isEqualTo(200.0);
        verify(trainerRepository).save(any(Trainer.class));
    }

    @Test
    @DisplayName("❌ should throw DuplicateResourceException when trainer email exists")
    void shouldThrowDuplicate_whenEmailExists() {
        when(trainerRepository.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> trainerService.createTrainer(trainerRequest))
                .isInstanceOf(DuplicateResourceException.class);

        verify(trainerRepository, never()).save(any());
    }

    @Test
    @DisplayName("✅ should return all trainers")
    void shouldGetAllTrainers() {
        Trainer t2 = TestDataBuilder.buildTrainer("yoga@test.com", "Yoga");
        t2.setId(2L);
        when(trainerRepository.findAll()).thenReturn(List.of(trainer, t2));

        List<Trainer> result = trainerService.getAllTrainers();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Trainer::getSpecialization)
                .containsExactlyInAnyOrder("Weightlifting", "Yoga");
    }

    @Test
    @DisplayName("✅ should return only active trainers")
    void shouldGetActiveTrainers() {
        when(trainerRepository.findByStatus(Trainer.TrainerStatus.ACTIVE))
                .thenReturn(List.of(trainer));

        List<Trainer> result = trainerService.getActiveTrainers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(Trainer.TrainerStatus.ACTIVE);
    }

    @Test
    @DisplayName("✅ should return trainer by ID")
    void shouldGetTrainerById() {
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer));

        Trainer result = trainerService.getTrainerById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("❌ should throw ResourceNotFoundException when trainer not found")
    void shouldThrowNotFound() {
        when(trainerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.getTrainerById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("✅ should update trainer")
    void shouldUpdateTrainer() {
        trainerRequest.setSalaryPerHour(350.0);
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer));
        when(trainerRepository.existsByEmail(anyString())).thenReturn(false);
        when(trainerRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Trainer result = trainerService.updateTrainer(1L, trainerRequest);

        assertThat(result.getSalaryPerHour()).isEqualTo(350.0);
    }

    @Test
    @DisplayName("✅ should delete trainer")
    void shouldDeleteTrainer() {
        when(trainerRepository.existsById(1L)).thenReturn(true);
        doNothing().when(trainerRepository).deleteById(1L);

        assertThatCode(() -> trainerService.deleteTrainer(1L)).doesNotThrowAnyException();
        verify(trainerRepository).deleteById(1L);
    }

    @Test
    @DisplayName("❌ should throw when deleting non-existing trainer")
    void shouldThrowWhenDeletingNotFound() {
        when(trainerRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> trainerService.deleteTrainer(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}