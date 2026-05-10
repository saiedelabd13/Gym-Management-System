package com.example.gym_system.repository;


import com.example.gym_system.entity.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {
    Optional<Trainer> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Trainer> findByStatus(Trainer.TrainerStatus status);
    List<Trainer> findBySpecializationContainingIgnoreCase(String specialization);
}
