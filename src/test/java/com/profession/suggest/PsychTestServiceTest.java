package com.profession.suggest;

import com.profession.suggest.database.entities.auth.Account;
import com.profession.suggest.database.entities.auth.role.Role;
import com.profession.suggest.database.entities.auth.role.RoleEnum;
import com.profession.suggest.database.entities.dataanalys.psychtests.PsychParam;
import com.profession.suggest.database.entities.dataanalys.psychtests.PsychTest;
import com.profession.suggest.database.entities.dataanalys.psychtests.PsychTestType;
import com.profession.suggest.database.entities.users.applicant.Applicant;
import com.profession.suggest.database.repositories.dataanalys.psychtests.PsychTestRepository;
import com.profession.suggest.database.services.dataanalys.psychtests.PsychParamService;
import com.profession.suggest.database.services.dataanalys.psychtests.PsychTestService;
import com.profession.suggest.database.services.dataanalys.psychtests.PsychTestTypeService;
import com.profession.suggest.dto.dataanalys.psychtests.PsychParamDTO;
import com.profession.suggest.dto.dataanalys.psychtests.PsychTestDTO;
import com.profession.suggest.dto.dataanalys.psychtests.PsychTestMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PsychTestServiceTest {
    @Mock
    private PsychTestRepository repository;
    @Mock
    private PsychParamService psychParamService;
    @Mock
    private PsychTestTypeService psychTestTypeService;
    @Mock
    private PsychTestMapper mapper;

    @Test
    void savesPsychologicalResultForApplicantWithoutSpecialistRole() {
        PsychTestService service = new PsychTestService(
                repository, psychParamService, psychTestTypeService, mapper);
        Applicant applicant = new Applicant();
        applicant.setId(10L);
        Account account = new Account();
        account.setId(20L);
        account.setApplicant(applicant);
        account.setRoles(new HashSet<>(List.of(
                new Role(1L, RoleEnum.APPLICANT, new HashSet<>()))));
        applicant.setAccount(account);

        PsychTestDTO request = new PsychTestDTO();
        request.setTestTypeName("Temperament");
        request.setCompletionTimeSeconds(45.0);
        request.setPsychParams(List.of(new PsychParamDTO(7, "sincerity_score")));
        PsychParam param = new PsychParam();
        param.setParam(7);
        PsychTest mapped = new PsychTest();
        mapped.setPsychParams(Set.of(param));
        PsychTestType type = new PsychTestType(1L, "Temperament", List.of());

        when(psychTestTypeService.getPsychTestTypeByName("Temperament")).thenReturn(type);
        when(mapper.fromDTO(request)).thenReturn(mapped);
        when(psychParamService.create(param)).thenReturn(param);
        when(repository.save(any(PsychTest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toDTO(any(PsychTest.class))).thenReturn(request);

        PsychTestDTO result = service.createPsychTest(request, account);

        assertSame(request, result);
        ArgumentCaptor<PsychTest> captor = ArgumentCaptor.forClass(PsychTest.class);
        verify(repository).save(captor.capture());
        assertSame(applicant, captor.getValue().getApplicant());
        assertNull(captor.getValue().getSpecialist());
        assertSame(type, captor.getValue().getPsychTestType());
        verify(psychParamService).create(param);
    }
}
