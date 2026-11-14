package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    //动态条件查询
    List<ShoppingCart> list(ShoppingCart shoppingCart);
    @Update("update sky_take_out.shopping_cart set number=#{number} where id =#{id}")
    Void updateNumeberById(ShoppingCart shoppingCart);

    //插入信息到购物车当中
    @Insert("insert into sky_take_out.shopping_cart(name, image, user_id, dish_id, setmeal_id, dish_flavor, number, amount, create_time) " +
            "values (#{name}, #{image}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{number}, #{amount}, #{createTime})")
    void insert(ShoppingCart shoppingCart);
    //根据用户id清空购物车
    @Delete("delete from sky_take_out.shopping_cart where user_id=#{userId}")
    void deleteByuserId(Long userId);
    //根据动态条件删除一条购物车数据
    void sub(ShoppingCart shoppingCart);

    void updateNumeberByAll(ShoppingCart shoppingCart);
}
