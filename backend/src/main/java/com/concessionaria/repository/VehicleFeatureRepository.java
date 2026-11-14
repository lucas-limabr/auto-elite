package com.concessionaria.repository;

import com.concessionaria.entity.VehicleFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehicleFeatureRepository extends JpaRepository<VehicleFeature, Long> {
    Optional<VehicleFeature> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}

