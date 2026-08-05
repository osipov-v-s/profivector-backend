package com.profession.suggest.controllers.dataanalys.psychtests;

import com.profession.suggest.configuration.security.annotation.HasRole;
import com.profession.suggest.database.entities.auth.role.RoleEnum;
import com.profession.suggest.database.services.auth.AccountService;
import com.profession.suggest.database.services.dataanalys.psychtests.PsychTestService;
import com.profession.suggest.dto.dataanalys.psychtests.AccountTestsDTO;
import com.profession.suggest.dto.dataanalys.psychtests.PsychTestDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/psych-tests")
public class PsychTestsController {
    private final PsychTestService psychTestService;
    //private final PsychTestTypeService psychTestTypeService; admin etc
    private final AccountService accountService;
    public PsychTestsController(PsychTestService psychTestService, AccountService accountService) {
        this.psychTestService = psychTestService;
        this.accountService = accountService;
    }
    // Psychological test results are stored for Applicant or Specialist profiles.
    @HasRole({RoleEnum.APPLICANT, RoleEnum.SPECIALIST})
    @PostMapping("/create-test")
    public ResponseEntity<PsychTestDTO> createPsychTest(@RequestBody PsychTestDTO requestDTO, @RequestAttribute("accountId") Long accountId) throws AccountNotFoundException {
        return ResponseEntity.ok(
                psychTestService.createPsychTest(
                        requestDTO,
                        accountService.getAccountById(accountId)));
    }
    @HasRole({RoleEnum.APPLICANT, RoleEnum.SPECIALIST})
    @GetMapping("/my-tests")
    public ResponseEntity<List<PsychTestDTO>> getTestsForAccount(@RequestAttribute("accountId") Long accountId) throws AccountNotFoundException {
        return ResponseEntity.ok(psychTestService.getTestsResultsByAccount(accountService.getAccountById(accountId)));
    }
    @HasRole({RoleEnum.APPLICANT, RoleEnum.SPECIALIST})
    @GetMapping("/my-recent-tests")
    public ResponseEntity<Map<String, PsychTestDTO>> getAccountRecentTests(@RequestAttribute("accountId") Long accountId) throws AccountNotFoundException {
        return ResponseEntity.ok(psychTestService.getAccountRecentTests(accountService.getAccountById(accountId)));
    }
    @HasRole({RoleEnum.APPLICANT, RoleEnum.SPECIALIST})
    @GetMapping("/my-tests/type/{testType}")
    public ResponseEntity<List<PsychTestDTO>> getTestsByType(@RequestAttribute("accountId") Long accountId, @PathVariable String testType) throws AccountNotFoundException {
        return ResponseEntity.ok(psychTestService.getAccountTestsByType(accountService.getAccountById(accountId), testType));
    }
    @HasRole({RoleEnum.ADMIN, RoleEnum.HR})
    @GetMapping("/completed-tests")
    public ResponseEntity<List<AccountTestsDTO>> getCompletedTestsByDates(@RequestParam("type") String type,
                                                                          @RequestParam("startDate") LocalDateTime startDate,
                                                                          @RequestParam("endDate") LocalDateTime endDate,
                                                                          @RequestAttribute("accountId") Long accountId) {
        try {
            var actor = accountService.getAccountById(accountId);
            boolean admin = actor.getRoles().stream().anyMatch(role -> role.getName() == RoleEnum.ADMIN);
            Long companyId = admin || actor.getCompany() == null ? null : actor.getCompany().getId();
            return ResponseEntity.ok(psychTestService.getCompletedTestsByDateRange(
                    type, startDate, endDate, companyId));
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }


}
