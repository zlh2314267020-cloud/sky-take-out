package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;

public interface DishService {
    //新增菜品以及对应的口味
    public void saveWithFlavor(DishDTO dishDTO);
}
