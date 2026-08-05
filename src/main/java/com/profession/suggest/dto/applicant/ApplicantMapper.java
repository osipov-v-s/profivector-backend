package com.profession.suggest.dto.applicant;

import com.profession.suggest.database.entities.users.applicant.Applicant;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ApplicantMapper {
    public Applicant fromDTO(ApplicantDTO dto) {
        Applicant applicant = new Applicant();
        updateFromDTO(applicant, dto);
        return applicant;
    }

    public Applicant updateFromDTO(Applicant applicant, ApplicantDTO dto) {
        Optional.ofNullable(dto.getName()).ifPresent(applicant::setName);
        Optional.ofNullable(dto.getSurname()).ifPresent(applicant::setSurname);
        Optional.ofNullable(dto.getPatronymic()).ifPresent(applicant::setPatronymic);
        Optional.ofNullable(dto.getBirthday()).ifPresent(applicant::setBirthday);
        Optional.ofNullable(dto.getActive()).ifPresent(applicant::setActive);
        return applicant;
    }

    public ApplicantDTO toDTO(Applicant applicant) {
        ApplicantDTO dto = new ApplicantDTO();
        dto.setId(applicant.getId());
        dto.setName(applicant.getName());
        dto.setSurname(applicant.getSurname());
        dto.setPatronymic(applicant.getPatronymic());
        dto.setBirthday(applicant.getBirthday());
        dto.setGender(applicant.getGender() == null ? null : applicant.getGender().getName());
        if (applicant.getTargetProfession() != null) {
            dto.setTargetProfessionId(applicant.getTargetProfession().getId());
            dto.setTargetProfessionName(applicant.getTargetProfession().getName());
        }
        if (applicant.getCompany() != null) {
            dto.setCompanyId(applicant.getCompany().getId());
            dto.setCompanyName(applicant.getCompany().getName());
        }
        dto.setActive(applicant.isActive());
        dto.setCreatedAt(applicant.getCreatedAt());
        dto.setUpdatedAt(applicant.getUpdatedAt());
        return dto;
    }

    public ApplicantResponseDTO toResponseDTO(Applicant applicant) {
        return new ApplicantResponseDTO(
                applicant.getAccount() == null ? null : applicant.getAccount().getEmail(),
                toDTO(applicant));
    }
}
