package com.profession.suggest.database.repositories.auth;

import com.profession.suggest.database.entities.auth.Account;
import com.profession.suggest.database.entities.auth.role.RoleEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Account findByEmail(String email);

    Page<Account> findByCompanyId(Long companyId, Pageable pageable);

    @Query("select distinct a from Account a join a.roles r " +
            "where a.company.id = :companyId and r.name = :role")
    Page<Account> findByCompanyIdAndRole(@Param("companyId") Long companyId,
                                         @Param("role") RoleEnum role,
                                         Pageable pageable);
}
