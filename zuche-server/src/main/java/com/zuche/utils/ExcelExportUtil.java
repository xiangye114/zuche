package com.zuche.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.zuche.entity.User;
import com.zuche.entity.Car;
import com.zuche.entity.Order;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel导出工具类
 */
public class ExcelExportUtil {

    /**
     * 导出用户数据
     */
    public static void exportUsers(List<User> users, HttpServletResponse response) throws IOException {
        List<UserExportVO> data = new ArrayList<>();
        for (User user : users) {
            UserExportVO vo = new UserExportVO();
            vo.setId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setPhone(user.getPhone());
            vo.setEmail(user.getEmail());
            vo.setRole(getRoleName(user.getRole()));
            vo.setStatus(user.getStatus() == 1 ? "正常" : "禁用");
            vo.setCreateTime(user.getCreateTime() != null ? user.getCreateTime().toString() : "");
            data.add(vo);
        }
        
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("用户列表", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        
        EasyExcel.write(response.getOutputStream(), UserExportVO.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet("用户列表")
                .doWrite(data);
    }

    /**
     * 导出车辆数据
     */
    public static void exportCars(List<Car> cars, HttpServletResponse response) throws IOException {
        List<CarExportVO> data = new ArrayList<>();
        for (Car car : cars) {
            CarExportVO vo = new CarExportVO();
            vo.setId(car.getId());
            vo.setName(car.getName());
            vo.setBrandId(car.getBrandId());
            vo.setCategoryId(car.getCategoryId());
            vo.setStoreId(car.getStoreId());
            vo.setColor(car.getColor());
            vo.setPricePerDay(car.getPricePerDay());
            vo.setDeposit(car.getDeposit());
            vo.setSeats(car.getSeats());
            vo.setTransmission(car.getTransmission());
            vo.setFuelType(car.getFuelType());
            vo.setStatus(getCarStatusName(car.getStatus()));
            vo.setCreateTime(car.getCreateTime() != null ? car.getCreateTime().toString() : "");
            data.add(vo);
        }
        
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("车辆列表", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        
        EasyExcel.write(response.getOutputStream(), CarExportVO.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet("车辆列表")
                .doWrite(data);
    }

    /**
     * 导出订单数据
     */
    public static void exportOrders(List<Order> orders, HttpServletResponse response) throws IOException {
        List<OrderExportVO> data = new ArrayList<>();
        for (Order order : orders) {
            OrderExportVO vo = new OrderExportVO();
            vo.setId(order.getId());
            vo.setUserId(order.getUserId());
            vo.setCarId(order.getCarId());
            vo.setPickupStoreId(order.getPickupStoreId());
            vo.setReturnStoreId(order.getReturnStoreId());
            vo.setStartDate(order.getStartTime() != null ? order.getStartTime().toString() : "");
            vo.setEndDate(order.getEndTime() != null ? order.getEndTime().toString() : "");
            vo.setDays(order.getTotalPrice() != null ? order.getTotalPrice().intValue() : 0);
            vo.setTotalPrice(order.getTotalPrice());
            vo.setStatus(getOrderStatusName(order.getStatus()));
            vo.setCreateTime(order.getCreateTime() != null ? order.getCreateTime().toString() : "");
            data.add(vo);
        }
        
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("订单列表", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        
        EasyExcel.write(response.getOutputStream(), OrderExportVO.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet("订单列表")
                .doWrite(data);
    }

    private static String getRoleName(Integer role) {
        if (role == null) return "普通用户";
        switch (role) {
            case 1: return "超级管理员";
            case 2: return "门店管理员";
            default: return "普通用户";
        }
    }

    private static String getCarStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "维修中";
            case 1: return "可租";
            case 2: return "已租";
            default: return "未知";
        }
    }

    private static String getOrderStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待支付";
            case 1: return "已支付";
            case 2: return "租赁中";
            case 3: return "已完成";
            case 4: return "已取消";
            default: return "未知";
        }
    }

    /**
     * 用户导出VO
     */
    public static class UserExportVO {
        @com.alibaba.excel.annotation.ExcelProperty("用户ID")
        private Long id;
        @com.alibaba.excel.annotation.ExcelProperty("用户名")
        private String username;
        @com.alibaba.excel.annotation.ExcelProperty("手机号")
        private String phone;
        @com.alibaba.excel.annotation.ExcelProperty("邮箱")
        private String email;
        @com.alibaba.excel.annotation.ExcelProperty("角色")
        private String role;
        @com.alibaba.excel.annotation.ExcelProperty("状态")
        private String status;
        @com.alibaba.excel.annotation.ExcelProperty("创建时间")
        private String createTime;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getCreateTime() { return createTime; }
        public void setCreateTime(String createTime) { this.createTime = createTime; }
    }

    /**
     * 车辆导出VO
     */
    public static class CarExportVO {
        @com.alibaba.excel.annotation.ExcelProperty("车辆ID")
        private Long id;
        @com.alibaba.excel.annotation.ExcelProperty("车辆名称")
        private String name;
        @com.alibaba.excel.annotation.ExcelProperty("品牌ID")
        private Long brandId;
        @com.alibaba.excel.annotation.ExcelProperty("车型ID")
        private Long categoryId;
        @com.alibaba.excel.annotation.ExcelProperty("门店ID")
        private Long storeId;
        @com.alibaba.excel.annotation.ExcelProperty("颜色")
        private String color;
        @com.alibaba.excel.annotation.ExcelProperty("日租金")
        private Double pricePerDay;
        @com.alibaba.excel.annotation.ExcelProperty("押金")
        private Double deposit;
        @com.alibaba.excel.annotation.ExcelProperty("座位数")
        private Integer seats;
        @com.alibaba.excel.annotation.ExcelProperty("变速箱")
        private String transmission;
        @com.alibaba.excel.annotation.ExcelProperty("燃油类型")
        private String fuelType;
        @com.alibaba.excel.annotation.ExcelProperty("状态")
        private String status;
        @com.alibaba.excel.annotation.ExcelProperty("创建时间")
        private String createTime;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Long getBrandId() { return brandId; }
        public void setBrandId(Long brandId) { this.brandId = brandId; }
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
        public Long getStoreId() { return storeId; }
        public void setStoreId(Long storeId) { this.storeId = storeId; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        public Double getPricePerDay() { return pricePerDay; }
        public void setPricePerDay(Double pricePerDay) { this.pricePerDay = pricePerDay; }
        public Double getDeposit() { return deposit; }
        public void setDeposit(Double deposit) { this.deposit = deposit; }
        public Integer getSeats() { return seats; }
        public void setSeats(Integer seats) { this.seats = seats; }
        public String getTransmission() { return transmission; }
        public void setTransmission(String transmission) { this.transmission = transmission; }
        public String getFuelType() { return fuelType; }
        public void setFuelType(String fuelType) { this.fuelType = fuelType; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getCreateTime() { return createTime; }
        public void setCreateTime(String createTime) { this.createTime = createTime; }
    }

    /**
     * 订单导出VO
     */
    public static class OrderExportVO {
        @com.alibaba.excel.annotation.ExcelProperty("订单ID")
        private Long id;
        @com.alibaba.excel.annotation.ExcelProperty("用户ID")
        private Long userId;
        @com.alibaba.excel.annotation.ExcelProperty("车辆ID")
        private Long carId;
        @com.alibaba.excel.annotation.ExcelProperty("取车门店ID")
        private Long pickupStoreId;
        @com.alibaba.excel.annotation.ExcelProperty("还车门店ID")
        private Long returnStoreId;
        @com.alibaba.excel.annotation.ExcelProperty("开始日期")
        private String startDate;
        @com.alibaba.excel.annotation.ExcelProperty("结束日期")
        private String endDate;
        @com.alibaba.excel.annotation.ExcelProperty("租赁天数")
        private Integer days;
        @com.alibaba.excel.annotation.ExcelProperty("总金额")
        private Double totalPrice;
        @com.alibaba.excel.annotation.ExcelProperty("订单状态")
        private String status;
        @com.alibaba.excel.annotation.ExcelProperty("创建时间")
        private String createTime;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Long getCarId() { return carId; }
        public void setCarId(Long carId) { this.carId = carId; }
        public Long getPickupStoreId() { return pickupStoreId; }
        public void setPickupStoreId(Long pickupStoreId) { this.pickupStoreId = pickupStoreId; }
        public Long getReturnStoreId() { return returnStoreId; }
        public void setReturnStoreId(Long returnStoreId) { this.returnStoreId = returnStoreId; }
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        public Integer getDays() { return days; }
        public void setDays(Integer days) { this.days = days; }
        public Double getTotalPrice() { return totalPrice; }
        public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getCreateTime() { return createTime; }
        public void setCreateTime(String createTime) { this.createTime = createTime; }
    }
}
