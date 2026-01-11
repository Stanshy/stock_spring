package com.chris.fin_shark.m06.controller;

import com.chris.fin_shark.common.dto.ApiResponse;
import com.chris.fin_shark.m06.dto.DataRepairResultDTO;
import com.chris.fin_shark.m06.dto.request.DataRepairRequest;
import com.chris.fin_shark.m06.service.DataRepairService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 資料補齊 Controller
 * <p>
 * 功能編號: F-M06-009
 * 功能名稱: 資料補齊機制
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/data-repair")
@Slf4j
@RequiredArgsConstructor
public class DataRepairController {

    private final DataRepairService dataRepairService;

    /**
     * 偵測缺漏資料
     *
     * @param request 補齊請求
     * @return 偵測結果
     */
    @PostMapping("/detect")
    public ApiResponse<DataRepairResultDTO> detectMissingData(
            @Valid @RequestBody DataRepairRequest request) {

        log.info("POST /api/data-repair/detect: {}", request);

        DataRepairResultDTO result = dataRepairService.detectMissingData(request);
        return ApiResponse.success(result);
    }

    /**
     * 執行資料補齊
     *
     * @param request 補齊請求
     * @return 補齊結果
     */
    @PostMapping("/execute")
    public ApiResponse<DataRepairResultDTO> executeRepair(
            @Valid @RequestBody DataRepairRequest request) {

        log.info("POST /api/data-repair/execute: {}", request);

        DataRepairResultDTO result = dataRepairService.executeRepair(request);
        return ApiResponse.success(result);
    }
}
