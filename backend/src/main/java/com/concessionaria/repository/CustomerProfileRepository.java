package com.concessionaria.repository;

import com.concessionaria.entity.CustomerProfile;
import com.concessionaria.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, Long> {
    Optional<CustomerProfile> findByUser(User user);
    Optional<CustomerProfile> findByUserId(Long userId);
    boolean existsByCpf(String cpf);
    boolean existsByCnhNumber(String cnhNumber);
}

