package com.profession.suggest.database.repositories.applicant;

import com.profession.suggest.database.entities.users.applicant.Applicant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicantRepository extends JpaRepository<Applicant, Long> {
    Optional<Applicant> findByAccountId(Long accountId);
    Optional<Applicant> findByAccountEmail(String email);
    Page<Applicant> findByCompanyId(Long companyId, Pageable pageable);
}
