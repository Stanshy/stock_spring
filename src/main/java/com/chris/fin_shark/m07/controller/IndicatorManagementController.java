package com.chris.fin_shark.m07.controller;

import com.chris.fin_shark.common.dto.ApiResponse;
import com.chris.fin_shark.m07.dto.IndicatorDefinitionDTO;
import com.chris.fin_shark.m07.service.IndicatorDefinitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 指標管理 Controller
 * <p>
 * 功能編號: F-M07-006
 * 功能名稱: 指標定義管理
 * 提供指標定義的查詢操作
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/indicators")
@Slf4j
@RequiredArgsConstructor
public class IndicatorManagementController {

    private final IndicatorDefinitionService definitionService;

    /**
     * 查詢指標定義清單
     *
     * @param category   指標類別（可選）
     * @param priority   優先級（可選，P0/P1/P2）
     * @param activeOnly 僅查詢啟用的指標（可選）
     * @return 指標定義列表
     */
    @GetMapping("/definitions")
    public ApiResponse<List<IndicatorDefinitionDTO>> getDefinitions(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false, defaultValue = "true") Boolean activeOnly) {

        log.info("GET /api/indicators/definitions?category={}&priority={}&activeOnly={}",
                category, priority, activeOnly);

        List<IndicatorDefinitionDTO> definitions;

        if (category != null) {
            definitions = definitionService.getDefinitionsByCategory(category);
        } else if (priority != null) {
            definitions = definitionService.getDefinitionsByPriority(priority);
        } else if (activeOnly) {
            definitions = definitionService.getActiveDefinitions();
        } else {
            definitions = definitionService.getAllDefinitions();
        }

        return ApiResponse.success(definitions);
    }

    /**
     * 查詢單一指標定義
     *
     * @param indicatorName 指標名稱
     * @return 指標定義
     */
    @GetMapping("/definitions/{indicatorName}")
    public ApiResponse<IndicatorDefinitionDTO> getDefinition(
            @PathVariable String indicatorName) {

        log.info("GET /api/indicators/definitions/{}", indicatorName);


        IndicatorDefinitionDTO definition = definitionService
                .getDefinitionByName(indicatorName);

        return ApiResponse.success(definition);
    }
}
