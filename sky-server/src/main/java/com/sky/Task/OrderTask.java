package com.sky.Task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;
    @Scheduled(cron = "0 * * * * ?")
    //订单超时
    public void  processTimeoutOrder(){
        log.info("定时处理订单：{}", LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        List<Orders> orderList =orderMapper.Timeout(Orders.PENDING_PAYMENT,time);
        for (Orders order : orderList) {
            order.setStatus(Orders.CANCELLED);
            order.setCancelReason("订单超时");
            order.setCancelTime(LocalDateTime.now());
            if (orderMapper.timeoutCancel(order) == 0) {
                log.warn("超时订单取消失败，订单id：{}", order.getId());
            }
        }

    }
    @Scheduled(cron = "0 0 1 * * ?")
    //订单配送超时
    public void  processDeliveryOrder(){
        log.info("定时处理配送超时订单：{}", LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        List<Orders> orderList =orderMapper.Timeout(Orders.DELIVERY_IN_PROGRESS,time);
        for (Orders order : orderList) {
            order.setStatus(Orders.COMPLETED);
            order.setDeliveryTime(LocalDateTime.now());
            if (orderMapper.updatecomplete(order) == 0) {
                log.warn("派送超时订单完成失败，订单id：{}", order.getId());
            }
        }

    }
}
