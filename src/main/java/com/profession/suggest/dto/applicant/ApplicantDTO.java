package com.profession.suggest.dto.applicant;

import com.profession.suggest.database.entities.gender.GenderEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicantDTO {
    private Long id;
    @NotBlank
    @Size(min = 2, max = 50)
    private String name;
    @NotBlank
    @Size(min = 2, max = 50)
    private String surname;
    @Size(max = 50)
    private String patronymic;
    @NotNull
    @Past
    private LocalDate birthday;
    @NotNull
    private GenderEnum gender;
    @NotNull
    private Long targetProfessionId;
    private String targetProfessionName;
    private Long companyId;
    private String companyName;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
