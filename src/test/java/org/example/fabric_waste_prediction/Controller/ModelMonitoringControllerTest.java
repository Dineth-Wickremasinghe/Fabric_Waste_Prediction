package org.example.fabric_waste_prediction.Controller;

import org.example.fabric_waste_prediction.Entity.Prediction;
import org.example.fabric_waste_prediction.Repository.PredictionRepository;
import org.example.fabric_waste_prediction.Service.MetricsService;
import org.example.fabric_waste_prediction.Service.PredictionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ModelMonitoringControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PredictionService predictionService;

    @MockitoBean
    private MetricsService metricsService;

    @MockitoBean
    private PredictionRepository predictionRepository;

    @Test
    void testShowDashboard() throws Exception {
        Prediction p = new Prediction();

        when(predictionService.getAllPredictions()).thenReturn(List.of(p));
        when(metricsService.computeR2()).thenReturn(0.85);
        when(metricsService.computeMAE()).thenReturn(0.12);

        mockMvc.perform(get("/monitoring/show"))
                .andExpect(status().isOk())
                .andExpect(view().name("model_feedback"))
                .andExpect(model().attributeExists("history"))
                .andExpect(model().attributeExists("maeScore"))
                .andExpect(model().attributeExists("r2Score"));
    }

    @Test
    void testUpdateActual() throws Exception {
        Prediction p = new Prediction();
        p.setId(1L);

        when(predictionRepository.findById(1L)).thenReturn(Optional.of(p));

        mockMvc.perform(post("/monitoring/1/actual")
                        .contentType("application/json")
                        .content("{\"actualResult\": 25.5}"))
                .andExpect(status().isOk());

        verify(predictionRepository).save(p);
    }

    @Test
    void testAddActual() throws Exception {
        Prediction p = new Prediction();
        p.setId(1L);

        when(predictionRepository.findById(1L)).thenReturn(Optional.of(p));
        when(metricsService.computeR2()).thenReturn(0.9);

        mockMvc.perform(put("/monitoring/predictions/1/actual")
                        .contentType("application/json")
                        .content("30.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Actual value added"))
                .andExpect(jsonPath("$.r2_score").value(0.9));

        verify(predictionRepository).save(p);
    }
}