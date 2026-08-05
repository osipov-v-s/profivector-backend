package com.profession.suggest.database.repositories.dataanalys.prediction;

import com.profession.suggest.database.entities.dataanalys.prediction.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PredictionRepository extends JpaRepository<Prediction, Long> {
    List<Prediction> findByApplicantId(Long applicantId);
}
