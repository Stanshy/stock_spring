package com.chris.fin_shark.m11.controller;

import com.chris.fin_shark.common.dto.PageResponse;
import com.chris.fin_shark.m11.dto.StrategyDTO;
import com.chris.fin_shark.m11.dto.StrategySignalDTO;
import com.chris.fin_shark.m11.dto.request.StrategyCreateRequest;
import com.chris.fin_shark.m11.dto.request.StrategyStatusRequest;
import com.chris.fin_shark.m11.dto.response.StrategyExecuteResponse;
import com.chris.fin_shark.m11.enums.StrategyStatus;
import com.chris.fin_shark.m11.enums.StrategyType;
import com.chris.fin_shark.m11.exception.StrategyNotFoundException;
import com.chris.fin_shark.m11.service.SignalService;
import com.chris.fin_shark.m11.service.StrategyExecutionService;
import com.chris.fin_shark.m11.service.StrategyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 策略 Controller 整合測試
 *
 * @author chris
 * @since 1.0.0
 */
@WebMvcTest(StrategyController.class)
@DisplayName("策略 Controller 測試")
class StrategyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StrategyService strategyService;

    @MockBean
    private StrategyExecutionService strategyExecutionService;

    @MockBean
    private SignalService signalService;

    private StrategyDTO testStrategyDTO;

    @BeforeEach
    void setUp() {
        testStrategyDTO = StrategyDTO.builder()
                .strategyId("STG_TEST_001")
                .strategyName("測試策略")
                .strategyType(StrategyType.MOMENTUM)
                .description("用於測試的策略")
                .version(1)
                .status(StrategyStatus.ACTIVE)
                .isPreset(false)
                .conditionCount(3)
                .build();
    }

    @Nested
    @DisplayName("GET /api/v1/strategy")
    class GetStrategiesTests {

        @Test
        @DisplayName("應該回傳策略清單")
        void shouldReturnStrategiesList() throws Exception {
            // Given
            PageResponse<StrategyDTO> pageResponse = PageResponse.of(
                    List.of(testStrategyDTO), 1, 20, 1L);

            when(strategyService.getStrategies(any())).thenReturn(pageResponse);

            // When/Then
            mockMvc.perform(get("/api/v1/strategy")
                            .param("page", "0")
                            .param("size", "20"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.items").isArray())
                    .andExpect(jsonPath("$.data.items[0].strategyId").value("STG_TEST_001"));
        }

        @Test
        @DisplayName("應該支援篩選參數")
        void shouldSupportFilterParams() throws Exception {
            // Given
            PageResponse<StrategyDTO> pageResponse = PageResponse.of(
                    List.of(testStrategyDTO), 1, 20, 1L);

            when(strategyService.getStrategies(any())).thenReturn(pageResponse);

            // When/Then
            mockMvc.perform(get("/api/v1/strategy")
                            .param("status", "ACTIVE")
                            .param("type", "MOMENTUM")
                            .param("keyword", "測試"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/strategy/{strategyId}")
    class GetStrategyTests {

        @Test
        @DisplayName("應該回傳策略詳情")
        void shouldReturnStrategyDetail() throws Exception {
            // Given
            when(strategyService.getStrategy("STG_TEST_001")).thenReturn(testStrategyDTO);

            // When/Then
            mockMvc.perform(get("/api/v1/strategy/STG_TEST_001"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.strategyId").value("STG_TEST_001"))
                    .andExpect(jsonPath("$.data.strategyName").value("測試策略"));
        }

        @Test
        @DisplayName("策略不存在時應回傳 404")
        void shouldReturn404WhenNotFound() throws Exception {
            // Given
            when(strategyService.getStrategy("NON_EXISTENT"))
                    .thenThrow(new StrategyNotFoundException("NON_EXISTENT"));

            // When/Then
            mockMvc.perform(get("/api/v1/strategy/NON_EXISTENT"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/strategy")
    class CreateStrategyTests {

        @Test
        @DisplayName("應該成功建立策略")
        void shouldCreateStrategy() throws Exception {
            // Given
            StrategyCreateRequest request = StrategyCreateRequest.builder()
                    .strategyName("新策略")
                    .strategyType("MOMENTUM")
                    .description("測試用策略")
                    .conditions(Map.of(
                            "logic", "AND",
                            "conditions", List.of(
                                    Map.of("factor_id", "rsi_14", "operator", "LESS_THAN", "value", 30)
                            )
                    ))
                    .build();

            when(strategyService.createStrategy(any())).thenReturn(testStrategyDTO);

            // When/Then
            mockMvc.perform(post("/api/v1/strategy")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.strategyId").exists());
        }

        @Test
        @DisplayName("缺少必要欄位時應回傳 400")
        void shouldReturn400WhenMissingRequiredFields() throws Exception {
            // Given: 缺少 strategyName
            String invalidRequest = "{}";

            // When/Then
            mockMvc.perform(post("/api/v1/strategy")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidRequest))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/strategy/{strategyId}/status")
    class UpdateStatusTests {

        @Test
        @DisplayName("應該成功更新策略狀態")
        void shouldUpdateStatus() throws Exception {
            // Given
            StrategyStatusRequest request = StrategyStatusRequest.builder()
                    .status("ACTIVE")
                    .build();

            when(strategyService.updateStatus("STG_TEST_001", "ACTIVE"))
                    .thenReturn(testStrategyDTO);

            // When/Then
            mockMvc.perform(patch("/api/v1/strategy/STG_TEST_001/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/strategy/{strategyId}/execute")
    class ExecuteStrategyTests {

        @Test
        @DisplayName("應該成功執行策略")
        void shouldExecuteStrategy() throws Exception {
            // Given
            StrategyExecuteResponse response = StrategyExecuteResponse.builder()
                    .executionId("EXEC_TEST_001")
                    .strategyId("STG_TEST_001")
                    .strategyName("測試策略")
                    .executionDate(LocalDate.now())
                    .executionSummary(StrategyExecuteResponse.ExecutionSummaryDTO.builder()
                            .stocksEvaluated(100)
                            .signalsGenerated(5)
                            .buySignals(5)
                            .sellSignals(0)
                            .avgConfidence(BigDecimal.valueOf(75))
                            .executionTimeMs(1000L)
                            .build())
                    .build();

            when(strategyExecutionService.executeStrategy(eq("STG_TEST_001"), any()))
                    .thenReturn(response);

            // When/Then
            mockMvc.perform(post("/api/v1/strategy/STG_TEST_001/execute")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"execution_date\": \"2024-12-24\"}"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.executionId").value("EXEC_TEST_001"))
                    .andExpect(jsonPath("$.data.executionSummary.signalsGenerated").value(5));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/strategy/{strategyId}/signals")
    class GetSignalsTests {

        @Test
        @DisplayName("應該回傳策略信號")
        void shouldReturnSignals() throws Exception {
            // Given
            StrategySignalDTO signalDTO = StrategySignalDTO.builder()
                    .signalId("SIG_TEST_001")
                    .stockId("2330")
                    .confidenceScore(BigDecimal.valueOf(75.5))
                    .build();

            PageResponse<StrategySignalDTO> pageResponse = PageResponse.of(
                    List.of(signalDTO), 1, 50, 1L);

            when(signalService.getSignals(eq("STG_TEST_001"), any()))
                    .thenReturn(pageResponse);

            // When/Then
            mockMvc.perform(get("/api/v1/strategy/STG_TEST_001/signals"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.items").isArray())
                    .andExpect(jsonPath("$.data.items[0].signalId").value("SIG_TEST_001"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/strategy/{strategyId}")
    class ArchiveStrategyTests {

        @Test
        @DisplayName("應該成功封存策略")
        void shouldArchiveStrategy() throws Exception {
            // When/Then
            mockMvc.perform(delete("/api/v1/strategy/STG_TEST_001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }
}
