package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private CategoryMapper categoryMapper;

    @Transactional
    @Override
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.insert(dish);
        Long dishId = dish.getId();
        List<DishFlavor> flavors = dishDTO.getFlavors();
        flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishId));
        dishFlavorMapper.insert(flavors);
    }

    @Override
    public PageResult pageQuery(DishPageQueryDTO queryDTO) {
        Page<DishVO> dishPage = Page.of(queryDTO.getPage(),queryDTO.getPageSize());
        IPage<DishVO> page = dishMapper.pageQuery(queryDTO.getName(),queryDTO.getCategoryId(),queryDTO.getStatus(),dishPage);
        return new PageResult(page.getTotal(),page.getRecords());
    }

    @Transactional
    @Override
    public void deleteBatch(List<Long> ids) {
        for (Long id : ids) {
            Dish dish = dishMapper.selectById(id);
            if(Objects.equals(dish.getStatus(), StatusConstant.ENABLE)){
                throw new DeletionNotAllowedException(dish.getName()+"为"+MessageConstant.DISH_ON_SALE);
            }
        }
        for (Long id : ids) {
            LambdaQueryWrapper<SetmealDish> setmealWrapper = new LambdaQueryWrapper<>();
            setmealWrapper.eq(SetmealDish::getDishId,id);
            List<SetmealDish> setmealDishList = setmealDishMapper.selectList(setmealWrapper);
            if(setmealDishList != null && !setmealDishList.isEmpty()){
                throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
            }
        }
        LambdaQueryWrapper<DishFlavor> dishFlavorWrapper = new LambdaQueryWrapper<>();
        dishFlavorWrapper.in(DishFlavor::getDishId,ids);
        dishFlavorMapper.delete(dishFlavorWrapper);
        dishMapper.deleteByIds(ids);
    }

    @Override
    public DishVO findById(Long id) {
        Dish dish = dishMapper.selectById(id);
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setCategoryName(categoryMapper.selectById(dish.getCategoryId()).getName());
        LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper();
        wrapper.eq(DishFlavor::getDishId,id);
        List<DishFlavor> list = dishFlavorMapper.selectList(wrapper);
        dishVO.setFlavors(list);
        return dishVO;
    }

    @Override
    public void updateDish(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.updateById(dish);
        Long dishId = dish.getId();
        //删除原先的
        LambdaQueryWrapper<DishFlavor> dishFlavorWrapper = new LambdaQueryWrapper<>();
        dishFlavorWrapper.eq(DishFlavor::getDishId,dishId);
        dishFlavorMapper.delete(dishFlavorWrapper);
        //再添加口味
        List<DishFlavor> flavors = dishDTO.getFlavors();
        flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishId));
        dishFlavorMapper.insert(flavors);
    }

    @Override
    public List<Dish> listByCategoryId(Long categoryId) {
        return  lambdaQuery().eq(Dish::getCategoryId,categoryId)
                .eq(Dish::getStatus, StatusConstant.ENABLE)
                .orderByDesc(Dish::getCreateTime).list();
    }

    @Override
    public List<DishVO> listWithFlavor(Long categoryId) {
        List<Dish> dishList = lambdaQuery()
                .eq(Dish::getStatus, StatusConstant.ENABLE)
                .eq(Dish::getCategoryId, categoryId)
                .list();
        log.info("标记！！！！！！！"+dishList.toString());
        List<DishVO> dishVOList = new ArrayList<>();
        for (Dish dish : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(dish,dishVO);
            LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DishFlavor::getDishId,dish.getId());
            dishVO.setFlavors(dishFlavorMapper.selectList(wrapper));
            dishVOList.add(dishVO);
        }
        log.info("标记！！！！！！！"+dishVOList);
        return dishVOList;
    }

    @Override
    public void updateStatus(Integer status, Long id) {
        Dish dish = new Dish();
        dish.setStatus(status);
        lambdaUpdate().eq(Dish::getId,id).update(dish);
    }

}
