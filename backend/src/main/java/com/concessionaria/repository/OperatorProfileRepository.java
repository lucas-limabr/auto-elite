package com.concessionaria.repository;

import com.concessionaria.entity.OperatorProfile;
import com.concessionaria.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OperatorProfileRepository extends JpaRepository<OperatorProfile, Long> {
    Optional<OperatorProfile> findByUser(User user);
    Optional<OperatorProfile> findByUserId(Long userId);
    Optional<OperatorProfile> findByUserEmail(String email);
    Optional<OperatorProfile> findByRegistrationCode(String registrationCode);
    boolean existsByRegistrationCode(String registrationCode);
    List<OperatorProfile> findByDealershipBranchId(Long branchId);
}
