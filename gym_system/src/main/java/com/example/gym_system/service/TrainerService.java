package com.example.gym_system.service;




import com.example.gym_system.DTOs.request.TrainerRequest;
import com.example.gym_system.entity.Trainer;
import com.example.gym_system.exception.DuplicateResourceException;
import com.example.gym_system.exception.ResourceNotFoundException;
import com.example.gym_system.repository.TrainerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TrainerService {

    private final TrainerRepository trainerRepository;

    public Trainer createTrainer(TrainerRequest request) {
        if (trainerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }
        Trainer trainer = Trainer.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .specialization(request.getSpecialization())
                .bio(request.getBio())
                .salaryPerHour(request.getSalaryPerHour())
                .build();
        return trainerRepository.save(trainer);
    }

    @Transactional(readOnly = true)
    public List<Trainer> getAllTrainers() {
        return trainerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Trainer getTrainerById(Long id) {
        return trainerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer", id));
    }

    public Trainer updateTrainer(Long id, TrainerRequest request) {
        Trainer trainer = getTrainerById(id);
        if (!trainer.getEmail().equals(request.getEmail()) && trainerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }
        trainer.setFirstName(request.getFirstName());
        trainer.setLastName(request.getLastName());
        trainer.setEmail(request.getEmail());
        trainer.setPhone(request.getPhone());
        trainer.setSpecialization(request.getSpecialization());
        trainer.setBio(request.getBio());
        trainer.setSalaryPerHour(request.getSalaryPerHour());
        return trainerRepository.save(trainer);
    }

    public void deleteTrainer(Long id) {
        if (!trainerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Trainer", id);
        }
        trainerRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Trainer> getActiveTrainers() {
        return trainerRepository.findByStatus(Trainer.TrainerStatus.ACTIVE);
    }
}
