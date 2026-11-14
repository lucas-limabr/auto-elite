package com.concessionaria.repository;

import com.concessionaria.entity.TestDriveAppointment;
import com.concessionaria.enums.TestDriveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TestDriveAppointmentRepository extends JpaRepository<TestDriveAppointment, Long> {
    List<TestDriveAppointment> findByCustomerId(Long customerId);
    List<TestDriveAppointment> findByVehicleId(Long vehicleId);
    List<TestDriveAppointment> findByStatus(TestDriveStatus status);

    @Query("SELECT COUNT(a) FROM TestDriveAppointment a WHERE a.vehicle.branch.id = :branchId AND a.scheduledDateTime >= :startOfDay AND a.scheduledDateTime < :endOfDay AND a.status IN ('PENDING', 'APPROVED')")
    long countAppointmentsForBranchOnDate(@Param("branchId") Long branchId, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT a FROM TestDriveAppointment a WHERE a.vehicle.id = :vehicleId AND a.scheduledDateTime >= :start AND a.scheduledDateTime <= :end AND a.status IN ('PENDING', 'APPROVED')")
    List<TestDriveAppointment> findConflictingAppointments(@Param("vehicleId") Long vehicleId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(a) FROM TestDriveAppointment a WHERE a.customer.id = :customerId AND a.status = 'CANCELED' AND a.scheduledDateTime >= :since")
    long countCustomerRecentCancellations(@Param("customerId") Long customerId, @Param("since") LocalDateTime since);
}

