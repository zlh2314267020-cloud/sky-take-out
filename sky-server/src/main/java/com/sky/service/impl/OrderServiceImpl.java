package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.OrderReminder;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.OrderReminderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
   //用户下单
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private OrderReminderMapper orderReminderMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private WebSocketServer webSocketServer;

    @Override
    @Transactional
    public void payment(OrdersPaymentDTO ordersPaymentDTO) {
        Long userId = BaseContext.getCurrentId();
        if (ordersPaymentDTO == null || ordersPaymentDTO.getOrderNumber() == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        Orders orders = orderMapper.getByNumberAndUserId(ordersPaymentDTO.getOrderNumber(), userId);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 重复请求直接视为成功，避免网络重试导致客户端提示失败。
        if (Orders.PAID.equals(orders.getPayStatus())) {
            return;
        }
        if (!Orders.PENDING_PAYMENT.equals(orders.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        orders.setStatus(Orders.TO_BE_CONFIRMED);
        orders.setPayStatus(Orders.PAID);
        orders.setPayMethod(
                ordersPaymentDTO.getPayMethod() == null ? 1 : ordersPaymentDTO.getPayMethod());
        orders.setCheckoutTime(LocalDateTime.now());

        if (orderMapper.updatePaymentStatus(orders) == 0) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //通过websocket向客户端浏览器推送消息
        Map map=new HashMap();
        map.put("type",1);
        map.put("orderId",orders.getId());
        map.put("content","订单号："+ordersPaymentDTO.getOrderNumber());
        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);


    }

    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //处理各种业务异常（地址为空，购物车数据为空
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            //抛出业务异常
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //查询当前的用户购物车
        Long userId= BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList= shoppingCartMapper.list(shoppingCart);
        if (shoppingCartList == null || shoppingCartList.size() == 0) {
            throw new StringIndexOutOfBoundsException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //像订单插入一条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(userId);
        orders.setAddress(addressBook.getProvinceName()+addressBook.getCityName()+addressBook.getDetail());
        orderMapper.insert(orders);
        List<OrderDetail> orderDetailList=new ArrayList<>();
        //向订单明细表插入n条数据
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();//订单明细
            BeanUtils.copyProperties(cart,orderDetail);
            orderDetail.setOrderId(orders.getId());//设置当前订单明细关联的订单id
            orderDetailList.add(orderDetail);

        }
        orderDetailMapper.insertBatch(orderDetailList);//批量插入订单明细数据
        //清空当前用户的购物车数据
        shoppingCartMapper.deleteByuserId(userId);
        //封装VO返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();
        return orderSubmitVO;
    }
    //历史订单查询
    @Override
    public PageResult historyOrder(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        Page<OrderVO> page =orderMapper.getHistoryOrdersPage(ordersPageQueryDTO);
        for(OrderVO orderVO:page.getResult()){
            List<OrderDetail> orderDetailList=orderMapper.getOrderDetailById(orderVO.getId());
            orderVO.setOrderDetailList(orderDetailList);
        }

        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    public OrderDetail selectOrderDetail(Long id) {
        OrderDetail orderDetail= orderMapper.getOrderDetailById1(id);
        return  orderDetail;
    }

    @Override
    public Orders getOrderByID(Long id) {

        return orderMapper.getOrderByid(id);
    }
    //获取订单详情
    @Override
    public List<OrderDetail> getorderDetail(Long id) {
        return orderMapper.getOrderDetailById(id);
    }
    //取消订单
    @Override
    @Transactional
    public void cancel(Long id) {
        Long userId = BaseContext.getCurrentId();
        Orders order = orderMapper.getOrderByid(id);
        if (order == null || !userId.equals(order.getUserId())) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!Orders.PENDING_PAYMENT.equals(order.getStatus())
                && !Orders.TO_BE_CONFIRMED.equals(order.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        order.setStatus(Orders.CANCELLED);
        order.setCancelReason("用户取消");
        order.setCancelTime(LocalDateTime.now());
        if (Orders.PAID.equals(order.getPayStatus())) {
            order.setPayStatus(Orders.REFUND);
        }

        if (orderMapper.cancel(order) == 0) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
    }

    //需要跳转到购物的界面，然后需要将订单的订单详情都返回到购物车当中
    @Transactional
    public void repetition(Long id) {
        Long userid=BaseContext.getCurrentId();
        //判断是否是用户id的订单号
        Orders order = orderMapper.getOrderByid(id);
        if (order == null || !userid.equals(order.getUserId())) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);}
        //添加订单详情集合信息
        List<OrderDetail> orderDetailList=orderMapper.getOrderDetailById(id);
        if (orderDetailList == null || orderDetailList.isEmpty()) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
       List<ShoppingCart>  shoppingCartList=new ArrayList<>(orderDetailList.size());
        //购物车添加信息进去
        for(OrderDetail orderDetail:orderDetailList){
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail,shoppingCart);
            shoppingCart.setUserId(userid);
            shoppingCart.setId(null);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartList.add(shoppingCart);
        }

        shoppingCartMapper.deleteByuserId(userid);
        shoppingCartMapper.insertbatch(shoppingCartList);
    }
    //  催单
    @Override
    @Transactional
    public void reminder(Long orderId) {
        Long userId = BaseContext.getCurrentId();
        Orders order = orderMapper.getOrderByid(orderId);

        if (order == null || !userId.equals(order.getUserId())) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        Integer status = order.getStatus();
        boolean allowed = Orders.TO_BE_CONFIRMED.equals(status)
                || Orders.CONFIRMED.equals(status)
                || Orders.DELIVERY_IN_PROGRESS.equals(status);
        if (!allowed) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        String reminderKey = "order:reminder:" + orderId + ":" + userId;
        Boolean firstReminder = stringRedisTemplate.opsForValue()
                .setIfAbsent(reminderKey, "1", Duration.ofSeconds(60));
        if (!Boolean.TRUE.equals(firstReminder)) {
            throw new OrderBusinessException(MessageConstant.REMINDER_TOO_FREQUENT);
        }

        OrderReminder reminder = OrderReminder.builder()
                .orderId(orderId)
                .userId(userId)
                .reminderTime(LocalDateTime.now())
                .handled(0)
                .build();

        try {
            if (orderReminderMapper.insert(reminder) != 1) {
                throw new OrderBusinessException(MessageConstant.UNKNOWN_ERROR);
            }
        } catch (RuntimeException ex) {
            // 数据库写入失败时释放限流标记，允许用户重新尝试。
            stringRedisTemplate.delete(reminderKey);
            throw ex;
        }
        //通过websocket向客户端浏览器推送消息
        Map map=new HashMap();
        map.put("type",2);
        map.put("orderId",reminder.getOrderId());
        map.put("content","订单号："+order.getNumber());
        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);

    }

    //管理端查询订单界面
    public PageResult selectOrderAdmin(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        Page<OrderVO> page =orderMapper.getAdminOrderPage(ordersPageQueryDTO);
        for(OrderVO orderVO:page.getResult()){
            List<OrderDetail> orderDetailList=orderMapper.getOrderDetailById(orderVO.getId());
            orderVO.setOrderDetailList(orderDetailList);
            String orderDishes = orderDetailList.stream()
                    .map(item -> item.getName() + "*" + item.getNumber())
                    .collect(Collectors.joining("；"));

            orderVO.setOrderDishes(orderDishes);//stream流
        }

        return new PageResult(page.getTotal(),page.getResult());
    }

   //更新订单变为接单
    public void confirm(Long id) {
        Orders order = orderMapper.getOrderByid(id);
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        if (!Orders.TO_BE_CONFIRMED.equals(order.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        order.setStatus(Orders.CONFIRMED);
        orderMapper.updateStatus(order);
    }

    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        if (ordersRejectionDTO == null || ordersRejectionDTO.getId() == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        if (ordersRejectionDTO.getRejectionReason() == null
                || ordersRejectionDTO.getRejectionReason().trim().isEmpty()) {
            throw new OrderBusinessException("拒单原因不能为空");
        }
        Orders order = orderMapper.getOrderByid(ordersRejectionDTO.getId());
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!Orders.TO_BE_CONFIRMED.equals(order.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        order.setCancelTime(LocalDateTime.now());
        order.setStatus(Orders.CANCELLED);
        order.setCancelReason(ordersRejectionDTO.getRejectionReason().trim());
        order.setRejectionReason(ordersRejectionDTO.getRejectionReason().trim());
        if (order.getPayStatus().equals(Orders.PAID)){
            order.setPayStatus(Orders.REFUND);
        }
        if (orderMapper.rejection(order)==0){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
    }

    @Override
    @Transactional
    //商家取消订单
    public void canceladmin(OrdersCancelDTO ordersCancelDTO) {
        if (ordersCancelDTO == null || ordersCancelDTO.getId() == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (ordersCancelDTO.getCancelReason() == null
                || ordersCancelDTO.getCancelReason().trim().isEmpty()) {
            throw new OrderBusinessException("取消原因不能为空");
        }

        Orders order = orderMapper.getOrderByid(ordersCancelDTO.getId());
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        Integer status = order.getStatus();
        boolean allowed = Orders.PENDING_PAYMENT.equals(status)
                || Orders.TO_BE_CONFIRMED.equals(status)
                || Orders.CONFIRMED.equals(status)
                || Orders.DELIVERY_IN_PROGRESS.equals(status)
                || Orders.COMPLETED.equals(status);
        if (!allowed) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        order.setStatus(Orders.CANCELLED);
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(ordersCancelDTO.getCancelReason().trim());
        if (Orders.PAID.equals(order.getPayStatus())) {
            order.setPayStatus(Orders.REFUND);
        }
        if (orderMapper.adminCancel(order) == 0) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
    }

    @Override
    public void delivery(Long id) {
        Orders order=orderMapper.getOrderByid(id);
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!Orders.CONFIRMED.equals(order.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        order.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.updateStatusDelivery(order);
    }

    @Override//完成订单
    @Transactional
    public void complete(Long id) {
        Orders order=orderMapper.getOrderByid(id);
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!Orders.DELIVERY_IN_PROGRESS.equals(order.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        order.setStatus(Orders.COMPLETED);
        order.setDeliveryTime(LocalDateTime.now());
        if (orderMapper.updatecomplete(order)==0){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        };
    }
}
