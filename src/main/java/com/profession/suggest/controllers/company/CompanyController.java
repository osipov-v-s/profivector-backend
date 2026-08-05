package com.profession.suggest.controllers.company;

import com.profession.suggest.configuration.security.annotation.HasRole;
import com.profession.suggest.database.entities.auth.role.RoleEnum;
import com.profession.suggest.database.entities.users.specialist.Company;
import com.profession.suggest.database.services.specialist.CompanyService;
import com.profession.suggest.dto.company.CompanyDTO;
import com.profession.suggest.dto.company.CompanyMapper;
import com.profession.suggest.dto.company.Employee;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** TODO route create company*/
@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/api/company")
public class CompanyController {
    private final CompanyService companyService;
    private final CompanyMapper companyMapper;
    @HasRole(RoleEnum.ADMIN)
    @PostMapping()
    public ResponseEntity<?> createCompany(@RequestAttribute("accountId") Long accountId,
                                           @RequestBody CompanyDTO company) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(companyService.createCompany(company));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    @HasRole(RoleEnum.HR)
    @GetMapping()
    public ResponseEntity<?> getMyCompany(@RequestAttribute("accountId") Long accountId) {
        try {
            return ResponseEntity.ok(companyMapper.toDTO(companyService.getCompanyByAccountId(accountId)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error getting company: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }
    /**
     * Return all  employees from company depends on requested account
     *
     * */
    @HasRole(RoleEnum.HR)
    @GetMapping("/employees")
    public ResponseEntity<?> getCompanyEmployees (@RequestAttribute("accountId") Long accountId,
                                                    @RequestParam(required = false) RoleEnum role,
                                                    Pageable pageable) {
        try {
            Company company = companyService.getCompanyByAccountId(accountId);
            Page<Employee> employees = companyService.getEmployeesByCompanyId(company.getId(), role, pageable);
            return ResponseEntity.ok(employees);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error getting company specialists: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }
    @GetMapping("/companies-with-employees")
    @HasRole(RoleEnum.ADMIN)
    public ResponseEntity<?> getCompaniesWithHRs() {
        try {
            return ResponseEntity.ok(companyService.getCompaniesWithEmployees());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


}
