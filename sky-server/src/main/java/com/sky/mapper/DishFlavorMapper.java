package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {
    //批量插入口味
    void insertBatch(@Param("flavors") List<DishFlavor> flavors);

    //根据菜品id删除对应得口味数据
@Delete("delete from sky_take_out.dish_flavor where dish_id= #{dishId}")
    void deleteByDishId(Long dishId);

    void deleteByDishIds(List<Long> dishIds);

    //根据菜品id查询对应得口味数据
@Select("SELECT * from sky_take_out.dish_flavor where dish_id=#{dishId}")
    List<DishFlavor> getByDishId(Long dishId);
}
