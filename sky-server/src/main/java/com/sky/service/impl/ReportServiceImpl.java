package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sky.entity.Orders;
import com.sky.entity.User;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private WorkspaceService workspaceService;

    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        QueryWrapper<Orders> ordersWrapper = Wrappers.query(Orders.class);
        ordersWrapper.select("DATE(order_time)","sum(amount) AS total_amount")
                .between("Date(order_time)",begin,end)
                .eq("status",Orders.COMPLETED)
                .groupBy("DATE(order_time)")
                .orderByAsc("DATE(order_time)");
        List<Map<String, Object>> results = ordersMapper.selectMaps(ordersWrapper);
        // 获取所有日期范围
        List<LocalDate> allDates = new ArrayList<>();
        LocalDate currentDate = begin;
        while (!currentDate.isAfter(end)) {
            allDates.add(currentDate);
            currentDate = currentDate.plusDays(1);
        }
        System.out.println(results);
        Map<String, String> dateToTurnover = new HashMap<>();
        for (Map<String, Object> result : results) {
            Object dateObj = result.get("DATE(order_time)");
            Object turnoverObj = result.get("total_amount");
            if (dateObj != null) {
                dateToTurnover.put(dateObj.toString(), turnoverObj != null ? turnoverObj.toString() : "0");
            }
        }
        StringJoiner dateJoiner = new StringJoiner(",");
        StringJoiner turnoverJoiner = new StringJoiner(",");

        // 遍历所有日期，检查并填充营业额为0的数据
        for (LocalDate date : allDates) {
            String dateStr = date.toString();
            dateJoiner.add(dateStr);

            // 如果查询结果中没有这个日期，设置营业额为0
            String turnover = dateToTurnover.getOrDefault(dateStr, "0");
            turnoverJoiner.add(turnover);
        }
        return new TurnoverReportVO(dateJoiner.toString(),turnoverJoiner.toString());
    }

    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> allDates = new ArrayList<>();
        LocalDate currentDate = begin;
        while (!currentDate.isAfter(end)) {
            allDates.add(currentDate);
            currentDate = currentDate.plusDays(1);
        }
        QueryWrapper<User> newUserWrapper = Wrappers.query(User.class);
        newUserWrapper.select("DATE(create_time)","count(id)")
                .between("DATE(create_time)",begin,end)
                .groupBy("DATE(create_time)");
        List<Map<String, Object>> mapList = userMapper.selectMaps(newUserWrapper);
        Map<String,String> dateToNew = new HashMap<>();
        for (Map<String, Object> map : mapList) {
            Object dateObj = map.get("DATE(create_time)");
            Object countObj = map.get("count(id)");
            if(dateObj != null){
                dateToNew.put(dateObj.toString(), countObj != null ? countObj.toString() : "0");
            }
        }
        StringJoiner dateJoiner = new StringJoiner(",");
        StringJoiner newJoiner = new StringJoiner(",");
        StringJoiner allJoiner = new StringJoiner(",");
        QueryWrapper<User> allUserWrapper = Wrappers.query(User.class);
        allUserWrapper.select("id").le("create_time",begin);
        Long startCount = userMapper.selectCount(allUserWrapper);
        for (LocalDate date : allDates) {
            dateJoiner.add(date.toString());
            String newCountStr = dateToNew.getOrDefault(date.toString(), "0");
            newJoiner.add(newCountStr);
            startCount += Long.parseLong(newCountStr);
            allJoiner.add(startCount.toString());
        }
        return new UserReportVO(
                dateJoiner.toString(),
                allJoiner.toString(),
                newJoiner.toString()
        );
    }

    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> allDates = new ArrayList<>();
        LocalDate currentDate = begin;
        while(!currentDate.isAfter(end)){
            allDates.add(currentDate);
            currentDate = currentDate.plusDays(1);
        }
        QueryWrapper<Orders> queryWrapper = Wrappers.query(Orders.class);
        // 构建查询
        queryWrapper.select(
                        "DATE(order_time) AS order_date",
                        "COUNT(1) AS orders_count",
                        "COUNT(CASE WHEN status = 5 THEN 1 END) AS valid_orders_count"
                )
                .between("DATE(order_time)", begin, end)  // 设置时间范围
                .groupBy("DATE(order_time)");  // 按日期分组
        List<Map<String, Object>> mapList = ordersMapper.selectMaps(queryWrapper);
        Map<String,String> orderCountMap = new HashMap<>();
        Map<String,String> validOrderCountMap = new HashMap<>();
        for (Map<String, Object> map : mapList) {
            Object dateObj = map.get("order_date");
            String dateObjString = dateObj.toString();
            Object orderCountObj = map.get("orders_count");
            Object validOrderCountObj = map.get("valid_orders_count");
            orderCountMap.put(dateObjString,orderCountObj !=null? orderCountObj.toString() : "0");
            validOrderCountMap.put(dateObjString,validOrderCountObj != null? validOrderCountObj.toString() : "0");
        }
        StringJoiner dateJoiner = new StringJoiner(",");
        StringJoiner orderCountJoiner = new StringJoiner(",");
        StringJoiner validOrderCountJoiner = new StringJoiner(",");
        Integer totalOrderCount = 0;
        Integer validOrderCount = 0;
        for (LocalDate allDate : allDates) {
            String dateStr = allDate.toString();
            dateJoiner.add(dateStr);
            String orderCountStr = orderCountMap.getOrDefault(dateStr,"0");
            totalOrderCount += Integer.parseInt(orderCountStr);
            orderCountJoiner.add(orderCountStr);
            String validCountStr = validOrderCountMap.getOrDefault(dateStr,"0");
            validOrderCount += Integer.parseInt(validCountStr);
            validOrderCountJoiner.add(validCountStr);
        }
        return new OrderReportVO(
                dateJoiner.toString(),
                orderCountJoiner.toString(),
                validOrderCountJoiner.toString(),
                totalOrderCount,
                validOrderCount,
                totalOrderCount == 0? 0.0 : Double.valueOf(validOrderCount)/totalOrderCount
        );
    }

    @Override
    public SalesTop10ReportVO popularDish(LocalDate begin, LocalDate end) {
        List<Map<String,Object>> mapList = orderDetailMapper.getTop10(begin,end);
        StringJoiner dishNameJoiner = new StringJoiner(",");
        StringJoiner salesJoiner = new StringJoiner(",");
        for (Map<String, Object> map : mapList) {
            dishNameJoiner.add(map.get("dish_name").toString());
            Object saleObj = map.get("total_sales");
            salesJoiner.add(saleObj != null? saleObj.toString() : "0");
        }
        return new SalesTop10ReportVO(
                dishNameJoiner.toString(),
                salesJoiner.toString()
        );
    }

    @Override
    public void exportBusinessData(HttpServletResponse response) {
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now();
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(begin.atStartOfDay(), end.atStartOfDay());
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            XSSFWorkbook excel = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = excel.getSheet("Sheet1");
            sheet.getRow(1).getCell(1).setCellValue("时间："+begin+"至"+end);
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());

            //获得第5行
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            //填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = begin.plusDays(i);
                //查询某一天的营业数据
                BusinessDataVO businessData = workspaceService.getBusinessData(date.atStartOfDay(),date.plusDays(1).atStartOfDay());

                //获得某一行
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            //3. 通过输出流将Excel文件下载到客户端浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);
            //关闭资源
            out.close();
            excel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
