package com.gpsit.sistema.leads.repository;

import com.gpsit.sistema.leads.domain.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {

    Optional<Lead> findByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);
}
