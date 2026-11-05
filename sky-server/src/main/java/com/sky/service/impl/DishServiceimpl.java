package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SeatmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceimpl implements DishService {
    @Autowired
    private DishMapper  dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SeatmealDishMapper seatmealDishMapper;
    //新增菜品以及对应口味
    @Transactional//原子性，要么全成功，要么全失败
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        //向菜品表插入一条数据，每次只能添加一个菜品
        dishMapper.insert(dish);
        //获取insert语句生成的主键值
        Long dishId = dish.getId();
        //口味表插入n条数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && !flavors.isEmpty()) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            dishFlavorMapper.insertBatch(flavors);
        }

    }

    //菜品查询
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page =dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }


    //批量删除
    @Transactional
    public void delete(List<Long> ids) {

        //判断当前菜品是否能够删除---如果存在起售不能删除
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus()== StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //当前菜品被套餐关联也不能删除
        List<Long> setmealIds = seatmealDishMapper.getSetmealIdsBydishIds(ids);
        if (setmealIds!=null && setmealIds.size()>0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //两者都不符合，可以删除菜品，删除菜品后需要删除口味
      /*  for (Long id : ids) {
            dishMapper.deleteById(id);
            //删除口味
            dishFlavorMapper.deleteByDishId(id);
        }
*/
    //根据菜品id批量删除菜品
    dishMapper.deleteByIds(ids);
    //根据菜品id批量删除菜品风味dishMapper.deleteByIds(ids);
    dishFlavorMapper.deleteByDishIds(ids);
    }

    //根据id查询菜品信息以及风味
    public DishVO getByIdWithFlavor(Long id) {
        //根据id查询菜品数据
        Dish dish = dishMapper.getById(id);
        //根据菜品id查询口味数据
        List<DishFlavor> dishFlavors=  dishFlavorMapper.getByDishId(id);
        //将查询到的数据封装到VO
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }

    //根据菜品id修改基本信息和对应的口味
    public void updateWithFlavor(DishDTO dishDTO) {
         //修改菜品表基本信息
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.update(dish);

        //删除原有口味
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        //插入新增口味
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }
}
