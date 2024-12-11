package com.sky.service;

import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.Orders;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author ChenSir
 * @since 2024-12-10
 */
public interface OrdersService extends IService<Orders> {

    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    PageResult pageQuery(int page, int pageSize, Integer status);

    OrderVO showDetails(Long id);
}
