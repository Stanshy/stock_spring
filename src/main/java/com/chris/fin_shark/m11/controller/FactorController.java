package com.chris.fin_shark.m11.controller;

import com.chris.fin_shark.common.dto.ApiResponse;
import com.chris.fin_shark.m11.dto.FactorMetadataDTO;
import com.chris.fin_shark.m11.dto.response.FactorListResponse;
import com.chris.fin_shark.m11.service.FactorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 因子管理 Controller
 * <p>
 * 提供因子清單查詢與詳情 API
 * Base URL: /api/v1/strategy/factors
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/strategy/factors")
@Slf4j
@RequiredArgsConstructor
public class FactorController {

    private final FactorService factorService;

    /**
     * API-M11-011: 查詢因子清單
     *
     * @param category     因子類別（TECHNICAL, FUNDAMENTAL, CHIP, PRICE_VOLUME）
     * @param sourceModule 來源模組（M06, M07, M08, M09）
     * @param keyword      關鍵字搜尋
     * @return 因子清單
     */
    @GetMapping
    public ApiResponse<FactorListResponse> getFactors(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String sourceModule,
            @RequestParam(required = false) String keyword) {

        log.info("GET /api/v1/strategy/factors?category={}&sourceModule={}", category, sourceModule);

        FactorListResponse response = factorService.getFactors(category, sourceModule, keyword);
        return ApiResponse.success(response);
    }

    /**
     * API-M11-012: 查詢因子詳情
     *
     * @param factorId 因子 ID
     * @return 因子詳情
     */
    @GetMapping("/{factorId}")
    public ApiResponse<FactorMetadataDTO> getFactor(@PathVariable String factorId) {

        log.info("GET /api/v1/strategy/factors/{}", factorId);

        FactorMetadataDTO response = factorService.getFactor(factorId);
        return ApiResponse.success(response);
    }
}
