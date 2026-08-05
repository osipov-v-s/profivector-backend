package com.profession.suggest.database.entities.dataanalys.psychtests;

import com.profession.suggest.database.entities.users.applicant.Applicant;
import com.profession.suggest.database.entities.users.specialist.Specialist;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "psych_test")
public class PsychTest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "completion_time_seconds")
    private Double completionTimeSeconds;
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    // Test completed by an applicant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id")
    private Applicant applicant;
    //Test completed by specialist
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialist_id")
    private Specialist specialist;
    //manyToOne PsychTestType +
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "psych_test_type_id")
    private PsychTestType psychTestType;
    //ManyToMany List PsychResultParam +
    @ManyToMany()
    @JoinTable(
            name = "psych_test_param",
            joinColumns = @JoinColumn(name = "psych_test_id"),
            inverseJoinColumns = @JoinColumn(name = "psych_param_id")
    )
    private Set<PsychParam> psychParams = new HashSet<>();
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}
