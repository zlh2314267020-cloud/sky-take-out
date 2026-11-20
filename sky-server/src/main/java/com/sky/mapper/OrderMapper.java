package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    //插入订单数据

    void insert(Orders orders);

    @Select("select * from sky_take_out.orders where number = #{number} and user_id = #{userId}")
    Orders getByNumberAndUserId(@Param("number") String number, @Param("userId") Long userId);

    @Update("update sky_take_out.orders " +
            "set status = #{status}, pay_status = #{payStatus}, " +
            "pay_method = #{payMethod}, checkout_time = #{checkoutTime} " +
            "where id = #{id} and user_id = #{userId} and status = 1 and pay_status = 0")
    int updatePaymentStatus(Orders orders);
    //历史订单查询
    Page<OrderVO> getHistoryOrdersPage(OrdersPageQueryDTO ordersPageQueryDTO);
    //查询detailorder细节
    @Select("select * from sky_take_out.order_detail where order_id=#{orderId}")
    List<OrderDetail> getOrderDetailById(Long id);
    //查询detailorder细节，单个
    @Select("select * from sky_take_out.order_detail where order_id=#{id}")
    OrderDetail getOrderDetailById1(Long id);
    @Select("select * from sky_take_out.orders where id=#{id}")
    Orders getOrderByid(Long id);
    //取消订单
    @Update("update sky_take_out.orders " +
            "set status = #{status}, pay_status = #{payStatus}, " +
            "cancel_reason = #{cancelReason}, cancel_time = #{cancelTime} " +
            "where id = #{id} and user_id = #{userId} and status in (1, 2)")
    int cancel(Orders order);

    @Update("update sky_take_out.orders " +
            "set status = #{status}, pay_status = #{payStatus}, " +
            "cancel_reason = #{cancelReason}, cancel_time = #{cancelTime} " +
            "where id = #{id} and status in (1, 2, 3, 4, 5)")
    int adminCancel(Orders order);

    Page<OrderVO> getAdminOrderPage(OrdersPageQueryDTO ordersPageQueryDTO);
    @Update("update sky_take_out.orders set status = #{status} where id=#{id} and status=2" )
    int updateStatus(Orders order);
    @Update("update sky_take_out.orders " +
            "set status = #{status}, " +
            "    rejection_reason = #{rejectionReason}, " +
            "    cancel_reason = #{cancelReason}, " +
            "    cancel_time = #{cancelTime}, " +
            "    pay_status = #{payStatus} " +
            "where id = #{id} and status = 2")
    int rejection(Orders order);
    @Update("update sky_take_out.orders set status = #{status} where id=#{id} and status=3" )
    void updateStatusDelivery(Orders order);
    @Update("update sky_take_out.orders " +
            "set status = #{status}, " +
            "    delivery_time = #{deliveryTime} " +
            "where id = #{id} and status = 4")
    int updatecomplete(Orders order);
    @Update("update sky_take_out.orders " +
            "set status = #{status}, pay_status = #{payStatus}, " +
            "cancel_reason = #{cancelReason}, cancel_time = #{cancelTime} " +
            "where id = #{id} and status = 1")
    int timeoutCancel(Orders order);
    @Select("select * from sky_take_out.orders " +
            "where status = #{status} and order_time < #{time}")
    List<Orders> Timeout(@Param("status") Integer status,
                         @Param("time") LocalDateTime time);

    Double sumByMap(Map map);

    Integer sumorderByMap(Map map);

    List<GoodsSalesDTO>getSalesTop10(LocalDateTime begin,LocalDateTime end);
    Integer countByMap(Map map);
}
