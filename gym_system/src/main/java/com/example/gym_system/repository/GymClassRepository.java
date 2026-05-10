package com.example.gym_system.repository;


import com.example.gym_system.entity.GymClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GymClassRepository extends JpaRepository<GymClass, Long> {
    List<GymClass> findByTrainerId(Long trainerId);
    List<GymClass> findByStatus(GymClass.ClassStatus status);
    List<GymClass> findByStartTimeBetween(LocalDateTime from, LocalDateTime to);
    List<GymClass> findByNameContainingIgnoreCase(String name);
}
