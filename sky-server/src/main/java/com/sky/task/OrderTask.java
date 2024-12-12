package com.sky.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sky.constant.MessageConstant;
import com.sky.entity.Orders;
import com.sky.mapper.OrdersMapper;
import com.sky.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrdersMapper ordersMapper;

    @Scheduled(cron = "0 0 1 * * ? ")
    public void processDeliveryOrder(){
        LambdaQueryWrapper<Orders> wrapper = Wrappers.lambdaQuery(Orders.class);
        wrapper.eq(Orders::getStatus,Orders.DELIVERY_IN_PROGRESS);
        List<Orders> ordersList = ordersMapper.selectList(wrapper);
        if(ordersList == null || ordersList.isEmpty()){
            return;
        }
        List<Orders> updateOrdersList = new ArrayList<>();
        for (Orders ordersDB : ordersList) {
            Orders orders = new Orders();
            orders.setId(ordersDB.getId());
            orders.setStatus(Orders.COMPLETED);
            orders.setDeliveryTime(LocalDateTime.now());
            updateOrdersList.add(orders);
        }
        ordersMapper.updateById(updateOrdersList);
    }

    @Scheduled(cron = "0 * * * * ? ")
    public void processTimeoutOrder(){
        LambdaQueryWrapper<Orders> wrapper = Wrappers.lambdaQuery(Orders.class);
        wrapper
                .le(Orders::getOrderTime,LocalDateTime.now().minusMinutes(15))
                .eq(Orders::getPayStatus,Orders.UN_PAID)
                .eq(Orders::getStatus,Orders.PENDING_PAYMENT);
        List<Orders> ordersList = ordersMapper.selectList(wrapper);
        if(ordersList == null || ordersList.isEmpty()){
            return;
        }
        List<Orders> updateOrdersList = new ArrayList<>();
        for (Orders ordersDB : ordersList) {
            Orders orders = new Orders();
            orders.setId(ordersDB.getId());
            orders.setStatus(Orders.CANCELLED);
            orders.setCancelReason("订单超时未支付，自动取消！");
            orders.setCancelTime(ordersDB  .getOrderTime().plusMinutes(15));
            updateOrdersList.add(orders);
        }
        ordersMapper.updateById(updateOrdersList);
    }
}
