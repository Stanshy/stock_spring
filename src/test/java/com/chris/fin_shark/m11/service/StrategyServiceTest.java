package com.chris.fin_shark.m11.service;

import com.chris.fin_shark.common.dto.PageResponse;
import com.chris.fin_shark.m11.converter.StrategyConverter;
import com.chris.fin_shark.m11.domain.Strategy;
import com.chris.fin_shark.m11.dto.StrategyDTO;
import com.chris.fin_shark.m11.dto.request.StrategyCreateRequest;
import com.chris.fin_shark.m11.dto.request.StrategyQueryRequest;
import com.chris.fin_shark.m11.dto.request.StrategyUpdateRequest;
import com.chris.fin_shark.m11.enums.StrategyStatus;
import com.chris.fin_shark.m11.enums.StrategyType;
import com.chris.fin_shark.m11.exception.InvalidStrategyDefinitionException;
import com.chris.fin_shark.m11.exception.StrategyNotFoundException;
import com.chris.fin_shark.m11.mapper.StrategyMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 策略服務單元測試
 *
 * @author chris
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("策略服務測試")
class StrategyServiceTest {

    @Mock
    private StrategyMapper strategyMapper;

    @Mock
    private StrategyConverter strategyConverter;

    @InjectMocks
    private StrategyService strategyService;

    private Strategy testStrategy;
    private StrategyDTO testStrategyDTO;

    @BeforeEach
    void setUp() {
        testStrategy = Strategy.builder()
                .strategyId("STG_TEST_001")
                .strategyName("測試策略")
                .strategyType(StrategyType.MOMENTUM)
                .description("用於測試的策略")
                .currentVersion(1)
                .status(StrategyStatus.DRAFT)
                .isPreset(false)
                .conditions(Map.of(
                        "logic", "AND",
                        "conditions", List.of(
                                Map.of("factor_id", "rsi_14", "operator", "LESS_THAN", "value", 30)
                        )
                ))
                .createdBy("system")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testStrategyDTO = StrategyDTO.builder()
                .strategyId("STG_TEST_001")
                .strategyName("測試策略")
                .strategyType(StrategyType.MOMENTUM)
                .description("用於測試的策略")
                .version(1)
                .status(StrategyStatus.DRAFT)
                .isPreset(false)
                .build();

        System.out.println("\n========================================");
        System.out.println("🧪 策略服務測試");
        System.out.println("========================================\n");
    }

    @Nested
    @DisplayName("查詢策略測試")
    class GetStrategyTests {

        @Test
        @DisplayName("測試: 查詢策略清單")
        void testGetStrategies() {
            System.out.println("📝 測試: 查詢策略清單");

            // Given
            StrategyQueryRequest request = StrategyQueryRequest.builder()
                    .page(0)
                    .size(20)
                    .build();

            when(strategyMapper.selectStrategies(request)).thenReturn(List.of(testStrategy));
            when(strategyMapper.countStrategies(request)).thenReturn(1);
            when(strategyConverter.toDTOList(anyList())).thenReturn(List.of(testStrategyDTO));
            when(strategyMapper.countTodaySignals(anyString(), any())).thenReturn(5);

            // When
            PageResponse<StrategyDTO> result = strategyService.getStrategies(request);

            // Then
            System.out.println("  結果: 查詢到 " + result.getItems().size() + " 個策略");
            assertThat(result.getItems()).hasSize(1);
            assertThat(result.getPagination().getTotalItems()).isEqualTo(1);

            verify(strategyMapper).selectStrategies(request);
            verify(strategyMapper).countStrategies(request);
        }

        @Test
        @DisplayName("測試: 查詢策略詳情")
        void testGetStrategy() {
            System.out.println("📝 測試: 查詢策略詳情");

            // Given
            String strategyId = "STG_TEST_001";
            when(strategyMapper.selectById(strategyId)).thenReturn(testStrategy);
            when(strategyConverter.toDetailDTO(testStrategy)).thenReturn(testStrategyDTO);

            // When
            StrategyDTO result = strategyService.getStrategy(strategyId);

            // Then
            System.out.println("  結果: 查詢到策略 " + result.getStrategyId());
            assertThat(result).isNotNull();
            assertThat(result.getStrategyId()).isEqualTo(strategyId);

            verify(strategyMapper).selectById(strategyId);
        }

