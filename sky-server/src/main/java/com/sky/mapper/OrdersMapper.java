package com.sky.mapper;

import com.sky.entity.Orders;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * <p>
 * 订单表 Mapper 接口
 * </p>
 *
 * @author ChenSir
 * @since 2024-12-10
 */
@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {
    Integer countByMap(Map map);

    Double sumByMap(Map map);
}
