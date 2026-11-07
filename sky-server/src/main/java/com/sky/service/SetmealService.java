package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.SetmealDish;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {
    //回显套餐内的名字
     List<Dish> getBycategoryIdWithDish(Long categoryId);
    //新增套餐以及菜品信息

    void saveWithDish(SetmealDTO setmealDTO);
    //  //分页查询
    PageResult SetmetalPageQuery(SetmealPageQueryDTO setmealPageQueryDTO);
}
