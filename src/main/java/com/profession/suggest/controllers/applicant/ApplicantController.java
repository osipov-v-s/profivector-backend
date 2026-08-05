package com.profession.suggest.controllers.applicant;

import com.profession.suggest.configuration.security.annotation.HasRole;
import com.profession.suggest.database.entities.auth.Account;
import com.profession.suggest.database.entities.auth.role.RoleEnum;
import com.profession.suggest.database.services.applicant.ApplicantService;
import com.profession.suggest.database.services.auth.AccountService;
import com.profession.suggest.database.services.dataanalys.prediction.PredictionService;
import com.profession.suggest.dto.applicant.ApplicantDTO;
import com.profession.suggest.dto.applicant.ApplicantResponseDTO;
import com.profession.suggest.dto.dataanalys.prediction.PredictionDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;
import java.util.List;

@RestController
@RequestMapping("/api/applicants")
@RequiredArgsConstructor
public class ApplicantController {
    private final ApplicantService applicantService;
    private final AccountService accountService;
    private final PredictionService predictionService;

    @HasRole({RoleEnum.ADMIN, RoleEnum.HR})
    @GetMapping
    public ResponseEntity<Page<ApplicantResponseDTO>> getApplicants(
            @RequestAttribute("accountId") Long accountId,
            Pageable pageable) throws AccountNotFoundException {
        Account actor = accountService.getAccountById(accountId);
        boolean admin = actor.getRoles().stream().anyMatch(role -> role.getName() == RoleEnum.ADMIN);
        if (admin) {
            return ResponseEntity.ok(applicantService.getAll(pageable));
        }
        if (actor.getCompany() == null) {
            throw new IllegalArgumentException("HR account has no company");
        }
        return ResponseEntity.ok(applicantService.getByCompany(actor.getCompany().getId(), pageable));
    }

    @HasRole(RoleEnum.APPLICANT)
    @GetMapping("/applicant-data")
    public ResponseEntity<ApplicantResponseDTO> getCurrentApplicant(
            @RequestAttribute("accountId") Long accountId) {
        return ResponseEntity.ok(applicantService.getResponseByAccountId(accountId));
    }

    @HasRole({RoleEnum.ADMIN, RoleEnum.HR, RoleEnum.APPLICANT})
    @PutMapping("/{id}")
    public ResponseEntity<ApplicantDTO> updateApplicant(
            @PathVariable Long id,
            @Valid @RequestBody ApplicantDTO dto,
            @RequestAttribute("accountId") Long accountId) throws AccountNotFoundException {
        return ResponseEntity.ok(applicantService.update(id, dto, accountService.getAccountById(accountId)));
    }

    @HasRole({RoleEnum.ADMIN, RoleEnum.HR})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateApplicant(
            @PathVariable Long id,
            @RequestAttribute("accountId") Long accountId) throws AccountNotFoundException {
        applicantService.deactivate(id, accountService.getAccountById(accountId));
        return ResponseEntity.noContent().build();
    }

    @HasRole(RoleEnum.APPLICANT)
    @GetMapping("/applicant/predictions")
    public ResponseEntity<List<PredictionDTO>> getPredictions(
            @RequestAttribute("accountId") Long accountId) {
        return ResponseEntity.ok(predictionService.getPredictionsByApplicantId(
                applicantService.getByAccountId(accountId).getId()));
    }
}
