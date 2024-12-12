package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrdersService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/order")
@Api(tags = "用户订单相关接口")
public class OrdersController {
    @Autowired
    private OrdersService ordersService;

    @GetMapping("/conditionSearch")
    @ApiOperation("订单搜索功能")
    public Result<PageResult> searchOrder(OrdersPageQueryDTO ordersPageQueryDTO){
        return Result.success(ordersService.searchOrder(ordersPageQueryDTO));
    }

    @GetMapping("/details/{id}")
    @ApiOperation("查看订单详情")
    public Result<OrderVO> showDetails(@PathVariable Long id){
        return Result.success(ordersService.showDetails(id));
    }

    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result cancelOrder(@RequestBody OrdersCancelDTO ordersCancelDTO){
        ordersService.cancelOrder(ordersCancelDTO.getCancelReason(), ordersCancelDTO.getId());
        return Result.success();
    }

    @PutMapping("/rejection")
    @ApiOperation("拒单")
    public Result rejectOrder(@RequestBody OrdersRejectionDTO ordersRejectionDTO){
        ordersService.rejectOrder(ordersRejectionDTO);
        return Result.success();
    }

    @PutMapping("/confirm")
    @ApiOperation("接单")
    public Result receiveOrder(@RequestBody OrdersConfirmDTO ordersConfirmDTO){
        ordersService.receiveOrder(ordersConfirmDTO.getId());
        return Result.success();
    }

    @PutMapping("/delivery/{id}")
    @ApiOperation("派送订单")
    public Result deliverOrder(@PathVariable Long id){
        ordersService.deliverOrder(id);
        return Result.success();
    }

    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result completeOrder(@PathVariable Long id){
        ordersService.completeOrder(id);
        return Result.success();
    }

    @GetMapping("/statistics")
    @ApiOperation("查看各订单数量")
    public Result<OrderStatisticsVO> showStatistics(){
        return Result.success(ordersService.showStatistics());
    }
}
