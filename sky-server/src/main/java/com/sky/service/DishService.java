package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;


public interface DishService extends IService<Dish> {
    void saveWithFlavor(DishDTO dishDTO);

    PageResult pageQuery(DishPageQueryDTO queryDTO);

    void deleteBatch(List<Long> ids);

    DishVO findById(Long id);

    void updateDish(DishDTO dishDTO);

    List<Dish> listByCategoryId(Long categoryId);

    List<DishVO> listWithFlavor(Long categoryId);

    void updateStatus(Integer status, Long id);
}
