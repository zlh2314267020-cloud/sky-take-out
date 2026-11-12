package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SeatmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceimpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO) {
        //插入基础数据
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.insert(setmeal);
        //插入添加菜品板块
        List<SetmealDish> setmealDishes=setmealDTO.getSetmealDishes();
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            setmealDishes.forEach(setmealDish -> {
               setmealDish.setSetmealId(setmeal.getId());
            });
            setmealMapper.insertbath(setmealDishes);
        }
    }

    //根据种类id查询菜品信息
    public List<Dish> getBycategoryIdWithDish(Long categoryId) {
        return setmealMapper.selectBycategoryId(categoryId);
    }

    //分页查询
    public PageResult SetmetalPageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page=setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }
    //批量删除套餐
    public void deleteSetmeal(List<Long> ids) {
        //判断当前套餐是否能删除，如果是起售就不能删除
        for (Long id : ids) {
            SetmealDTO setmeal=setmealMapper.getALLsetmeal(id);
            if (setmeal.getStatus()== StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //删除semeal——dish表格
        setmealMapper.deleteSetmeal_dish(ids);
        //删除setmeal表格
        setmealMapper.deleteSetmeal(ids);

    }
    //启用停用

    @Override
    public void StartorStop(Integer status, Long setmealId) {
        Setmeal setmeal = new Setmeal();
        setmeal.setStatus(status);
        setmeal.setId(setmealId);
        setmealMapper.update(setmeal);
    }

    @Override//查询修改界面的套餐数据
    public SetmealVO selectSetmealWithCategoryName(Long id) {
        SetmealDTO setmealDTO = new SetmealDTO();
        setmealDTO=setmealMapper.getALLsetmeal(id);
        String categoryname= categoryMapper.getCategoryName(setmealDTO.getCategoryId());
        List<SetmealDish> setmealDish=setmealMapper.getSetmealDishAll(id);
        setmealDTO.setSetmealDishes(setmealDish);
        SetmealVO setmealVO=new SetmealVO();
        BeanUtils.copyProperties(setmealDTO,setmealVO);
        setmealVO.setCategoryName(categoryname);
        return setmealVO;
    }
    //修改套餐信息
    @Override
    public void updateSetmealWithSetmealDish(SetmealDTO setmealDTO) {
        Setmeal setmeal=new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.update(setmeal);
        List<SetmealDish> setmealDishes=setmealDTO.getSetmealDishes();
//           //删除部分菜品信息
        setmealMapper.deleteSetmeal_dish1(setmealDTO.getId());
        //插入菜品信息
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmeal.getId());
            });
            setmealMapper.insertbath(setmealDishes);
        }
    }
    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }

}

