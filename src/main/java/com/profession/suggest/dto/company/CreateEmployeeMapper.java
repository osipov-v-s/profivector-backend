package com.profession.suggest.dto.company;

import com.profession.suggest.database.entities.auth.Account;
import com.profession.suggest.database.entities.auth.role.Role;
import com.profession.suggest.database.entities.auth.role.RoleEnum;
import com.profession.suggest.database.entities.users.specialist.Company;
import com.profession.suggest.database.entities.users.applicant.Applicant;
import com.profession.suggest.database.entities.users.specialist.Specialist;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class CreateEmployeeMapper {
    public CreateEmployeeResponse toDTO(Account account) {
        CreateEmployeeResponse response = new CreateEmployeeResponse();
        response.setEmail(account.getEmail());
        response.setAccountId(account.getId());
        response.setRoles(account.getRoles().stream()
                .map(Role::getName).collect(Collectors.toList()));

        Specialist specialist = account.getSpecialist();
        Applicant applicant = account.getApplicant();
        if (specialist != null) {
            response.setFullName(specialist.getFullName());
        } else if (applicant != null) {
            response.setFullName(applicant.getFullName());
        } else {
            response.setFullName(String.join(" ",
                    account.getSurname() == null ? "" : account.getSurname(),
                    account.getName() == null ? "" : account.getName(),
                    account.getPatronymic() == null ? "" : account.getPatronymic()).trim());
        }

        Company company = account.getCompany();
        if (company != null) {
            response.setCompanyId(company.getId());
            response.setCompanyName(company.getName());
        }

        return response;
    }
}
