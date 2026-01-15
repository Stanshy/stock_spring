package com.chris.fin_shark.m09.engine.model;

import com.chris.fin_shark.m09.enums.ChipCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 籌碼指標元資料
 * <p>
 * 定義籌碼計算器的元資料，包含名稱、類別、最少資料點數等資訊。
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChipMetadata {

    /** 指標名稱（英文，唯一識別） */
    private String name;

    /** 指標名稱（中文） */
    private String nameZh;

    /** 指標類別 */
    private ChipCategory category;

    /** 描述 */
    private String description;

    /** 最少需要的資料天數 */
    private Integer minDataDays;

    /** 預設參數 */
    private Map<String, Object> defaultParams;

    /** 優先級（P0, P1, P2） */
    private String priority;

    /**
     * 取得類別代碼（字串格式，供 Registry 使用）
     */
    public String getCategoryCode() {
        return category != null ? category.getCode() : null;
    }
}
