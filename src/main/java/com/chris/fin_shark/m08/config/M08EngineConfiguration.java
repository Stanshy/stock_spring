package com.chris.fin_shark.m08.config;

import com.chris.fin_shark.m08.engine.FundamentalCalculator;
import com.chris.fin_shark.m08.engine.M08IndicatorRegistry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * M08 引擎配置
 * <p>
 * 自動載入所有計算器到註冊表
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class M08EngineConfiguration {

    /**
     * 配置指標註冊表
     * <p>
     * 自動掃描並註冊所有 FundamentalCalculator Bean
     * </p>
     */
    @Bean("m08IndicatorRegistry")
    public M08IndicatorRegistry indicatorRegistry(List<FundamentalCalculator> calculators) {
        M08IndicatorRegistry registry = new M08IndicatorRegistry();
        registry.registerAll(calculators);

        log.info("已註冊 {} 個財務指標計算器", calculators.size());

        return registry;
    }
}
