package com.profession.suggest.dto.dataanalys.prediction;

import com.profession.suggest.database.entities.dataanalys.prediction.Prediction;
import com.profession.suggest.database.entities.dataanalys.prediction.PredictionType;
import org.springframework.stereotype.Component;

@Component
public class PredictionMapper {
    public PredictionDTO toDTO(Prediction prediction) {
        PredictionDTO dto = new PredictionDTO();

        dto.setId(prediction.getId());
        dto.setApplicantId(prediction.getApplicant().getId());
        dto.setFilePath(prediction.getFilePath());
        dto.setPredictionType(prediction.getPredictionType().getName());
        dto.setCreatedAt(prediction.getCreatedAt());

        return dto;
    }
    public Prediction fromDTO(PredictionDTO dto, PredictionType type) {
        Prediction prediction = new Prediction();
        prediction.setPredictionType(type);
        return prediction;
    }
}
