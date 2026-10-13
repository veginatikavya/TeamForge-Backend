package com.teamforge.repository;

import com.teamforge.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    // Find an account using its login email
    Optional<Account> findByEmail(String email);

    // Check whether an email is already registered
    boolean existsByEmail(String email);

    // Find the login account linked to a candidate
    Optional<Account> findByCandidate_Id(Long candidateId);
}