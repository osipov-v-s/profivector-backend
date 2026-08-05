package com.profession.suggest.database.services.dataanalys.psychtests;

import com.profession.suggest.database.entities.auth.Account;
import com.profession.suggest.database.entities.auth.role.Role;
import com.profession.suggest.database.entities.auth.role.RoleEnum;
import com.profession.suggest.database.entities.dataanalys.psychtests.PsychParam;
import com.profession.suggest.database.entities.dataanalys.psychtests.PsychTest;
import com.profession.suggest.database.entities.dataanalys.psychtests.PsychTestType;
import com.profession.suggest.database.entities.users.User;
import com.profession.suggest.database.entities.users.applicant.Applicant;
import com.profession.suggest.database.entities.users.specialist.Specialist;
import com.profession.suggest.database.repositories.dataanalys.psychtests.PsychTestRepository;
import com.profession.suggest.dto.dataanalys.psychtests.AccountTestsDTO;
import com.profession.suggest.dto.dataanalys.psychtests.PsychTestDTO;
import com.profession.suggest.dto.dataanalys.psychtests.PsychTestMapper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class PsychTestService {
    private final PsychTestRepository repository;
    private final PsychParamService psychParamService;
    private final PsychTestTypeService psychTestTypeService;
    private final PsychTestMapper mapper;

    @Transactional
    public PsychTestDTO createPsychTest(PsychTestDTO psychTestDTO, Account account) {
        validateAccountRoles(account);
        validateRequest(psychTestDTO);

        PsychTestType psychTestType = psychTestTypeService.getPsychTestTypeByName(psychTestDTO.getTestTypeName());
        PsychTest psychTest = mapper.fromDTO(psychTestDTO);
        Set<PsychParam> params = new HashSet<>();
        for (PsychParam param : psychTest.getPsychParams()) {
            PsychParam savedParam = psychParamService.create(param);
            params.add(savedParam);
        }
        psychTest.setPsychParams(params);
        psychTest.setPsychTestType(psychTestType);
        psychTest.setApplicant(account.getApplicant());
        psychTest.setSpecialist(account.getSpecialist());

        return mapper.toDTO(repository.save(psychTest));
    }
    public List<PsychTestDTO> getTestsResultsByAccount(Account account) {
        validateAccountRoles(account);
        List<PsychTest> psychTests = repository.findByAccountId(account.getId());
        return psychTests.stream().filter(test -> test.getPsychTestType() != null).
                map(mapper::toDTO)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    public List<PsychTestDTO> getAccountTestsByType(Account account, String testTypeName) {
        validateAccountRoles(account);
        List<PsychTest> psychTests = repository.findByAccountIdAndTestType(account.getId(), testTypeName);
        return psychTests.stream().map(mapper::toDTO).collect(Collectors.toList());
    }
    public List<AccountTestsDTO> getCompletedTestsByDateRange(
            String type, LocalDateTime startDate, LocalDateTime endDate, Long companyId) {
        List<PsychTest> psychTests;
        if (Objects.equals(type, "Applicant"))
            psychTests = repository.findByApplicantAndDateRange(startDate, endDate);
        else if (Objects.equals(type, "Specialist"))
            psychTests = repository.findBySpecialistAndDateRange(startDate, endDate);
        else
            psychTests = repository.findByDateRange(startDate, endDate);
        Map<Long, AccountTestsDTO> accountMap = new LinkedHashMap<>();
        for (PsychTest test: psychTests) {
            User user = test.getApplicant() != null ? test.getApplicant() : test.getSpecialist();
            if (user == null) continue;
            if (companyId != null && (user.getAccount().getCompany() == null
                    || !companyId.equals(user.getAccount().getCompany().getId()))) continue;
            Long accountId = user.getAccount().getId();

            AccountTestsDTO accountDTO = accountMap.computeIfAbsent(accountId,
                    id -> {
                        AccountTestsDTO dto = new AccountTestsDTO();
                        dto.setAccountId(accountId);
                        dto.setEmail(user.getAccount().getEmail());
                        dto.setName(user.getName());
                        dto.setSurname(user.getSurname());
                        dto.setPatronymic(user.getPatronymic());
                        dto.setFullName(user.getFullName());
                        dto.setRoles(user.getAccount().getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
                        dto.setPsychTests(new ArrayList<>());
                        return dto;
                    });
            accountDTO.getPsychTests().add(mapper.toDTO(test));
        }
        return new ArrayList<>(accountMap.values());
    }
    /**
     * Search tests only for the most recent date and only one testType
     * Works good if tests < 1000 mean always
     * */
    public Map<String, PsychTestDTO> getAccountRecentTests(Account account) {
        validateAccountRoles(account);
        List<PsychTest> psychTests = repository.findByAccountId(account.getId()).stream()
                .filter(test -> test.getPsychTestType() != null)
                .toList();
        return psychTests.stream().collect(Collectors.toMap(
                test -> test.getPsychTestType().getName(),
                test -> mapper.toDTO(test),
                (existing, replacement) -> existing.getCreatedAt().isAfter(replacement.getCreatedAt()) ? existing : replacement
        ));
    }
    private void validateAccountRoles(Account account) {
        Applicant applicant = account.getApplicant();
        Specialist specialist = account.getSpecialist();
        boolean applicantRole = account.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleEnum.APPLICANT);
        boolean specialistRole = account.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleEnum.SPECIALIST);
        if (applicantRole == specialistRole) {
            throw new IllegalArgumentException("Exactly one test participant role is required");
        }
        if (applicantRole && applicant == null) {
            throw new IllegalArgumentException("Applicant profile is missing");
        }
        if (specialistRole && specialist == null) {
            throw new IllegalArgumentException("Specialist profile is missing");
        }
    }
    private void validateRequest(PsychTestDTO psychTestDTO) {
        if (psychTestDTO.getPsychParams() == null)
            throw new IllegalArgumentException("psychParams list is null or empty");
        if (psychTestDTO.getTestTypeName() == null || psychTestDTO.getTestTypeName().isBlank())
            throw new IllegalArgumentException("TestTypeName is null");
    }
}
