package com.sky.service;



import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import org.springframework.stereotype.Service;

@Service
public interface ShoppingCartService extends IService<ShoppingCart> {
    void addItem(ShoppingCartDTO shoppingCartDTO);

    void cleanAll();

    void subItem(ShoppingCartDTO shoppingCartDTO);
}
