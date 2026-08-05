package com.profession.suggest.database.services.applicant;

import com.profession.suggest.database.entities.auth.Account;
import com.profession.suggest.database.entities.auth.role.RoleEnum;
import com.profession.suggest.database.entities.users.applicant.Applicant;
import com.profession.suggest.database.entities.users.specialist.Company;
import com.profession.suggest.database.repositories.applicant.ApplicantRepository;
import com.profession.suggest.database.services.gender.GenderService;
import com.profession.suggest.database.services.profession.ProfessionService;
import com.profession.suggest.dto.applicant.ApplicantDTO;
import com.profession.suggest.dto.applicant.ApplicantMapper;
import com.profession.suggest.dto.applicant.ApplicantResponseDTO;
import com.profession.suggest.dto.company.CreateEmployeeRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApplicantService {
    private final ApplicantRepository repository;
    private final ApplicantMapper mapper;
    private final GenderService genderService;
    private final ProfessionService professionService;

    public Applicant getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Applicant not found: " + id));
    }

    public Applicant getByAccountId(Long accountId) {
        return repository.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Applicant profile not found"));
    }

    public ApplicantResponseDTO getResponseByAccountId(Long accountId) {
        return mapper.toResponseDTO(getByAccountId(accountId));
    }

    public Page<ApplicantResponseDTO> getAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponseDTO);
    }

    public Page<ApplicantResponseDTO> getByCompany(Long companyId, Pageable pageable) {
        return repository.findByCompanyId(companyId, pageable).map(mapper::toResponseDTO);
    }

    @Transactional
    public Applicant createForAccount(CreateEmployeeRequest request, Account account, Company company) {
        if (request.getBirthday() == null || request.getGender() == null || request.getTargetProfessionId() == null) {
            throw new IllegalArgumentException("Applicant birthday, gender and targetProfessionId are required");
        }
        Applicant applicant = new Applicant();
        applicant.setName(request.getName());
        applicant.setSurname(request.getSurname());
        applicant.setPatronymic(request.getPatronymic());
        applicant.setBirthday(request.getBirthday());
        applicant.setGender(genderService.findGenderByName(request.getGender()));
        applicant.setTargetProfession(professionService.getProfessionById(request.getTargetProfessionId()));
        applicant.setCompany(company);
        applicant.setAccount(account);
        applicant.setActive(request.getActive() == null || request.getActive());
        return repository.save(applicant);
    }

    @Transactional
    public ApplicantDTO update(Long id, ApplicantDTO dto, Account actor) {
        Applicant applicant = getById(id);
        assertCanManage(actor, applicant);
        mapper.updateFromDTO(applicant, dto);
        if (dto.getGender() != null) {
            applicant.setGender(genderService.findGenderByName(dto.getGender()));
        }
        if (dto.getTargetProfessionId() != null) {
            applicant.setTargetProfession(professionService.getProfessionById(dto.getTargetProfessionId()));
        }
        return mapper.toDTO(repository.save(applicant));
    }

    @Transactional
    public void deactivate(Long id, Account actor) {
        Applicant applicant = getById(id);
        assertCanManage(actor, applicant);
        applicant.setActive(false);
        repository.save(applicant);
    }

    private void assertCanManage(Account actor, Applicant applicant) {
        boolean isAdmin = actor.getRoles().stream().anyMatch(role -> role.getName() == RoleEnum.ADMIN);
        boolean isHr = actor.getRoles().stream().anyMatch(role -> role.getName() == RoleEnum.HR);
        boolean isOwner = applicant.getAccount() != null
                && applicant.getAccount().getId().equals(actor.getId());
        boolean sameCompany = actor.getCompany() != null && applicant.getCompany() != null
                && applicant.getCompany().getId().equals(actor.getCompany().getId());
        if (!isAdmin && !isOwner && !(isHr && sameCompany)) {
            throw new SecurityException("Applicant belongs to another company");
        }
    }
}
