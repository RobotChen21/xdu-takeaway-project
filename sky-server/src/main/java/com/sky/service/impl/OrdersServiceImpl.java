package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrdersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.service.ShoppingCartService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author ChenSir
 * @since 2024-12-10
 */
@Slf4j
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {
    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Override
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        //处理业务异常
        if (addressBook == null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.selectById(userId);
        LambdaQueryWrapper<ShoppingCart> shoppingCartWrapper = new LambdaQueryWrapper<>();
        shoppingCartWrapper.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.selectList(shoppingCartWrapper);
        if(shoppingCarts == null || shoppingCarts.isEmpty()){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //向订单表插入一条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setPayStatus(Orders.UN_PAID);
        orders.setOrderTime(LocalDateTime.now());
        orders.setNumber(String.valueOf(Instant.now().getEpochSecond()));
        orders.setPhone(addressBook.getPhone());
        orders.setAddress(addressBook.getProvinceName()+addressBook.getCityName()+addressBook.getDistrictName()+addressBook.getDetail());
        orders.setUserId(userId);
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserName(user.getName());
        save(orders);
        Long orderId = orders.getId();
        //像订单明细表插入n条数据
        List<OrderDetail> orderDetails = new ArrayList<>();
        shoppingCarts.forEach(shoppingCart -> {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart,orderDetail);
            orderDetail.setId(null);
            orderDetail.setOrderId(orderId);
            orderDetails.add(orderDetail);
        });
        orderDetailMapper.insert(orderDetails);
        shoppingCartService.cleanAll();
        //封装VO数据给前端返回
        return OrderSubmitVO
                .builder()
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();
    }
    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */

    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", "ORDERPAID");
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        lambdaUpdate()
                .eq(Orders::getNumber, ordersPaymentDTO.getOrderNumber())
                .eq(Orders::getUserId, BaseContext.getCurrentId())
                .set(Orders::getStatus, Orders.TO_BE_CONFIRMED)
                .set(Orders::getPayStatus, Orders.PAID)
                .set(Orders::getCheckoutTime, LocalDateTime.now())
                .update();
        return vo;
    }

    @Override
    public PageResult pageQuery(int page, int pageSize, Integer status) {
        Page<Orders> ordersPage = Page.of(page,pageSize);
        LambdaQueryWrapper<Orders> ordersWrapper = new LambdaQueryWrapper<>();
        ordersWrapper
                .eq(status != null,Orders::getStatus,status)
                .orderBy(true,false,Orders::getOrderTime);
        IPage<Orders> iPage = ordersMapper.selectPage(ordersPage,ordersWrapper);
        List<Orders> ordersList = iPage.getRecords();
        List<OrderVO> orderVOList= new ArrayList<>();
        ordersList.forEach(orders -> {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(orders,orderVO);
            LambdaQueryWrapper<OrderDetail> orderDetailWrapper = new LambdaQueryWrapper<>();
            orderDetailWrapper.eq(OrderDetail::getOrderId,orders.getId());
            orderVO.setOrderDetailList(orderDetailMapper.selectList(orderDetailWrapper));
            orderVOList.add(orderVO);
        });
        return new PageResult(iPage.getTotal(),orderVOList);
    }

    @Override
    public OrderVO showDetails(Long id) {
        Orders orders = getById(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders,orderVO);
        LambdaQueryWrapper<OrderDetail> orderDetailWrapper = new LambdaQueryWrapper<>();
        orderDetailWrapper.eq(OrderDetail::getOrderId,orders.getId());
        orderVO.setOrderDetailList(orderDetailMapper.selectList(orderDetailWrapper));
        return orderVO;
    }

    @Override
    public void cancelOrder(Long id) {
        cancelMethod("用户取消",id);
    }

    @Override
    public void reOrder(Long id) {
        Orders ordersDB = getById(id);
        if(ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        LambdaQueryWrapper<OrderDetail> orderDetailWrapper = new LambdaQueryWrapper<>();
        orderDetailWrapper.eq(OrderDetail::getOrderId,id);
        List<OrderDetail> orderDetails = orderDetailMapper.selectList(orderDetailWrapper);
        List<ShoppingCart> shoppingCarts = orderDetails.stream().map(orderDetail->{
                ShoppingCart shoppingCart = new ShoppingCart();
                BeanUtils.copyProperties(orderDetail,shoppingCart,"id");
                shoppingCart.setUserId(BaseContext.getCurrentId());
                shoppingCart.setCreateTime(LocalDateTime.now());
                return shoppingCart;
            }
        ).collect(Collectors.toList());
        shoppingCartService.cleanAll();
        shoppingCartMapper.insert(shoppingCarts);
    }

    @Override
    public PageResult searchOrder(OrdersPageQueryDTO ordersPageQueryDTO) {
        Page<Orders> ordersPage = Page.of(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        String number = ordersPageQueryDTO.getNumber();
        String phone = ordersPageQueryDTO.getPhone();
        Integer status = ordersPageQueryDTO.getStatus();
        LocalDateTime beginTime = ordersPageQueryDTO.getBeginTime();
        LocalDateTime endTime = ordersPageQueryDTO.getEndTime();
        Long userId = ordersPageQueryDTO.getUserId();
        // 构造 LambdaQueryWrapper 条件查询
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(number != null && !number.isEmpty(), Orders::getNumber, number)        // 按订单号查询
                .eq(phone != null && !phone.isEmpty(), Orders::getPhone, phone)           // 按手机号查询
                .eq(status != null, Orders::getStatus, status)                            // 按状态查询
                .eq(userId != null, Orders::getUserId, userId)                            // 按用户ID查询
                .ge(beginTime != null, Orders::getOrderTime, beginTime)                   // 起始时间
                .le(endTime != null, Orders::getOrderTime, endTime)                       // 结束时间
                .orderByDesc(Orders::getOrderTime);
        Page<Orders> page = page(ordersPage, queryWrapper);
        List<Orders> ordersList = page.getRecords();
        List<OrderVO> orderVOList = new ArrayList<>();
        if(ordersList != null){
            orderVOList = ordersList.stream().map(orders->
            {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders,orderVO);
                Long orderId = orders.getId();
                LambdaQueryWrapper<OrderDetail> orderDetailWrapper = new LambdaQueryWrapper<>();
                orderDetailWrapper.eq(OrderDetail::getOrderId,orderId);
                String orderDishes = orderDetailMapper.selectList(orderDetailWrapper)
                    .stream()
                    .map(orderDetail -> orderDetail.getName() + "*" + orderDetail.getNumber())
                    .collect(Collectors.joining(";"));
                orderVO.setOrderDishes(orderDishes);
                return orderVO;
            }
            ).collect(Collectors.toList());
        }
        return new PageResult(page.getTotal(),orderVOList);
    }

    @Override
    public void cancelOrder(String cancelReason, Long id) {
        cancelMethod(cancelReason,id);
    }

    @Override
    public void rejectOrder(OrdersRejectionDTO ordersRejectionDTO) {
        Long orderId = ordersRejectionDTO.getId();
        Orders orderDB = getById(orderId);
        if(orderDB == null || !Objects.equals(orderDB.getStatus(), Orders.TO_BE_CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = new Orders();
        if(Objects.equals(orderDB.getPayStatus(), Orders.PAID)){
            orders.setPayStatus(Orders.REFUND);
        }
        orders.setId(orderId);
        orders.setRejectionReason(orders.getRejectionReason());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        updateById(orders);
    }

    @Override
    public void receiveOrder(Long id) {
        updateOrderStatus(id, Orders.TO_BE_CONFIRMED, Orders.CONFIRMED, null);
    }

    @Override
    public void deliverOrder(Long id) {
        updateOrderStatus(id, Orders.CONFIRMED, Orders.DELIVERY_IN_PROGRESS, null);
    }


    @Override
    public void completeOrder(Long id) {
        updateOrderStatus(id, Orders.DELIVERY_IN_PROGRESS, Orders.COMPLETED, LocalDateTime.now());
    }

    @Override
    public OrderStatisticsVO showStatistics() {
        // 获取所有的订单状态
        QueryWrapper<Orders> wrapper = Wrappers.query();
        wrapper.select("status", "COUNT(*) as count")
                .groupBy("status");
        List<Map<String, Object>> results = ordersMapper.selectMaps(wrapper);
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        for (Map<String, Object> map : results) {
            Integer status = (Integer) map.get("status");
            Long count = (Long) map.get("count");
            if (status.equals(Orders.TO_BE_CONFIRMED)) {
                orderStatisticsVO.setToBeConfirmed(count.intValue());
            } else if (status.equals(Orders.CONFIRMED)) {
                orderStatisticsVO.setConfirmed(count.intValue());
            } else if (status.equals(Orders.DELIVERY_IN_PROGRESS)) {
                orderStatisticsVO.setDeliveryInProgress(count.intValue());
            }
        }
        return orderStatisticsVO;
    }

    private void cancelMethod(String reason, Long id) {
        Orders ordersDB = getById(id);
        if(ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if(ordersDB.getStatus() > 2){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = new Orders();
        orders.setId(id);
        orders.setStatus(Orders.CANCELLED);
        if("用户取消".equals(reason)){
            orders.setPayStatus(Orders.REFUND);
        }
        orders.setCancelReason(reason);
        orders.setCancelTime(LocalDateTime.now());
        updateById(orders);
    }
    private void updateOrderStatus(Long id, Integer expectedStatus, Integer newStatus, LocalDateTime deliveryTime) {
        Orders ordersDB = getById(id);
        if (ordersDB == null || !Objects.equals(ordersDB.getStatus(), expectedStatus)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = new Orders();
        orders.setId(id);
        orders.setStatus(newStatus);
        if (deliveryTime != null) {
            orders.setDeliveryTime(deliveryTime);
        }
        updateById(orders);
    }
}
