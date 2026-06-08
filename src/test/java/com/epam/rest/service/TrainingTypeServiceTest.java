package com.epam.rest.service;

import com.epam.rest.dto.response.TrainingTypeResponse;
import com.epam.rest.entity.TrainingType;
import com.epam.rest.repository.TrainingTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Task 17: Training Type Service Unit Tests")
class TrainingTypeServiceTest {

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @InjectMocks
    private TrainingTypeService trainingTypeService;

    @Test
    @DisplayName("Task 17: Should return all training types")
    void getAll_ReturnsAllTypes() {
        List<TrainingType> types = List.of(
                TrainingType.builder().id(1L).trainingTypeName("Cardio").build(),
                TrainingType.builder().id(2L).trainingTypeName("Yoga").build(),
                TrainingType.builder().id(3L).trainingTypeName("Strength").build()
        );

        when(trainingTypeRepository.findAll()).thenReturn(types);

        List<TrainingTypeResponse> response = trainingTypeService.getAll();

        assertThat(response).hasSize(3);
        assertThat(response.get(0).trainingTypeName()).isEqualTo("Cardio");
        assertThat(response.get(1).trainingTypeName()).isEqualTo("Yoga");
        assertThat(response.get(2).trainingTypeName()).isEqualTo("Strength");

        verify(trainingTypeRepository).findAll();
    }

    @Test
    @DisplayName("Task 17: Should return empty list when no types exist")
    void getAll_EmptyList() {
        when(trainingTypeRepository.findAll()).thenReturn(List.of());

        List<TrainingTypeResponse> response = trainingTypeService.getAll();

        assertThat(response).isEmpty();
    }

    @Test
    @DisplayName("Task 14: Training types should be constant (read-only)")
    void trainingTypes_ReadOnly() {
        List<TrainingType> types = List.of(
                TrainingType.builder().id(1L).trainingTypeName("Cardio").build()
        );

        when(trainingTypeRepository.findAll()).thenReturn(types);

        trainingTypeService.getAll();

        verify(trainingTypeRepository, only()).findAll();
        verify(trainingTypeRepository, never()).save(any());
        verify(trainingTypeRepository, never()).delete(any());
    }
}