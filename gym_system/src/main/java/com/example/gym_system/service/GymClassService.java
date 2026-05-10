package com.example.gym_system.service;


import com.example.gym_system.DTOs.request.GymClassRequest;
import com.example.gym_system.entity.GymClass;
import com.example.gym_system.entity.Member;
import com.example.gym_system.entity.Trainer;
import com.example.gym_system.exception.ResourceNotFoundException;
import com.example.gym_system.repository.GymClassRepository;
import com.example.gym_system.repository.MemberRepository;
import com.example.gym_system.repository.TrainerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GymClassService {

    private final GymClassRepository gymClassRepository;
    private final TrainerRepository trainerRepository;
    private final MemberRepository memberRepository;

    public GymClass createClass(GymClassRequest request) {
        Trainer trainer = trainerRepository.findById(request.getTrainerId())
                .orElseThrow(() -> new ResourceNotFoundException("Trainer", request.getTrainerId()));

        GymClass gymClass = GymClass.builder()
                .name(request.getName())
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .capacity(request.getCapacity())
                .trainer(trainer)
                .build();
        return gymClassRepository.save(gymClass);
    }

    @Transactional(readOnly = true)
    public List<GymClass> getAllClasses() {
        return gymClassRepository.findAll();
    }

    @Transactional(readOnly = true)
    public GymClass getClassById(Long id) {
        return gymClassRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GymClass", id));
    }

    public GymClass updateClass(Long id, GymClassRequest request) {
        GymClass gymClass = getClassById(id);
        Trainer trainer = trainerRepository.findById(request.getTrainerId())
                .orElseThrow(() -> new ResourceNotFoundException("Trainer", request.getTrainerId()));

        gymClass.setName(request.getName());
        gymClass.setDescription(request.getDescription());
        gymClass.setStartTime(request.getStartTime());
        gymClass.setEndTime(request.getEndTime());
        gymClass.setCapacity(request.getCapacity());
        gymClass.setTrainer(trainer);
        return gymClassRepository.save(gymClass);
    }

    public void deleteClass(Long id) {
        if (!gymClassRepository.existsById(id)) {
            throw new ResourceNotFoundException("GymClass", id);
        }
        gymClassRepository.deleteById(id);
    }

    public GymClass enrollMember(Long classId, Long memberId) {
        GymClass gymClass = getClassById(classId);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member", memberId));

        if (gymClass.getEnrolledCount() >= gymClass.getCapacity()) {
            throw new RuntimeException("Class is full");
        }
        if (!gymClass.getEnrolledMembers().contains(member)) {
            gymClass.getEnrolledMembers().add(member);
            gymClass.setEnrolledCount(gymClass.getEnrolledCount() + 1);
        }
        return gymClassRepository.save(gymClass);
    }

    public GymClass unenrollMember(Long classId, Long memberId) {
        GymClass gymClass = getClassById(classId);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member", memberId));

        gymClass.getEnrolledMembers().remove(member);
        gymClass.setEnrolledCount(Math.max(0, gymClass.getEnrolledCount() - 1));
        return gymClassRepository.save(gymClass);
    }

    @Transactional(readOnly = true)
    public List<GymClass> getClassesByTrainer(Long trainerId) {
        return gymClassRepository.findByTrainerId(trainerId);
    }
}
