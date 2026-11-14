package com.concessionaria.repository;

import com.concessionaria.entity.DealershipBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DealershipBranchRepository extends JpaRepository<DealershipBranch, Long> {
    Optional<DealershipBranch> findByCnpj(String cnpj);
    boolean existsByCnpj(String cnpj);
}

