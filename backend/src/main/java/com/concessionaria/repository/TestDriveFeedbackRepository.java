package com.concessionaria.repository;

import com.concessionaria.entity.TestDriveFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TestDriveFeedbackRepository extends JpaRepository<TestDriveFeedback, Long> {
    Optional<TestDriveFeedback> findByAppointmentId(Long appointmentId);
    boolean existsByAppointmentId(Long appointmentId);
}

