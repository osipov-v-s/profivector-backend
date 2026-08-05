package com.profession.suggest.database.entities.professions;

import com.profession.suggest.database.entities.users.applicant.Applicant;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "profession")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Profession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", unique = true)
    private String name;
    @OneToMany(mappedBy = "targetProfession")
    private List<Applicant> applicants;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profession_sphere_id")
    private ProfessionSphere professionSphere;
}
