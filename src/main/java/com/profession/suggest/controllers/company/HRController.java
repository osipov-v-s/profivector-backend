package com.profession.suggest.controllers.company;

import com.profession.suggest.configuration.security.annotation.HasRole;
import com.profession.suggest.database.entities.auth.role.RoleEnum;
import com.profession.suggest.database.services.auth.AccountService;
import com.profession.suggest.database.services.specialist.CompanyService;
import com.profession.suggest.dto.company.CreateEmployeeRequest;
import lombok.AllArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/hr")
public class HRController {
    private final CompanyService companyService;
    private final AccountService accountService;
    /**
     * Admin creates HR with company
     */
    @HasRole(RoleEnum.ADMIN)
    @PostMapping
    public ResponseEntity<?> createHR(@RequestBody CreateEmployeeRequest request) {
        try {
            // Force role to HR for this endpoint
            request.setRoles(List.of(RoleEnum.HR));
            var response = companyService.createEmployeeForCompany(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);  // ? 201 CREATED
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error, please check request");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error, please check request params");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Probably OGRN, INN or name duplicate error");
        }
    }
    /**
     * HR creates Specialist (regular employee) in their company
     */
    @HasRole({RoleEnum.HR, RoleEnum.ADMIN})
    @PostMapping("/employee")
    public ResponseEntity<?> createEmployee(
            @RequestAttribute("accountId") Long accountId,
            @RequestBody CreateEmployeeRequest request) {
        try {
            if (request.getRoles() == null || request.getRoles().isEmpty())
                request.setRoles(List.of(RoleEnum.SPECIALIST));
            var actor = accountService.getAccountById(accountId);
            boolean admin = actor.getRoles().stream()
                    .anyMatch(role -> role.getName() == RoleEnum.ADMIN);
            if (!admin) {
                if (actor.getCompany() == null) {
                    throw new IllegalArgumentException("HR account has no company");
                }
                request.setCompanyName(actor.getCompany().getName());
            } else if (request.getCompanyName() == null || request.getCompanyName().isBlank()) {
                throw new IllegalArgumentException("Company is required for admin-created employee");
            }
            var response = companyService.createEmployeeForCompany(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }
}
