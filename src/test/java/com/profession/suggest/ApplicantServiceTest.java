package com.profession.suggest;

import com.profession.suggest.database.entities.auth.Account;
import com.profession.suggest.database.entities.auth.role.Role;
import com.profession.suggest.database.entities.auth.role.RoleEnum;
import com.profession.suggest.database.entities.gender.Gender;
import com.profession.suggest.database.entities.gender.GenderEnum;
import com.profession.suggest.database.entities.professions.Profession;
import com.profession.suggest.database.entities.users.applicant.Applicant;
import com.profession.suggest.database.entities.users.specialist.Company;
import com.profession.suggest.database.repositories.applicant.ApplicantRepository;
import com.profession.suggest.database.services.applicant.ApplicantService;
import com.profession.suggest.database.services.gender.GenderService;
import com.profession.suggest.database.services.profession.ProfessionService;
import com.profession.suggest.dto.applicant.ApplicantDTO;
import com.profession.suggest.dto.applicant.ApplicantMapper;
import com.profession.suggest.dto.company.CreateEmployeeRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicantServiceTest {
    @Mock
    private ApplicantRepository repository;
    @Mock
    private GenderService genderService;
    @Mock
    private ProfessionService professionService;

    private ApplicantService service;

    @BeforeEach
    void setUp() {
        service = new ApplicantService(repository, new ApplicantMapper(), genderService, professionService);
    }

    @Test
    void createsApplicantWithExistingGenderProfessionCompanyAndAccount() {
        Gender gender = new Gender(1L, GenderEnum.FEMALE, List.of(), List.of());
        Profession profession = new Profession();
        profession.setId(10L);
        Company company = company(20L);
        Account account = account(30L, RoleEnum.APPLICANT, company);
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .email("applicant@example.com")
                .password("password")
                .roles(List.of(RoleEnum.APPLICANT))
                .name("Анна")
                .surname("Иванова")
                .birthday(LocalDate.of(1995, 5, 10))
                .gender(GenderEnum.FEMALE)
                .targetProfessionId(10L)
                .build();

        when(genderService.findGenderByName(GenderEnum.FEMALE)).thenReturn(gender);
        when(professionService.getProfessionById(10L)).thenReturn(profession);
        when(repository.save(any(Applicant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Applicant result = service.createForAccount(request, account, company);

        assertEquals("Анна", result.getName());
        assertSame(gender, result.getGender());
        assertSame(profession, result.getTargetProfession());
        assertSame(company, result.getCompany());
        assertSame(account, result.getAccount());
        assertTrue(result.isActive());
    }

    @Test
    void applicantCannotUpdateAnotherApplicantInSameCompany() {
        Company company = company(20L);
        Account owner = account(30L, RoleEnum.APPLICANT, company);
        Account actor = account(31L, RoleEnum.APPLICANT, company);
        Applicant target = new Applicant();
        target.setId(40L);
        target.setAccount(owner);
        target.setCompany(company);
        when(repository.findById(40L)).thenReturn(Optional.of(target));

        assertThrows(SecurityException.class, () -> service.update(40L, new ApplicantDTO(), actor));
        verify(repository, never()).save(any());
    }

    @Test
    void hrCanDeactivateApplicantOnlyInsideOwnCompany() {
        Company company = company(20L);
        Account hr = account(31L, RoleEnum.HR, company);
        Applicant target = new Applicant();
        target.setId(40L);
        target.setAccount(account(30L, RoleEnum.APPLICANT, company));
        target.setCompany(company);
        target.setActive(true);
        when(repository.findById(40L)).thenReturn(Optional.of(target));
        when(repository.save(target)).thenReturn(target);

        service.deactivate(40L, hr);

        assertFalse(target.isActive());
        verify(repository).save(target);
    }

    private static Company company(Long id) {
        Company company = new Company();
        company.setId(id);
        company.setName("Company " + id);
        return company;
    }

    private static Account account(Long id, RoleEnum roleName, Company company) {
        Account account = new Account();
        account.setId(id);
        account.setCompany(company);
        account.setRoles(new HashSet<>(List.of(new Role(1L, roleName, new HashSet<>()))));
        return account;
    }
}
