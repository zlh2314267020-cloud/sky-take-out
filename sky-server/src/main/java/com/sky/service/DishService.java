package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    //新增菜品以及对应的口味
    public void saveWithFlavor(DishDTO dishDTO);

    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    void delete(List<Long> ids);
    //根据菜品id查询菜品信息以及口味

    DishVO getByIdWithFlavor(Long id);
    //根据菜品id修改基本信息和对应的口味
    void updateWithFlavor(DishDTO dishDTO);
}
