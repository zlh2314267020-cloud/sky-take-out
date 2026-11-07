package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from sky_take_out.setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);
   //将数据插入数据库
    @AutoFill(value = OperationType.INSERT)
    void insert(Setmeal setmeal);
    //根据启用状态和种类id查询菜品列
    @Select("select * from sky_take_out.dish where category_id=#{categoryId} and status=1")
     List<Dish> selectBycategoryId( @Param("categoryId")Long categoryId);
//根据种类id和菜品名字确定dishid
    void insertbath(@Param("setmealDishes") List<SetmealDish> setmealDishes);
@Select("select id from sky_take_out.dish where price=#{price} and name=#{name};")
    Long getDishId(SetmealDish setmealDish);

    Page<SetmealVO> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

//插入套餐数据
}
