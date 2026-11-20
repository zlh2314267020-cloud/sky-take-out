package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.io.File.separator;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //当前集合用于存放从begin到end范围内每天的日期
        List<LocalDate> dateList=new ArrayList<>();
        List<Double> turnoverList=new ArrayList<>();
        dateList.add(begin);
        while(!begin.equals(end)) {
            //计算指定日期的后一天
            begin=begin.plusDays(1);
            dateList.add(begin);
        }
        for(LocalDate date : dateList) {
            //查询date日期对应的营业额，营业额是指状态已完成的订单金额合计
            LocalDateTime beginTime=LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime=LocalDateTime.of(date, LocalTime.MAX);
            //select sum(amount) from orders where order_time>? and order_time<? and status = 5
            Map map=new HashMap<>();
            map.put("begin",beginTime);
            map.put("end",endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover= orderMapper.sumByMap(map);
            turnover=turnover == null ? 0.0:turnover;
            turnoverList.add(turnover);

        }

        return TurnoverReportVO.builder()
                .dateList( StringUtils.join(dateList,","))
                .turnoverList(StringUtils.join(turnoverList,","))
                .build();
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList=new ArrayList<>();
        dateList.add(begin);
        while(!begin.equals(end)) {
            begin=begin.plusDays(1);
            dateList.add(begin);
        }
        List<Integer> newUserList=new ArrayList<>();
        List<Integer> totalUserList=new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime=LocalDateTime.of(date,LocalTime.MIN);
            LocalDateTime endTime=LocalDateTime.of(date,LocalTime.MAX);
            Map map=new HashMap<>();
            map.put("end",endTime);
           Integer totaluser= userMapper.countBymap(map);
            map.put("begin",beginTime);
            Integer newuser=userMapper.countBymap(map);
            totalUserList.add(totaluser);
            newUserList.add(newuser);
        }
        return  new UserReportVO().builder()
                .dateList(StringUtils.join(dateList,","))
                .totalUserList(StringUtils.join(totalUserList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .build();
    }

    //统计订单
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList=new ArrayList<>();
        dateList.add(begin);
        while(!begin.equals(end)) {
            begin=begin.plusDays(1);
            dateList.add(begin);
        }
        List<Integer> orderList=new ArrayList<>();
        List<Integer> validOrderList=new ArrayList<>();
        //遍历datelist，查询每天的订单数和订单总数
        for(LocalDate date : dateList) {
            //查询每天订单的总数
            LocalDateTime beginTime=LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime=LocalDateTime.of(date, LocalTime.MAX);
            Map map=new HashMap<>();
            map.put("begin",beginTime);
            map.put("end",endTime);
            map.put("status", null);
            Integer orders= orderMapper.sumorderByMap(map);
            map.put("status", Orders.COMPLETED);
            //查询每天订单的有效数量
            Integer validorders= orderMapper.sumorderByMap(map);
            orderList.add(orders);
            validOrderList.add(validorders);

        }
        //查询时间区间内总的订单数
        Integer totalordercount = orderList.stream().reduce(Integer::sum).get();
        //计算时间区域内的有效订单数量
        Integer totalvalidordercount = validOrderList.stream().reduce(Integer::sum).get();

        Double ordercompleteRate=0.0;
        if (totalordercount!=0)
            //计算订单完成率
            ordercompleteRate = totalvalidordercount.doubleValue()/totalordercount;
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .orderCountList(StringUtils.join(orderList,","))
                .validOrderCountList(StringUtils.join(validOrderList,","))
                .totalOrderCount(totalordercount)
                .validOrderCount(totalvalidordercount)
                .orderCompletionRate(ordercompleteRate)
                .build();
    }

    @Override
    public SalesTop10ReportVO getOrdersSales(LocalDate begin, LocalDate end) {
        //统计时间区间内排名销量前10
        LocalDateTime beginTime=LocalDateTime.of(begin,LocalTime.MIN);
        LocalDateTime endTime=LocalDateTime.of(end,LocalTime.MAX);
        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);
        List<String> names = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String nameList = StringUtils.join(names, ",");
        List<Integer> numbers = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numberList = StringUtils.join(numbers, ",");

        return SalesTop10ReportVO.builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }
}
