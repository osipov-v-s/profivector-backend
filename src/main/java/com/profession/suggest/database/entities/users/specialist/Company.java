package com.profession.suggest.database.entities.users.specialist;

import com.profession.suggest.database.entities.auth.Account;
import com.profession.suggest.database.entities.users.applicant.Applicant;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "company")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", unique = true)
    private String name;
    @Column(name = "inn", unique = true)
    private String inn;
    @Column(name = "ogrn", unique = true)
    private String ogrn;
    @Column(name = "address")
    private String address;
    @Column(name = "phone")
    private String phone;
    @Column(name = "email")
    private String email;
    @OneToMany(mappedBy = "company", orphanRemoval = false)
    private List<Specialist> specialists;
    @OneToMany(mappedBy = "company", orphanRemoval = false)
    private List<Applicant> applicants;
    @OneToMany(mappedBy = "company", orphanRemoval = false)
    private List<Account> accounts;
}
