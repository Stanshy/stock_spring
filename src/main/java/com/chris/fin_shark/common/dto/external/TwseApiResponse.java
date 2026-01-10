package com.chris.fin_shark.m06.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 台灣證券交易所 API 回應資料傳輸物件
 * <p>
 * 用於接收 TWSE API 的 JSON 回應資料
 * API 端點：https://www.twse.com.tw/rwd/zh/afterTrading/STOCK_DAY
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
public class TwseApiResponse {

    /**
     * API 回應狀態
     * - "OK": 成功
     * - 其他: 失敗
     */
    @JsonProperty("stat")
    private String status;

    /**
     * 資料期間（例如：113年01月）
     */
    @JsonProperty("date")
    private String date;

    /**
     * 標題（例如：113年01月 2330 各日成交資訊）
     */
    @JsonProperty("title")
    private String title;

    /**
     * 欄位名稱
     * 順序：日期、成交股數、成交金額、開盤價、最高價、最低價、收盤價、漲跌價差、成交筆數
     */
    @JsonProperty("fields")
    private List<String> fields;

    /**
     * 股價資料（二維陣列）
     * 每一列對應一個交易日的資料
     * 欄位順序與 fields 對應
     */
    @JsonProperty("data")
    private List<List<String>> data;

    /**
     * 備註訊息（可選）
     */
    @JsonProperty("notes")
    private List<String> notes;
}
