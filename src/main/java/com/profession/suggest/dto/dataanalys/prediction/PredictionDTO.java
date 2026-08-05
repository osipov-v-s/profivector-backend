package com.profession.suggest.dto.dataanalys.prediction;

import com.profession.suggest.database.entities.dataanalys.prediction.PredictionTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PredictionDTO {
    private Long id;
    private Long applicantId;
    private String filePath;
    private PredictionTypeEnum predictionType;
    private LocalDateTime createdAt;
}
