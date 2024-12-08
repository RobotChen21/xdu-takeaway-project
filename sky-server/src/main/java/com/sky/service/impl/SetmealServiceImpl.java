package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealServicre;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealServicre {
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private DishMapper dishMapper;

    @Transactional
    @Override
    public void saveSetmeal(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.insert(setmeal);
        Long setmealId = setmeal.getId();
        List<SetmealDish> list = setmealDTO.getSetmealDishes();
        for (SetmealDish setmealDish : list) {
            setmealDish.setSetmealId(setmealId);
        }
        setmealDishMapper.insert(list);
    }

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        Page<SetmealVO> setmealPage = Page.of(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        String name = setmealPageQueryDTO.getName();
        Integer categoryId = setmealPageQueryDTO.getCategoryId();
        Integer status = setmealPageQueryDTO.getStatus();
        IPage<SetmealVO> resultPage = setmealMapper.pageQuery(name,categoryId,status,setmealPage);
        return new PageResult(resultPage.getTotal(),resultPage.getRecords());
    }

    @Transactional
    @Override
    public void delete(List<Long> ids) {
        List<Setmeal> setmeals = setmealMapper.selectBatchIds(ids);
        List<Setmeal> onSaleSetmeals = setmeals.stream()
                .filter(setmeal -> Objects.equals(setmeal.getStatus(), StatusConstant.ENABLE))
                .collect(Collectors.toList());
        if (!onSaleSetmeals.isEmpty()) {
            String names = onSaleSetmeals.stream()
                    .map(Setmeal::getName)
                    .collect(Collectors.joining(", "));
            throw new DeletionNotAllowedException(names + "为" + MessageConstant.DISH_ON_SALE);
        }
        LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SetmealDish::getSetmealId,ids);
        setmealDishMapper.delete(wrapper);
//        lambdaUpdate().in(Setmeal::getId,ids).remove();
        setmealMapper.deleteByIds(ids);
    }

    @Override
    public SetmealVO findById(Long id) {
        SetmealVO setmealVO = setmealMapper.findById(id);
        LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SetmealDish::getSetmealId,id);
        setmealVO.setSetmealDishes(setmealDishMapper.selectList(wrapper));
        return setmealVO;
    }

    @Transactional
    @Override
    public void updateSetmeal(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.updateById(setmeal);
        Long setmealId = setmealDTO.getId();
        LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SetmealDish::getSetmealId,setmealId);
        setmealDishMapper.delete(wrapper);
        List<SetmealDish> list = setmealDTO.getSetmealDishes();
        for (SetmealDish setmealDish : list) {
            setmealDish.setSetmealId(setmealId);
        }
        setmealDishMapper.insert(list);
    }
}
