
package com.sky.controller.user;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userorderController")
@RequestMapping("/user/order")
public class OrderController {
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    @Autowired
    private OrderService orderService;
    @PostMapping("/submit")
    @ApiOperation("用户下单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }
    @GetMapping("/historyOrders")
    @ApiOperation("历史订单接口")
    public Result<PageResult> historyOrders(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("历史订单：{}",ordersPageQueryDTO);
        PageResult pageResult= orderService.historyOrder(ordersPageQueryDTO);
        return  Result.success(pageResult);
    }

    //订单详情
    @GetMapping("/orderDetail/{id}")
    public Result<OrderVO> selectOrderDetail(@PathVariable Long id){
        log.info("查看订单详情接口：{}",id);
        Orders order=orderService.getOrderByID(id);
        List<OrderDetail> orderDetail= orderService.getorderDetail(id);
        OrderVO orderVO=new OrderVO();
        BeanUtils.copyProperties(order,orderVO);
        orderVO.setOrderDetailList(orderDetail);
        return  Result.success(orderVO);
    }
    //取消订单
    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单")
    public Result cancel(@PathVariable Long id){
        log.info("取消订单模块：{}",id);
        orderService.cancel(id);
        return Result.success();
    }
    //再来一单:需要将订单中对应的订单详情数据恢复到购物车当中
    @PostMapping("/repetition/{id}")
    public Result repetition(@PathVariable Long id){
        log.info("再来一单：#{}",id);
        orderService.repetition(id);
       return Result.success();
    }

    @GetMapping("/reminder/{id}")
    @ApiOperation("催单")
    public Result reminder(@PathVariable Long id) {
        log.info("用户催单，订单id：{}", id);
        orderService.reminder(id);
        return Result.success();
    }
}
