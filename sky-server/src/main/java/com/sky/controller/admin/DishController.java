package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.SetmealDish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.service.SetmealService;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/*
* 菜品管理
* */
@RestController
@RequestMapping("/admin/dish")
@Api(tags ="菜品相关接口")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private RedisTemplate redisTemplate;
    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品：{}",dishDTO);
        dishService.saveWithFlavor(dishDTO);
        //清理缓存数据
        String key="dish_"+dishDTO.getCategoryId();
        clearcache(key);

        return Result.success();
    }
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询：{}",dishPageQueryDTO);
        PageResult pageResult=dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);

    }
    /*菜品批量删除功能*/
    @DeleteMapping
    @ApiOperation("菜品批量删除")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("菜品批量删除：{}", ids);
        dishService.delete(ids);
        //将所有的菜品缓存数据都清除掉
        clearcache("dish_*");
        return Result.success();
    }
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("根据id查询菜品：{}",id);
       DishVO dishVO= dishService.getByIdWithFlavor(id);
       return Result.success(dishVO);
    }

    @PutMapping
    @ApiOperation("修改菜品信息")
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品信息:{}",dishDTO);
        dishService.updateWithFlavor(dishDTO);
        clearcache("dish_*");
        return  Result.success();
    }
    //根据种类id查询对应套餐内的菜品
    @GetMapping("/list")
    @ApiOperation("根据种类id查询菜品信息")
    public Result<List<Dish>> getByCategoryId(@RequestParam Long categoryId){
    log.info("根据种类id查询菜品信息：{}",categoryId);
    List<Dish> list= setmealService.getBycategoryIdWithDish(categoryId);
    return Result.success(list);
    }

    @PostMapping("/status/{status}")
    @ApiOperation("启用或禁用菜品")
    public Result startOrStop(
            @PathVariable Integer status,
            @RequestParam Long id) {
        log.info("启用或禁用菜品：status={}, id={}", status, id);
        dishService.startOrStop(status, id);
        clearcache("dish_*");
        return Result.success();
    }

    private void clearcache(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
