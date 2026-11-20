package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.SetmealDTO;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
    //添加购物车
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    public void addshopingcart(ShoppingCartDTO shoppingCartDTO) {
        //判断加入到购物车的商品是否已经存在
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        Long userID = BaseContext.getCurrentId();//通过拦截器拿到用户id，来自JwtTokenUserInterceptor
        shoppingCart.setUserId(userID);
        List<ShoppingCart> list=shoppingCartMapper.list(shoppingCart);
        //如果存在，只需要将数量number+1
        if(list!=null&&list.size()>0){
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber()+1);
            shoppingCartMapper.updateNumberById(cart);
        }else{
            //如果不存在，需要插入一条购物车数据


            //判断本次添加的是套餐还是菜品
            Long dishId = shoppingCartDTO.getDishId();
            if(dishId!=null&&dishId>0){
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
               /* shoppingCart.setNumber(1);
                shoppingCart.setCreateTime(LocalDateTime.now());*/

            }else {
                Long setmealId = shoppingCartDTO.getSetmealId();
                SetmealDTO setmealDTO= setmealMapper.getALLsetmeal(setmealId);
                Setmeal setmeal = new Setmeal();
                BeanUtils.copyProperties(setmealDTO,setmeal);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
               /* shoppingCart.setNumber(1);
                shoppingCart.setCreateTime(LocalDateTime.now());
*/
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }

    }
    //查看购物车

    public List<ShoppingCart> showShoppingCart() {
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .build();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        return  list;
    }
    //清空购物车
    public void cleanShopping() {
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteByuserId(userId);
    }
    //删除一条信息

    @Override
    @Transactional
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        // 根据菜品/套餐、口味和当前用户查询真实购物车记录
        List<ShoppingCart> cartList = shoppingCartMapper.list(shoppingCart);
        if (cartList == null || cartList.isEmpty()) {
            return;
        }

        ShoppingCart cart = cartList.get(0);
        int number = cart.getNumber() == null ? 0 : cart.getNumber();

        if (number > 1) {
            cart.setNumber(number - 1);
            shoppingCartMapper.updateNumberById(cart);
        } else {
            shoppingCartMapper.deleteById(cart);
        }
    }
}
