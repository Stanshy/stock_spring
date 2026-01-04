package com.chris.fin_shark.m06.converter;

import com.chris.fin_shark.m06.domain.Stock;
import com.chris.fin_shark.m06.dto.StockDTO;
import com.chris.fin_shark.m06.dto.request.StockCreateRequest;
import com.chris.fin_shark.m06.dto.request.StockUpdateRequest;
import org.mapstruct.*;

import java.util.List;

/**
 * 股票資料轉換 Mapper
 * <p>
 * 使用 MapStruct 進行 Entity 和 DTO 之間的轉換
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StockConverter {

    /**
     * Entity 轉 DTO
     *
     * @param stock Entity
     * @return DTO
     */
    StockDTO toDTO(Stock stock);

    /**
     * Entity 列表轉 DTO 列表
     *
     * @param stocks Entity 列表
     * @return DTO 列表
     */
    List<StockDTO> toDTOList(List<Stock> stocks);

    /**
     * CreateRequest 轉 Entity
     *
     * @param request 建立請求
     * @return Entity
     */
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Stock toEntity(StockCreateRequest request);

    /**
     * UpdateRequest 更新 Entity
     * <p>
     * 僅更新非 null 的欄位
     * </p>
     *
     * @param request 更新請求
     * @param stock   目標 Entity
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "stockId", ignore = true)
    @Mapping(target = "marketType", ignore = true)
    @Mapping(target = "listingDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(StockUpdateRequest request, @MappingTarget Stock stock);
}