        @Test
        @DisplayName("測試: 策略不存在時拋出異常")
        void testGetStrategyNotFound() {
            System.out.println("📝 測試: 策略不存在時拋出異常");

            // Given
            String strategyId = "NON_EXISTENT";
            when(strategyMapper.selectById(strategyId)).thenReturn(null);

            // When/Then
            assertThatThrownBy(() -> strategyService.getStrategy(strategyId))
                    .isInstanceOf(StrategyNotFoundException.class);

            System.out.println("  結果: ✅ 正確拋出 StrategyNotFoundException");
        }
    }

    @Nested
    @DisplayName("建立策略測試")
    class CreateStrategyTests {

        @Test
        @DisplayName("測試: 建立新策略")
        void testCreateStrategy() {
            System.out.println("📝 測試: 建立新策略");

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

            when(strategyConverter.toEntity(request)).thenReturn(testStrategy);
            when(strategyMapper.insert(any())).thenReturn(1);
            when(strategyMapper.insertVersion(any())).thenReturn(1);
            when(strategyConverter.toDetailDTO(any())).thenReturn(testStrategyDTO);

            // When
            StrategyDTO result = strategyService.createStrategy(request);

            // Then
            System.out.println("  結果: 建立策略成功");
            assertThat(result).isNotNull();

            verify(strategyMapper).insert(any());
            verify(strategyMapper).insertVersion(any());
        }

        @Test
        @DisplayName("測試: 條件定義缺失時拋出異常")
        void testCreateStrategyWithoutConditions() {
            System.out.println("📝 測試: 條件定義缺失時拋出異常");

            // Given
            StrategyCreateRequest request = StrategyCreateRequest.builder()
                    .strategyName("新策略")
                    .strategyType("MOMENTUM")
                    .conditions(null)
                    .build();

            // When/Then
            assertThatThrownBy(() -> strategyService.createStrategy(request))
                    .isInstanceOf(InvalidStrategyDefinitionException.class);

            System.out.println("  結果: ✅ 正確拋出 InvalidStrategyDefinitionException");
        }

        @Test
        @DisplayName("測試: 條件定義格式錯誤時拋出異常")
        void testCreateStrategyWithInvalidConditions() {
            System.out.println("📝 測試: 條件定義格式錯誤時拋出異常");

            // Given: 缺少 logic 和 factor_id
            StrategyCreateRequest request = StrategyCreateRequest.builder()
                    .strategyName("新策略")
                    .strategyType("MOMENTUM")
                    .conditions(Map.of("invalid_key", "value"))
                    .build();

            // When/Then
            assertThatThrownBy(() -> strategyService.createStrategy(request))
                    .isInstanceOf(InvalidStrategyDefinitionException.class);

            System.out.println("  結果: ✅ 正確拋出 InvalidStrategyDefinitionException");
        }
    }

    @Nested
    @DisplayName("更新策略測試")
    class UpdateStrategyTests {

        @Test
        @DisplayName("測試: 更新策略")
        void testUpdateStrategy() {
            System.out.println("📝 測試: 更新策略");

            // Given
            String strategyId = "STG_TEST_001";
            StrategyUpdateRequest request = StrategyUpdateRequest.builder()
                    .strategyName("更新後的策略名稱")
                    .description("更新後的描述")
                    .build();

            when(strategyMapper.selectById(strategyId)).thenReturn(testStrategy);
            when(strategyMapper.update(any())).thenReturn(1);
            when(strategyMapper.insertVersion(any())).thenReturn(1);
            when(strategyConverter.toDetailDTO(any())).thenReturn(testStrategyDTO);

            // When
            StrategyDTO result = strategyService.updateStrategy(strategyId, request);

            // Then
            System.out.println("  結果: 更新策略成功");
            assertThat(result).isNotNull();

            verify(strategyMapper).update(any());
            verify(strategyMapper).insertVersion(any());
        }

