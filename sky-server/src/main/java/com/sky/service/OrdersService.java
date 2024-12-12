package com.sky.service;

import com.sky.dto.*;
import com.sky.entity.Orders;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
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

    void cancelOrder(Long id);

    void reOrder(Long id);

    PageResult searchOrder(OrdersPageQueryDTO ordersPageQueryDTO);

    void cancelOrder(String cancelReason, Long id);

    void rejectOrder(OrdersRejectionDTO ordersRejectionDTO);

    void receiveOrder(Long id);

    void deliverOrder(Long id);

    void completeOrder(Long id);

    OrderStatisticsVO showStatistics();
}
