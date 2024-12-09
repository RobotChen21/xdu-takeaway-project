package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;


@Slf4j
@Api(tags = "店铺状态接口")
@RequestMapping("/user/shop")
@RestController("userShopController")
public class ShopController {
    @Autowired
    private RedisTemplate redisTemplate;

    private static final String KEY = "SHOP_STATUS";//常量为全大写

    @ApiOperation("查询店铺状态")
    @GetMapping("/status")
    public Result<Integer> queryStatus(){
        Integer status = (Integer) redisTemplate.opsForValue().get(KEY);
        return Result.success(status);
    }
    
}
