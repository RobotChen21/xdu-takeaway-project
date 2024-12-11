package com.sky.controller.admin;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrdersService;
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
}
