package com.sky.mapper;

import com.sky.entity.OrderDetail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 订单明细表 Mapper 接口
 * </p>
 *
 * @author ChenSir
 * @since 2024-12-11
 */
@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {

    List<Map<String, Object>> getTop10(LocalDate begin, LocalDate end);
}
