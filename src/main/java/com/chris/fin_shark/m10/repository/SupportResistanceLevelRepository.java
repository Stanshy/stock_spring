package com.chris.fin_shark.m10.repository;

import com.chris.fin_shark.m10.domain.SupportResistanceLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 支撐壓力位 Repository
 *
 * @author chris
 * @since 1.0.0
 */
@Repository
public interface SupportResistanceLevelRepository extends JpaRepository<SupportResistanceLevel, Long> {

    /**
     * 根據股票代碼和分析日期查詢
     */
    List<SupportResistanceLevel> findByStockIdAndAnalysisDate(String stockId, LocalDate analysisDate);

    /**
     * 根據股票代碼查詢有效的價位
     */
    List<SupportResistanceLevel> findByStockIdAndIsActiveTrueOrderByPriceLevelDesc(String stockId);

    /**
     * 根據股票代碼和類型查詢
     */
    List<SupportResistanceLevel> findByStockIdAndLevelTypeAndIsActiveTrueOrderByStrengthDesc(
            String stockId, String levelType);

    /**
     * 查詢指定日期的支撐位
     */
    @Query("SELECT s FROM SupportResistanceLevel s WHERE s.stockId = :stockId AND s.analysisDate = :date AND s.levelType = 'SUPPORT' AND s.isActive = true ORDER BY s.priceLevel DESC")
    List<SupportResistanceLevel> findSupportLevels(@Param("stockId") String stockId, @Param("date") LocalDate date);

    /**
     * 查詢指定日期的壓力位
     */
    @Query("SELECT s FROM SupportResistanceLevel s WHERE s.stockId = :stockId AND s.analysisDate = :date AND s.levelType = 'RESISTANCE' AND s.isActive = true ORDER BY s.priceLevel ASC")
    List<SupportResistanceLevel> findResistanceLevels(@Param("stockId") String stockId, @Param("date") LocalDate date);

    /**
     * 刪除指定股票和日期的記錄
     */
    @Modifying
    @Query("DELETE FROM SupportResistanceLevel s WHERE s.stockId = :stockId AND s.analysisDate = :analysisDate")
    void deleteByStockIdAndAnalysisDate(@Param("stockId") String stockId, @Param("analysisDate") LocalDate analysisDate);

    /**
     * 使價位失效
     */
    @Modifying
    @Query("UPDATE SupportResistanceLevel s SET s.isActive = false, s.invalidatedAt = CURRENT_TIMESTAMP, s.invalidationReason = :reason WHERE s.levelId = :levelId")
    void invalidateLevel(@Param("levelId") Long levelId, @Param("reason") String reason);
}
