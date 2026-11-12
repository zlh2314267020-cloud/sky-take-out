package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("userShopController")
@RequestMapping("/user/shop")
@Api("营业状态")
@Slf4j
public class ShopController {
    //设置店铺状态
    @Autowired
    private RedisTemplate redisTemplate;
    @PutMapping("/{status}")
    @ApiOperation("设置营业状态")
    public Result ShopStatus( @PathVariable Integer staus){
        log.info("设置营业状态:{}",staus);
        redisTemplate.opsForValue().set("Shop_status",staus);
        return Result.success();
    }
    @GetMapping("/status")
    @ApiOperation("店铺状态")
    public Result<Integer> GetShopStatus(){
        Integer status = (Integer) redisTemplate.opsForValue().get("Shop_status");
        log.info("店铺状态为：{}",status==1?"营业中":"打烊中");
        return Result.success(status);
    }
}
