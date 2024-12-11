package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * <p>
 * 购物车 服务实现类
 * </p>
 *
 * @author ChenSir
 * @since 2024-12-10
 */
@Slf4j
@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public void addItem(ShoppingCartDTO shoppingCartDTO) {
        Long userId = BaseContext.getCurrentId();
        Long dishId = shoppingCartDTO.getDishId();
        String dishFlavor = shoppingCartDTO.getDishFlavor();
        Long setmealId = shoppingCartDTO.getSetmealId();
        boolean updated = lambdaUpdate()
                .eq(ShoppingCart::getUserId, userId)
                .eq(dishId != null, ShoppingCart::getDishId, dishId)
                .eq(dishId == null, ShoppingCart::getSetmealId, setmealId)
                .eq(dishFlavor != null,ShoppingCart::getDishFlavor,dishFlavor)
                .setSql("number = number + 1")
                .update();
        if (updated) {
            return;
        }
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        shoppingCart.setUserId(userId);
        BigDecimal amount;
        if(dishId != null){
            Dish dish = dishMapper.selectById(dishId);
            shoppingCart.setDishId(dishId);
            shoppingCart.setImage(dish.getImage());
            amount = dish.getPrice();
            shoppingCart.setName(dish.getName());
        }else{
            Setmeal setmeal = setmealMapper.selectById(setmealId);
            shoppingCart.setSetmealId(setmealId);
            shoppingCart.setImage(setmeal.getImage());
            amount = setmeal.getPrice();
            shoppingCart.setName(setmeal.getName());
        }
        shoppingCart.setAmount(amount);
        save(shoppingCart);
    }

    @Override
    public void cleanAll() {
        lambdaUpdate().eq(ShoppingCart::getUserId,BaseContext.getCurrentId())
                .remove();
    }

    @Override
    public void subItem(ShoppingCartDTO shoppingCartDTO) {
        Long userId = BaseContext.getCurrentId();
        Long dishId = shoppingCartDTO.getDishId();
        String dishFlavor = shoppingCartDTO.getDishFlavor();
        Long setmealId = shoppingCartDTO.getSetmealId();
        ShoppingCart shoppingCart = lambdaQuery()
                .eq(ShoppingCart::getUserId, userId)
                .eq(dishId != null, ShoppingCart::getDishId, dishId)
                .eq(dishId == null, ShoppingCart::getSetmealId, setmealId)
                .eq(dishFlavor != null, ShoppingCart::getDishFlavor, dishFlavor)
                .one();
        if (shoppingCart == null) {
            // 如果记录不存在，直接返回或抛出异常
            throw new IllegalArgumentException("购物车中未找到对应的条目");
        }
        if (shoppingCart.getNumber() == 1){
            //删除刚才查询到的哪个条目
            lambdaUpdate()
                    .eq(ShoppingCart::getId, shoppingCart.getId())
                    .remove();
        }else{
            lambdaUpdate()
                    .set(ShoppingCart::getNumber, shoppingCart.getNumber() - 1)
                    .eq(ShoppingCart::getId, shoppingCart.getId())
                    .update();
        }
    }
}
