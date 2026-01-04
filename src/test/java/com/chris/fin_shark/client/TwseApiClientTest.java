package com.chris.fin_shark.client;

import com.chris.fin_shark.m06.dto.external.TwseStockPriceData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TwseApiClientTest {

    @Autowired
    private com.chris.fin_shark.client.TwseApiClient twseApiClient;

    @Test
    void testGetStockMonthlyPrices() {
        LocalDate date = LocalDate.of(2025, 1, 2);
        List<TwseStockPriceData> prices = twseApiClient.getStockMonthlyPrices("2330", date);

        assertNotNull(prices);
        assertFalse(prices.isEmpty());

        prices.forEach(price -> {
            System.out.println(price.getTradeDate() + ": " + price.getClosePrice());
        });
    }
}