        @Test
        @DisplayName("測試: 更新不存在的策略拋出異常")
        void testUpdateNonExistentStrategy() {
            System.out.println("📝 測試: 更新不存在的策略拋出異常");

            // Given
            String strategyId = "NON_EXISTENT";
            StrategyUpdateRequest request = StrategyUpdateRequest.builder()
                    .strategyName("更新後的策略名稱")
                    .build();

            when(strategyMapper.selectById(strategyId)).thenReturn(null);

            // When/Then
            assertThatThrownBy(() -> strategyService.updateStrategy(strategyId, request))
                    .isInstanceOf(StrategyNotFoundException.class);

            System.out.println("  結果: ✅ 正確拋出 StrategyNotFoundException");
        }
    }

    @Nested
    @DisplayName("策略狀態管理測試")
    class StatusManagementTests {

        @Test
        @DisplayName("測試: 啟用策略 (DRAFT -> ACTIVE)")
        void testActivateStrategy() {
            System.out.println("📝 測試: 啟用策略 (DRAFT -> ACTIVE)");

            // Given
            String strategyId = "STG_TEST_001";
            testStrategy.setStatus(StrategyStatus.DRAFT);

            when(strategyMapper.selectById(strategyId)).thenReturn(testStrategy);
            when(strategyMapper.updateStatus(strategyId, "ACTIVE")).thenReturn(1);
            when(strategyConverter.toDetailDTO(any())).thenReturn(testStrategyDTO);

            // When
            StrategyDTO result = strategyService.updateStatus(strategyId, "ACTIVE");

            // Then
            System.out.println("  結果: 策略狀態更新成功");
            assertThat(result).isNotNull();

            verify(strategyMapper).updateStatus(strategyId, "ACTIVE");
        }

        @Test
        @DisplayName("測試: 無效的狀態轉換拋出異常")
        void testInvalidStatusTransition() {
            System.out.println("📝 測試: 無效的狀態轉換拋出異常");

            // Given: ARCHIVED 狀態無法轉換
            String strategyId = "STG_TEST_001";
            testStrategy.setStatus(StrategyStatus.ARCHIVED);

            when(strategyMapper.selectById(strategyId)).thenReturn(testStrategy);

            // When/Then
            assertThatThrownBy(() -> strategyService.updateStatus(strategyId, "ACTIVE"))
                    .isInstanceOf(InvalidStrategyDefinitionException.class);

            System.out.println("  結果: ✅ 正確拋出 InvalidStrategyDefinitionException");
        }

        @Test
        @DisplayName("測試: 封存策略")
        void testArchiveStrategy() {
            System.out.println("📝 測試: 封存策略");

            // Given
            String strategyId = "STG_TEST_001";
            testStrategy.setStatus(StrategyStatus.ACTIVE);

            when(strategyMapper.selectById(strategyId)).thenReturn(testStrategy);
            when(strategyMapper.updateStatus(eq(strategyId), eq("ARCHIVED"))).thenReturn(1);

            // When
            strategyService.archiveStrategy(strategyId);

            // Then
            System.out.println("  結果: 策略封存成功");
            verify(strategyMapper).updateStatus(strategyId, "ARCHIVED");
        }
    }

    @Nested
    @DisplayName("查詢特殊策略測試")
    class SpecialQueryTests {

        @Test
        @DisplayName("測試: 查詢啟用的策略")
        void testGetActiveStrategies() {
            System.out.println("📝 測試: 查詢啟用的策略");

            // Given
            testStrategy.setStatus(StrategyStatus.ACTIVE);
            when(strategyMapper.selectActiveStrategies()).thenReturn(List.of(testStrategy));

            // When
            List<Strategy> result = strategyService.getActiveStrategies();

            // Then
            System.out.println("  結果: 查詢到 " + result.size() + " 個啟用的策略");
            assertThat(result).hasSize(1);

            verify(strategyMapper).selectActiveStrategies();
        }

        @Test
        @DisplayName("測試: 查詢預設策略")
        void testGetPresetStrategies() {
            System.out.println("📝 測試: 查詢預設策略");

            // Given
            testStrategy.setIsPreset(true);
            when(strategyMapper.selectPresetStrategies()).thenReturn(List.of(testStrategy));

            // When
            List<Strategy> result = strategyService.getPresetStrategies();

            // Then
            System.out.println("  結果: 查詢到 " + result.size() + " 個預設策略");
            assertThat(result).hasSize(1);

            verify(strategyMapper).selectPresetStrategies();
        }
    }
}
