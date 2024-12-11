package com.sky.controller.user;


import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrdersService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 订单表 前端控制器
 * </p>
 *
 * @author ChenSir
 * @since 2024-12-10
 */
@Slf4j
@RestController("userOrdersController")
@RequestMapping("/user/order")
@Api(tags = "用户订单相关接口")
public class OrdersController {
    @Autowired
    private OrdersService ordersService;

    @ApiOperation("提交订单")
    @PostMapping("/submit")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO){
        return Result.success(ordersService.submit(ordersSubmitDTO));
    }
    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = ordersService.payment(ordersPaymentDTO);
        return Result.success(orderPaymentVO);
    }

    @GetMapping("/historyOrders")
    @ApiOperation("查看历史订单")
    public Result<PageResult> page(int page, int pageSize, Integer status){
        return Result.success(ordersService.pageQuery(page,pageSize,status));
    }

    @GetMapping("/orderDetail/{id}")
    @ApiOperation("查看订单详情页面")
    public Result<OrderVO> showDetails(@PathVariable Long id){
        return Result.success(ordersService.showDetails(id));
    }

    @PutMapping("/cancel/{id}")
    @ApiOperation("用户取消订单")
    public Result cancelOrder(@PathVariable Long id){
        ordersService.cancelOrder(id);
        return Result.success();
    }

    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result reOrder(@PathVariable Long id){
        ordersService.reOrder(id);
        return Result.success();
    }
}
