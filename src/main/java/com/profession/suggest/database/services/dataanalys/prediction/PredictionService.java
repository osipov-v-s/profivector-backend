package com.profession.suggest.database.services.dataanalys.prediction;

import com.profession.suggest.database.entities.dataanalys.prediction.Prediction;
import com.profession.suggest.database.entities.dataanalys.prediction.PredictionType;
import com.profession.suggest.database.entities.users.applicant.Applicant;
import com.profession.suggest.database.repositories.dataanalys.prediction.PredictionRepository;
import com.profession.suggest.database.repositories.dataanalys.prediction.PredictionTypeRepository;
import com.profession.suggest.database.services.applicant.ApplicantService;
import com.profession.suggest.dto.dataanalys.prediction.PredictionDTO;
import com.profession.suggest.dto.dataanalys.prediction.PredictionMapper;
import com.profession.suggest.services.files.FileStorageService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PredictionService {
    private final PredictionRepository repository;
    private final PredictionMapper mapper;
    private final PredictionTypeService predictionTypeService;
    private final ApplicantService applicantService;
    private final FileStorageService fileStorageService;

    public PredictionDTO createPrediction(PredictionDTO dto, MultipartFile file) throws Exception {
        if (dto == null)
            throw new IllegalArgumentException("Prediction DTO cannot be null");
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("File cannot be null or empty");
        if (dto.getApplicantId() == null || dto.getApplicantId() <= 0)
            throw new IllegalArgumentException("Valid applicant ID is required");
        if (dto.getPredictionType() == null)
            throw new IllegalArgumentException("Prediction type is required");
        if (file.getSize() > 10 * 1024 * 1024)
            throw new IllegalArgumentException("File size more then 10MB");

        PredictionType type = predictionTypeService.getByName(dto.getPredictionType());
        Prediction prediction = mapper.fromDTO(dto, type);
        Applicant applicant = applicantService.getById(dto.getApplicantId());

        if (applicant == null) throw new EntityNotFoundException(
                String.format("Applicant not found with id: %d", dto.getApplicantId()));
        if (type == null) throw new EntityNotFoundException(
                String.format("Prediction type not found: %s", dto.getPredictionType()));

        prediction.setFilePath(fileStorageService.saveFile(file, "predictions", true));
        prediction.setApplicant(applicant);
        return mapper.toDTO(repository.save(prediction));
    }
    public List<PredictionDTO> getPredictionsByApplicantId(Long applicantId) {
        List<Prediction> predictions = repository.findByApplicantId(applicantId);
        return predictions.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }
}
