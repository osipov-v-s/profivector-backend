package com.profession.suggest.controllers.dataanalys.prediction;

import com.profession.suggest.configuration.security.annotation.HasRole;
import com.profession.suggest.database.entities.auth.role.RoleEnum;
import com.profession.suggest.database.entities.dataanalys.prediction.Prediction;
import com.profession.suggest.database.services.dataanalys.prediction.PredictionService;
import com.profession.suggest.dto.dataanalys.prediction.PredictionDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/predictions")
public class PredictionController {
    private final PredictionService predictionService;

    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }
    @HasRole(RoleEnum.ADMIN)
    @PostMapping("/create")
    public ResponseEntity<?> createPrediction(@RequestPart("prediction") PredictionDTO predictionDTO,
                                                          @RequestPart("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(predictionService.createPrediction(predictionDTO, file));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Please check all required parameters, cant save prediction");
        }
    }
    @HasRole(RoleEnum.ADMIN)
    @GetMapping("/applicant/{applicantId}")
    public ResponseEntity<List<PredictionDTO>> getPredictionsByApplicantId(
            @PathVariable("applicantId") Long applicantId
    ) {
        return ResponseEntity.ok(predictionService.getPredictionsByApplicantId(applicantId));
    }
}
