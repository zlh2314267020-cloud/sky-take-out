package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Api("套餐相关接口")
@Slf4j
public class SetmealController {
    //新增套餐实现接口
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealMapper setmealMapper;

    @PostMapping
    @ApiOperation("新增套餐")
    public Result save(@RequestBody SetmealDTO setmealDTO) {
        log.info("新增套餐：{}", setmealDTO);
        setmealService.saveWithDish(setmealDTO);
        return Result.success();
    }
    @GetMapping("/page")
    @ApiOperation("套餐分页查询接口")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO) {
        log.info("套餐分页查询：{}", setmealPageQueryDTO);
        PageResult pageResult=setmealService.SetmetalPageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }
    @DeleteMapping
    @ApiOperation("删除套餐接口")
    public Result deleteSetmeal(@RequestParam List<Long> ids) {
        log.info("删除套餐接口: {}",ids);
        setmealService.deleteSetmeal(ids);
        return Result.success();
    }
    //启用或者停用状态
    @PostMapping("/status/{status}")
    @ApiOperation("套餐停用以及启用")
    public Result StrartorStop(@PathVariable Integer status,@RequestParam Long id) {
        log.info("套餐停用以及启用：{}",id);
        setmealService.StartorStop(status,id);
        return Result.success();
    }
  //根据id查询菜品信息
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<SetmealVO> select(@PathVariable Long id){
        log.info("根据id查询：{}",id);
        SetmealVO setmealVO = setmealService.selectSetmealWithCategoryName(id);
        return Result.success(setmealVO);
    }
    //修改套餐信息
    @PutMapping
    @ApiOperation("修改菜品信息")
    public Result<SetmealDTO> updateSetmeal(@RequestBody SetmealDTO setmealDTO) {
          log.info("修改套餐信息:{}",setmealDTO);
          setmealService.updateSetmealWithSetmealDish(setmealDTO);
          return  Result.success();
    }
}
