package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {
    //回显套餐内的名字
     List<Dish> getBycategoryIdWithDish(Long categoryId);
    //新增套餐以及菜品信息

    void saveWithDish(SetmealDTO setmealDTO);
    //  //分页查询
    PageResult SetmetalPageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    void deleteSetmeal(List<Long> ids);
    //启用停用
    void StartorStop(Integer status, Long setmealId);
   //修改菜品
    SetmealVO selectSetmealWithCategoryName(Long id);

    void updateSetmealWithSetmealDish(SetmealDTO setmealDTO);
    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);
    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    List<DishItemVO> getDishItemById(Long id);



}
