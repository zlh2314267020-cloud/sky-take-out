package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;

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

    @Override//导出报表
    public void exportBusinessData(HttpServletResponse response) {
        //查询数据库，获取营业数据 查询最近30天
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);
        //查询概览数据
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(
                LocalDateTime.of(dateBegin, LocalTime.MIN),
                LocalDateTime.of(dateEnd, LocalTime.MAX));
        //通过POI将数据写入到Excel文件
        InputStream in = this.getClass().getClassLoader()
                .getResourceAsStream("template/运营数据报表模板.xlsx");
        if (in == null) {
            throw new IllegalStateException("运营数据报表模板不存在");
        }

        try (InputStream inputStream = in;
             XSSFWorkbook excel = new XSSFWorkbook(inputStream)) {
            //填充数据时间
            Sheet sheet = excel.getSheet("Sheet1");
            if (sheet == null) {
                throw new IllegalStateException("Excel模板中不存在Sheet1");
            }
            sheet.getRow(1).getCell(1)
                    .setCellValue("时间：" + dateBegin + "至" + dateEnd);

            //填充概览数据
            Row row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());

            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            //填充最近30天明细
            for (int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                BusinessDataVO dailyData = workspaceService.getBusinessData(
                        LocalDateTime.of(date, LocalTime.MIN),
                        LocalDateTime.of(date, LocalTime.MAX));

                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(dailyData.getTurnover());
                row.getCell(3).setCellValue(dailyData.getValidOrderCount());
                row.getCell(4).setCellValue(dailyData.getOrderCompletionRate());
                row.getCell(5).setCellValue(dailyData.getUnitPrice());
                row.getCell(6).setCellValue(dailyData.getNewUsers());
            }

            response.setContentType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition",
                    "attachment; filename=business-data.xlsx");
            excel.write(response.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
