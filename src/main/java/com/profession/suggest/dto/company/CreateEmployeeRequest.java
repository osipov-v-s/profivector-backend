package com.profession.suggest.dto.company;

import com.profession.suggest.database.entities.auth.role.Role;
import com.profession.suggest.database.entities.auth.role.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.time.LocalDate;
import com.profession.suggest.database.entities.gender.GenderEnum;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEmployeeRequest {
    // Account fields
    private String email;
    private String password;
    private List<RoleEnum> roles;
    //HR fields (as Specialist)
    private String name;
    private String surname;
    private String patronymic;
    // Company fields
    private String companyName;
    private String companyInn;
    private String companyOgrn;
    // Applicant fields (required only for APPLICANT)
    private LocalDate birthday;
    private GenderEnum gender;
    private Long targetProfessionId;
    private Boolean active;
}
