package com.profession.suggest.database.entities.gender;

import com.profession.suggest.database.entities.users.applicant.Applicant;
import com.profession.suggest.database.entities.users.specialist.Specialist;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "gender")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Gender {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false)
    private GenderEnum name;
    @OneToMany(mappedBy = "gender")
    private List<Applicant> applicants;
    @OneToMany(mappedBy = "gender")
    private List<Specialist> specialists;
}
