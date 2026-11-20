package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

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
@Select("select * from sky_take_out.setmeal where id = #{id}")
SetmealDTO getALLsetmeal(Long id);

    void deleteSetmeal(List<Long> ids);

    void deleteSetmeal_dish(List<Long> ids);
@AutoFill(value = OperationType.INSERT)
    void update(Setmeal setmeal);
    @Select("select * from sky_take_out.setmeal_dish where setmeal_id=#{id}")
    List<SetmealDish> getSetmealDishAll(Long id);
   @Delete("delete  from sky_take_out.setmeal_dish where setmeal_id=#{id} ")
    void deleteSetmeal_dish1(Long id);


    /**
     * 动态条件查询套餐
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);
    /**
     * 根据套餐id查询菜品选项
     * @param setmealId
     * @return
     */
    @Select("select sd.name, sd.copies, d.image, d.description " +
            "from sky_take_out.setmeal_dish sd left join sky_take_out.dish d on sd.dish_id = d.id " +
            "where sd.setmeal_id = #{setmealId}")
    List<DishItemVO> getDishItemBySetmealId(Long setmealId);

//插入套餐数据

    /**
     * 根据条件统计套餐数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);


}

