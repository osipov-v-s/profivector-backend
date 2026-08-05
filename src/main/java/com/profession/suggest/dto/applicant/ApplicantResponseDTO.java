package com.profession.suggest.dto.applicant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicantResponseDTO {
    private String email;
    private ApplicantDTO applicantDTO;
}
