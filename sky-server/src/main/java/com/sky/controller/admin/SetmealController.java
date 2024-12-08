package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealServicre;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "套餐相关接口")
@Slf4j
@RequestMapping("/admin/setmeal")
@RestController
public class SetmealController {
    @Autowired
    private SetmealServicre setmealServicre;
    @PostMapping
    @ApiOperation("新增套餐")
    public Result save(@RequestBody SetmealDTO setmealDTO){
        setmealServicre.saveSetmeal(setmealDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("分页查询套餐")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO){
        return Result.success(setmealServicre.pageQuery(setmealPageQueryDTO));
    }

    @DeleteMapping
    @ApiOperation("删除套餐")
    public Result delete(@RequestParam List<Long> ids){
        setmealServicre.delete(ids);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询套餐")
    public Result findById(@PathVariable Long id){
        return Result.success(setmealServicre.findById(id));
    }

    @PutMapping
    @ApiOperation("修改套餐")
    public Result update(@RequestBody SetmealDTO setmealDTO){
        setmealServicre.updateSetmeal(setmealDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("修改套餐状态")
    public Result updateStatus(@PathVariable Integer status,Long id){
        setmealServicre.updateStatus(status,id);
        return Result.success();
    }
}
