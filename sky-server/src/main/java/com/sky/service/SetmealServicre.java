package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.SetmealDTO;
import com.sky.entity.Setmeal;

public interface SetmealServicre extends IService<Setmeal> {
    void saveSetmeal(SetmealDTO setmealDTO);
}
