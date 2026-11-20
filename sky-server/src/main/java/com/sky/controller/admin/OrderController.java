package com.sky.controller.admin;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("adminorderController")
@RequestMapping("/admin/order")
@Slf4j
@Api("管理端订单接口")
public class OrderController {
    public OrderController() {
    }
    @Autowired
    private OrderService orderService;

    //订单查询接口
    @ApiOperation("管理端相关接口")
    @GetMapping("/conditionSearch")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("管理端订单查询相：{}",ordersPageQueryDTO);
        PageResult pageResult=orderService.selectOrderAdmin(ordersPageQueryDTO);
        return Result.success(pageResult);
    }
    //订单详情
    @ApiOperation("管理端订单详情界面接口")
    @GetMapping("/details/{id}")
    public Result<OrderVO> details(@PathVariable Long id) {
        log.info("管理端查看订单详情接口：{}",id);
        Orders order=orderService.getOrderByID(id);
        List<OrderDetail> orderDetail= orderService.getorderDetail(id);
        OrderVO orderVO=new OrderVO();
        BeanUtils.copyProperties(order,orderVO);
        orderVO.setOrderDetailList(orderDetail);
        return  Result.success(orderVO);
    }
    @ApiOperation("接单")
    @PutMapping("/confirm")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        log.info("管理端接单:{}",ordersConfirmDTO);
        orderService.confirm(ordersConfirmDTO.getId());
        return Result.success();
    }

    //商家拒绝订单
    @PutMapping("/rejection")
    @ApiOperation("拒单")
    public  Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO) {
        log.info("拒单：{}",ordersRejectionDTO);
        orderService.rejection(ordersRejectionDTO);
        return Result.success();
    }
    //商家取消订单
    @ApiOperation("商家取消订单")
    @PutMapping("/cancel")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO) {
        log.info("商家取消订单：{}",ordersCancelDTO);
        orderService.canceladmin(ordersCancelDTO);
        return Result.success();
    }
    //商家派送订单
    @ApiOperation("商家派送订单")
    @PutMapping("/delivery/{id}")
    public Result delivery(@PathVariable  Long id){
        log.info("商家开始派送订单：{}",id);
        orderService.delivery(id);
        return Result.success();
    }
    //完成订单
    @ApiOperation("完成订单接口")
    @PutMapping("complete/{id}")
    public Result complete(@PathVariable Long id){
        log.info("完成订单接口：{}",id);
        orderService.complete(id);
        return Result.success();
    }

}
