package com.sky.service;

import com.sky.dto.*;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.vo.OrderSubmitVO;

import java.util.List;

public interface OrderService {
    //用户下单
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    //开发环境模拟支付
    void payment(OrdersPaymentDTO ordersPaymentDTO);
    //历史订单查询
    PageResult historyOrder(OrdersPageQueryDTO ordersPageQueryDTO);

    OrderDetail selectOrderDetail(Long id);

    Orders getOrderByID(Long id);

    List<OrderDetail> getorderDetail(Long id);
    //取消订单
    void cancel(Long id);

    void repetition(Long id);

    void reminder(Long orderId);

    PageResult selectOrderAdmin(OrdersPageQueryDTO ordersPageQueryDTO);

    void confirm(Long id);

    void rejection(OrdersRejectionDTO ordersRejectionDTO);

    void canceladmin(OrdersCancelDTO ordersCancelDTO);

    void delivery(Long id);
    //完成订单
    void complete(Long id);
}
