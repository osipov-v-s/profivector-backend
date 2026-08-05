package com.profession.suggest.database.services.specialist;

import com.profession.suggest.database.entities.auth.Account;
import com.profession.suggest.database.entities.auth.role.Role;
import com.profession.suggest.database.entities.auth.role.RoleEnum;
import com.profession.suggest.database.entities.users.applicant.Applicant;
import com.profession.suggest.database.entities.users.specialist.Company;
import com.profession.suggest.database.entities.users.specialist.Specialist;
import com.profession.suggest.database.repositories.specialist.CompanyRepository;
import com.profession.suggest.database.services.applicant.ApplicantService;
import com.profession.suggest.database.services.auth.AccountService;
import com.profession.suggest.dto.auth.AccountRegisterRequestDTO;
import com.profession.suggest.dto.company.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyService {
    private static final Set<RoleEnum> EMPLOYEE_ROLES =
            Set.of(RoleEnum.HR, RoleEnum.SPECIALIST, RoleEnum.APPLICANT);

    private final CompanyRepository repository;
    private final AccountService accountService;
    private final SpecialistService specialistService;
    private final ApplicantService applicantService;
    private final CreateEmployeeMapper createEmployeeMapper;

    public Company getCompanyByAccountId(Long accountId) throws AccountNotFoundException {
        Account account = accountService.getAccountById(accountId);
        if (account.getCompany() == null) {
            throw new IllegalArgumentException("No company assigned to this account");
        }
        return account.getCompany();
    }

    public Page<Employee> getEmployeesByCompanyId(
            Long companyId, RoleEnum role, Pageable pageable) {
        return accountService.getAccountsByCompany(companyId, role, pageable)
                .map(this::toEmployee);
    }

    public List<CompanyWithEmployeesDTO> getCompaniesWithEmployees() {
        return repository.findAll().stream().map(company -> {
            List<Employee> employees = company.getAccounts() == null
                    ? new ArrayList<>()
                    : company.getAccounts().stream().map(this::toEmployee).toList();
            return CompanyWithEmployeesDTO.builder()
                    .id(company.getId())
                    .name(company.getName())
                    .inn(company.getInn())
                    .ogrn(company.getOgrn())
                    .address(company.getAddress())
                    .phone(company.getPhone())
                    .email(company.getEmail())
                    .employees(employees)
                    .employeesCount(employees.size())
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional
    public CreateEmployeeResponse createEmployeeForCompany(CreateEmployeeRequest request)
            throws BadRequestException, AccountNotFoundException {
        validateRequest(request);
        Company company = repository.findByName(request.getCompanyName());
        if (company == null) {
            throw new BadRequestException("Company not found: " + request.getCompanyName());
        }

        RoleEnum role = request.getRoles().get(0);
        AccountRegisterRequestDTO accountDTO = new AccountRegisterRequestDTO();
        accountDTO.setEmail(request.getEmail());
        accountDTO.setPassword(request.getPassword());
        Account account = accountService.registration(accountDTO, role);
        account.setCompany(company);
        account.setName(request.getName());
        account.setSurname(request.getSurname());
        account.setPatronymic(request.getPatronymic());
        account = accountService.createAccount(account);

        if (role == RoleEnum.SPECIALIST) {
            Specialist specialist = new Specialist();
            specialist.setAccount(account);
            specialist.setCompany(company);
            specialist.setName(request.getName());
            specialist.setSurname(request.getSurname());
            specialist.setPatronymic(request.getPatronymic());
            specialistService.save(specialist);
        } else if (role == RoleEnum.APPLICANT) {
            applicantService.createForAccount(request, account, company);
        }

        return createEmployeeMapper.toDTO(accountService.getAccountById(account.getId()));
    }

    public Company createCompany(CompanyDTO companyDTO) {
        if (companyDTO.getName() == null || companyDTO.getName().isBlank()) {
            throw new IllegalArgumentException("Company name is required");
        }
        if (repository.findByName(companyDTO.getName()) != null) {
            throw new IllegalArgumentException("Company already exists: " + companyDTO.getName());
        }
        Company company = new Company();
        company.setName(companyDTO.getName());
        company.setInn(companyDTO.getInn());
        company.setOgrn(companyDTO.getOgrn());
        company.setEmail(companyDTO.getEmail());
        company.setAddress(companyDTO.getAddress());
        company.setPhone(companyDTO.getPhone());
        return repository.save(company);
    }

    private void validateRequest(CreateEmployeeRequest request) throws BadRequestException {
        if (request == null || request.getEmail() == null || request.getPassword() == null
                || request.getCompanyName() == null || request.getName() == null
                || request.getSurname() == null) {
            throw new BadRequestException("Required employee fields are missing");
        }
        if (request.getRoles() == null || request.getRoles().size() != 1) {
            throw new BadRequestException("Exactly one employee role is required");
        }
        if (!EMPLOYEE_ROLES.contains(request.getRoles().get(0))) {
            throw new BadRequestException("Unsupported employee role");
        }
    }

    private Employee toEmployee(Account account) {
        Employee.EmployeeBuilder builder = Employee.builder()
                .id(account.getId())
                .email(account.getEmail())
                .roles(account.getRoles().stream().map(Role::getName).toList());

        Specialist specialist = account.getSpecialist();
        if (specialist != null) {
            builder.fullName(specialist.getFullName())
                    .contactEmail(specialist.getContactEmail())
                    .contactPhone(specialist.getContactPhone())
                    .experience(specialist.getExperience())
                    .jobSatisfaction(specialist.getJobSatisfaction())
                    .profession(specialist.getProfession() == null ? null : specialist.getProfession().getName())
                    .gender(specialist.getGender() == null ? null : specialist.getGender().getName());
        }

        Applicant applicant = account.getApplicant();
        if (applicant != null) {
            builder.fullName(applicant.getFullName())
                    .gender(applicant.getGender() == null ? null : applicant.getGender().getName())
                    .birthday(applicant.getBirthday())
                    .targetProfessionId(applicant.getTargetProfession() == null
                            ? null : applicant.getTargetProfession().getId())
                    .targetProfession(applicant.getTargetProfession() == null
                            ? null : applicant.getTargetProfession().getName())
                    .active(applicant.isActive());
        }

        if (specialist == null && applicant == null) {
            builder.fullName(fullName(account));
        }
        return builder.build();
    }

    private String fullName(Account account) {
        return String.join(" ",
                account.getSurname() == null ? "" : account.getSurname(),
                account.getName() == null ? "" : account.getName(),
                account.getPatronymic() == null ? "" : account.getPatronymic()).trim();
    }
}
