package com.concessionaria.repository;

import com.concessionaria.entity.Vehicle;
import com.concessionaria.enums.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long>, JpaSpecificationExecutor<Vehicle> {
    Optional<Vehicle> findByChassisNumber(String chassisNumber);
    boolean existsByChassisNumber(String chassisNumber);
    List<Vehicle> findByStatus(VehicleStatus status);
    List<Vehicle> findByBranchId(Long branchId);
}

