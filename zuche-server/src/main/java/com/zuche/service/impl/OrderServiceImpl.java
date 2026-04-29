 package com.zuche.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zuche.dto.OrderDTO;
import com.zuche.dto.OrderDetailDTO;
import com.zuche.dto.OrderFeeItemDTO;
import com.zuche.dto.OrderLogDTO;
import com.zuche.dto.OrderQueryDTO;
import com.zuche.dto.OrderCarCheckDTO;
import com.zuche.dto.CancelReasonDTO;
import com.zuche.entity.Brand;
import com.zuche.entity.Car;
import com.zuche.entity.Category;
import com.zuche.entity.Order;
import com.zuche.entity.OrderLog;
import com.zuche.entity.OrderCarCheck;
import com.zuche.entity.CancelReason;
import com.zuche.entity.Store;
import com.zuche.entity.User;
import com.zuche.mapper.BrandMapper;
import com.zuche.mapper.CarMapper;
import com.zuche.mapper.CategoryMapper;
import com.zuche.mapper.OrderMapper;
import com.zuche.mapper.OrderLogMapper;
import com.zuche.mapper.OrderCarCheckMapper;
import com.zuche.mapper.CancelReasonMapper;
import com.zuche.mapper.StoreMapper;
import com.zuche.mapper.UserMapper;
import com.zuche.service.OrderService;
import com.zuche.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private CarMapper carMapper;

    @Resource
    private StoreMapper storeMapper;

    @Resource
    private BrandMapper brandMapper;

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private OrderLogMapper orderLogMapper;

    @Resource
    private OrderCarCheckMapper orderCarCheckMapper;

    @Resource
    private CancelReasonMapper cancelReasonMapper;

    @Override
    public Page<OrderDTO> getList(Integer page, Integer size, Long userId, Integer status) {
        Page<Order> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
            .eq(Order::getUserId, userId)
            .eq(Order::getDeleted, 0);

        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }

        wrapper.orderByDesc(Order::getCreateTime);
        Page<Order> orderPage = orderMapper.selectPage(pageParam, wrapper);

        List<OrderDTO> dtoList = orderPage.getRecords().stream().map(this::convertToDTO).collect(Collectors.toList());

        Page<OrderDTO> resultPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        resultPage.setRecords(dtoList);
        return resultPage;
    }

    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setUserId(order.getUserId());
        dto.setCarId(order.getCarId());
        dto.setStartTime(order.getStartTime());
        dto.setEndTime(order.getEndTime());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setDeposit(order.getDeposit());
        dto.setPickupStoreId(order.getPickupStoreId());
        dto.setReturnStoreId(order.getReturnStoreId());
        dto.setStatus(order.getStatus());
        dto.setCreateTime(order.getCreateTime());
        dto.setUpdateTime(order.getUpdateTime());

        // 获取车辆信息
        if (order.getCarId() != null) {
            Car car = carMapper.selectById(order.getCarId());
            if (car != null) {
                dto.setCarName(car.getName());
                dto.setCarImage(car.getImage());
                dto.setCarBrand(getBrandName(car.getBrandId()));
                dto.setCarCategory(getCategoryName(car.getCategoryId()));
            }
        }

        // 获取门店信息
        if (order.getPickupStoreId() != null) {
            Store store = storeMapper.selectById(order.getPickupStoreId());
            if (store != null) {
                dto.setPickupStoreName(store.getName());
            }
        }

        if (order.getReturnStoreId() != null) {
            Store store = storeMapper.selectById(order.getReturnStoreId());
            if (store != null) {
                dto.setReturnStoreName(store.getName());
            }
        }

        // 获取用户信息
        if (order.getUserId() != null) {
            User user = userMapper.selectById(order.getUserId());
            if (user != null) {
                dto.setUserName(user.getUsername());
                dto.setUserPhone(user.getPhone());
            }
        }

        return dto;
    }

    private String getBrandName(Long brandId) {
        if (brandId == null) return "";
        Brand brand = brandMapper.selectById(brandId);
        return brand != null ? brand.getName() : "";
    }

    private String getCategoryName(Long categoryId) {
        if (categoryId == null) return "";
        Category category = categoryMapper.selectById(categoryId);
        return category != null ? category.getName() : "";
    }

    private String getStatusText(Integer status) {
        switch (status) {
            case 0: return "待确认";
            case 1: return "已确认";
            case 2: return "已取车";
            case 3: return "已还车";
            case 4: return "已完成";
            case 5: return "已取消";
            default: return "未知";
        }
    }

    @Override
    public Order getById(Long id) {
        return orderMapper.selectById(id);
    }

    @Override
    public OrderDetailDTO getDetail(Long id, Long userId) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权查看此订单");
        }

        return buildOrderDetailDTO(order);
    }

    @Override
    public OrderDetailDTO getAdminDetail(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return buildOrderDetailDTO(order);
    }

    private OrderDetailDTO buildOrderDetailDTO(Order order) {
        OrderDetailDTO dto = new OrderDetailDTO();
        
        // 基础订单信息
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setUserId(order.getUserId());
        dto.setCarId(order.getCarId());
        dto.setStartTime(order.getStartTime());
        dto.setEndTime(order.getEndTime());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setDeposit(order.getDeposit());
        dto.setPickupStoreId(order.getPickupStoreId());
        dto.setReturnStoreId(order.getReturnStoreId());
        dto.setStatus(order.getStatus());
        dto.setStatusText(getStatusText(order.getStatus()));
        dto.setCreateTime(order.getCreateTime());
        dto.setUpdateTime(order.getUpdateTime());
        
        // 取还车信息
        dto.setPickupTime(order.getPickupTime());
        dto.setPickupOperator(order.getPickupOperator());
        dto.setPickupRemark(order.getPickupRemark());
        dto.setReturnTime(order.getReturnTime());
        dto.setReturnOperator(order.getReturnOperator());
        dto.setReturnRemark(order.getReturnRemark());

        // 获取用户信息
        User user = userMapper.selectById(order.getUserId());
        if (user != null) {
            dto.setUserName(user.getUsername());
            dto.setUserPhone(user.getPhone());
            dto.setUserEmail(user.getEmail());
            dto.setUserAvatar(user.getAvatar());
        }

        // 获取车辆信息
        if (order.getCarId() != null) {
            Car car = carMapper.selectById(order.getCarId());
            if (car != null) {
                dto.setCarName(car.getName());
                dto.setCarImage(car.getImage());
                dto.setCarBrand(getBrandName(car.getBrandId()));
                dto.setCarCategory(getCategoryName(car.getCategoryId()));
                dto.setCarColor(car.getColor());
                dto.setCarSeats(car.getSeats());
                dto.setCarTransmission(car.getTransmission());
                dto.setCarFuelType(car.getFuelType());
                dto.setCarPricePerDay(car.getPricePerDay());
                dto.setCarDeposit(car.getDeposit());
            }
        }

        // 获取取车门店信息
        if (order.getPickupStoreId() != null) {
            Store store = storeMapper.selectById(order.getPickupStoreId());
            if (store != null) {
                dto.setPickupStoreName(store.getName());
                dto.setPickupStoreAddress(store.getAddress());
                dto.setPickupStorePhone(store.getPhone());
            }
        }

        // 获取还车门店信息
        if (order.getReturnStoreId() != null) {
            Store store = storeMapper.selectById(order.getReturnStoreId());
            if (store != null) {
                dto.setReturnStoreName(store.getName());
                dto.setReturnStoreAddress(store.getAddress());
                dto.setReturnStorePhone(store.getPhone());
            }
        }

        // 费用明细（预约时）
        dto.setRentalFee(order.getRentalFee());
        dto.setDailyRate(order.getDailyRate());
        dto.setRentalDays(order.getRentalDays());
        dto.setInsuranceFee(order.getInsuranceFee());
        dto.setServiceFee(order.getServiceFee());
        dto.setOvertimeFee(order.getOvertimeFee());
        dto.setMileageFee(order.getMileageFee());
        dto.setDiscountAmount(order.getDiscountAmount());
        
        // 实际费用明细（结算时）
        dto.setActualRentalDays(order.getActualRentalDays());
        dto.setActualRentalFee(order.getActualRentalFee());
        dto.setActualInsuranceFee(order.getActualInsuranceFee());
        dto.setActualServiceFee(order.getActualServiceFee());
        dto.setActualOvertimeFee(order.getActualOvertimeFee());
        dto.setSettlementAmount(order.getSettlementAmount());
        
        // 获取状态日志
        dto.setStatusLogs(getOrderLogs(order.getId()));
        
        // 设置操作权限
        dto.setCanConfirmPickup(order.getStatus() == 1);
        dto.setCanConfirmReturn(order.getStatus() == 2);
        dto.setCanCancel(order.getStatus() == 0 || order.getStatus() == 1);
        dto.setCanModify(order.getStatus() == 0);

        return dto;
    }

    @Override
    public List<OrderFeeItemDTO> calculateOrderFees(Long orderId) {
        return new ArrayList<>();
    }

    @Override
    public List<OrderLogDTO> getOrderLogs(Long orderId) {
        LambdaQueryWrapper<OrderLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderLog::getOrderId, orderId);
        wrapper.orderByDesc(OrderLog::getCreateTime);
        List<OrderLog> logs = orderLogMapper.selectList(wrapper);
        
        return logs.stream().map(log -> {
            OrderLogDTO dto = new OrderLogDTO();
            dto.setId(log.getId());
            dto.setOrderId(log.getOrderId());
            dto.setOperatorId(log.getOperatorId());
            dto.setOperatorName(log.getOperatorName());
            dto.setFromStatus(log.getFromStatus());
            dto.setFromStatusText(getStatusText(log.getFromStatus()));
            dto.setToStatus(log.getToStatus());
            dto.setToStatusText(getStatusText(log.getToStatus()));
            dto.setRemark(log.getRemark());
            dto.setCreateTime(log.getCreateTime());
            return dto;
        }).collect(Collectors.toList());
    }

    private String generateOrderNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomStr = String.format("%04d", new Random().nextInt(10000));
        return "ORD" + dateStr + randomStr;
    }

    @Override
    @Transactional
    public void create(Order order) {
        order.setOrderNo(generateOrderNo());

        Car car = carMapper.selectById(order.getCarId());
        if (car == null) {
            throw new BusinessException("车辆不存在");
        }

        // 检查车辆状态
        if (car.getStatus() != 1) {
            throw new BusinessException("车辆当前状态不可租");
        }

        // 检查车辆在该时间段内是否可用
        if (!checkCarAvailability(order.getCarId(), order.getStartTime(), order.getEndTime())) {
            throw new BusinessException("车辆在该时间段内已被预订");
        }

        // 计算租期天数
        long days = ChronoUnit.DAYS.between(order.getStartTime().toLocalDate(), order.getEndTime().toLocalDate());
        if (days < 1) {
            days = 1;
        }
        
        // 计算租金
        double dailyRate = car.getPricePerDay();
        double rentalFee = dailyRate * days;
        double deposit = car.getDeposit();
        double totalPrice = rentalFee;

        order.setTotalPrice(totalPrice);
        order.setDeposit(deposit);
        order.setStatus(0);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        order.setDeleted(0);
        orderMapper.insert(order);
    }

    @Override
    @Transactional
    public void pay(Long id) {
        Order order = getById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("订单状态不正确，无法支付");
        }
        order.setStatus(1);
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        Order order = getById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        order.setStatus(5);
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void review(Long orderId, Long userId, String content) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权评价此订单");
        }
    }
    
    @Override
    @Transactional
    public void confirmPickup(Long orderId, Long operatorId, String operatorName, String remark) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("订单状态不正确，只有已确认的订单才能确认取车");
        }
        
        int fromStatus = order.getStatus();
        order.setStatus(2);
        order.setPickupTime(LocalDateTime.now());
        order.setPickupOperator(operatorName);
        order.setPickupRemark(remark);
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        
        // 记录日志
        OrderLog log = new OrderLog();
        log.setOrderId(orderId);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setFromStatus(fromStatus);
        log.setToStatus(2);
        log.setRemark(remark);
        log.setCreateTime(LocalDateTime.now());
        orderLogMapper.insert(log);
    }
    
    @Override
    @Transactional
    public void confirmReturn(Long orderId, Long operatorId, String operatorName, String remark) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 2) {
            throw new BusinessException("订单状态不正确，只有已取车的订单才能确认还车");
        }
        
        int fromStatus = order.getStatus();
        order.setStatus(3);
        order.setReturnTime(LocalDateTime.now());
        order.setReturnOperator(operatorName);
        order.setReturnRemark(remark);
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        
        // 记录日志
        OrderLog log = new OrderLog();
        log.setOrderId(orderId);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setFromStatus(fromStatus);
        log.setToStatus(3);
        log.setRemark(remark);
        log.setCreateTime(LocalDateTime.now());
        orderLogMapper.insert(log);
    }
    
    @Override
    @Transactional
    public void updateOrderStatusWithLog(Long orderId, Integer newStatus, Long operatorId, String operatorName, String remark) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        int fromStatus = order.getStatus();
        order.setStatus(newStatus);
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        
        // 记录日志
        OrderLog log = new OrderLog();
        log.setOrderId(orderId);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setFromStatus(fromStatus);
        log.setToStatus(newStatus);
        log.setRemark(remark);
        log.setCreateTime(LocalDateTime.now());
        orderLogMapper.insert(log);
    }
    
    @Override
    public Page<OrderDTO> getStoreOrders(Integer page, Integer size, Long storeId, Integer status) {
        Page<Order> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
            .eq(Order::getPickupStoreId, storeId)
            .eq(Order::getDeleted, 0);

        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }

        wrapper.orderByDesc(Order::getCreateTime);
        Page<Order> orderPage = orderMapper.selectPage(pageParam, wrapper);

        List<OrderDTO> dtoList = orderPage.getRecords().stream().map(this::convertToDTO).collect(Collectors.toList());

        Page<OrderDTO> resultPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        resultPage.setRecords(dtoList);
        return resultPage;
    }

    @Override
    public Page<OrderDTO> getAdminOrderList(OrderQueryDTO queryDTO) {
        Page<Order> pageParam = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getDeleted, 0);
        
        // 订单号模糊查询
        if (StringUtils.hasText(queryDTO.getOrderNo())) {
            wrapper.like(Order::getOrderNo, queryDTO.getOrderNo());
        }
        
        // 状态筛选
        if (queryDTO.getStatus() != null) {
            wrapper.eq(Order::getStatus, queryDTO.getStatus());
        }
        
        // 门店筛选
        if (queryDTO.getStoreId() != null) {
            wrapper.eq(Order::getPickupStoreId, queryDTO.getStoreId());
        }
        
        // 车辆筛选
        if (queryDTO.getCarId() != null) {
            wrapper.eq(Order::getCarId, queryDTO.getCarId());
        }
        
        // 取车时间范围
        if (queryDTO.getStartTimeBegin() != null) {
            wrapper.ge(Order::getStartTime, queryDTO.getStartTimeBegin());
        }
        if (queryDTO.getStartTimeEnd() != null) {
            wrapper.le(Order::getStartTime, queryDTO.getStartTimeEnd());
        }
        
        // 还车时间范围
        if (queryDTO.getEndTimeBegin() != null) {
            wrapper.ge(Order::getEndTime, queryDTO.getEndTimeBegin());
        }
        if (queryDTO.getEndTimeEnd() != null) {
            wrapper.le(Order::getEndTime, queryDTO.getEndTimeEnd());
        }
        
        // 创建时间范围
        if (queryDTO.getCreateTimeBegin() != null) {
            wrapper.ge(Order::getCreateTime, queryDTO.getCreateTimeBegin());
        }
        if (queryDTO.getCreateTimeEnd() != null) {
            wrapper.le(Order::getCreateTime, queryDTO.getCreateTimeEnd());
        }
        
        // 时间范围快捷筛选
        if (StringUtils.hasText(queryDTO.getTimeRange())) {
            LocalDateTime now = LocalDateTime.now();
            switch (queryDTO.getTimeRange()) {
                case "today":
                    wrapper.ge(Order::getCreateTime, now.toLocalDate().atStartOfDay());
                    wrapper.le(Order::getCreateTime, now);
                    break;
                case "week":
                    wrapper.ge(Order::getCreateTime, now.minusDays(7));
                    wrapper.le(Order::getCreateTime, now);
                    break;
                case "month":
                    wrapper.ge(Order::getCreateTime, now.minusDays(30));
                    wrapper.le(Order::getCreateTime, now);
                    break;
            }
        }
        
        // 排序
        if ("asc".equals(queryDTO.getSortOrder())) {
            wrapper.orderByAsc(Order::getCreateTime);
        } else {
            wrapper.orderByDesc(Order::getCreateTime);
        }
        
        Page<Order> orderPage = orderMapper.selectPage(pageParam, wrapper);
        List<OrderDTO> dtoList = orderPage.getRecords().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        
        // 用户名和手机号筛选（内存筛选）
        if (StringUtils.hasText(queryDTO.getUserName()) || StringUtils.hasText(queryDTO.getUserPhone())) {
            dtoList = dtoList.stream().filter(dto -> {
                boolean match = true;
                if (StringUtils.hasText(queryDTO.getUserName())) {
                    match = match && dto.getUserName() != null && 
                           dto.getUserName().contains(queryDTO.getUserName());
                }
                if (StringUtils.hasText(queryDTO.getUserPhone())) {
                    match = match && dto.getUserPhone() != null && 
                           dto.getUserPhone().contains(queryDTO.getUserPhone());
                }
                return match;
            }).collect(Collectors.toList());
        }
        
        // 品牌筛选（内存筛选）
        if (queryDTO.getBrandId() != null) {
            dtoList = dtoList.stream()
                .filter(dto -> {
                    if (dto.getCarId() == null) return false;
                    Car car = carMapper.selectById(dto.getCarId());
                    return car != null && queryDTO.getBrandId().equals(car.getBrandId());
                })
                .collect(Collectors.toList());
        }
        
        // 分类筛选（内存筛选）
        if (queryDTO.getCategoryId() != null) {
            dtoList = dtoList.stream()
                .filter(dto -> {
                    if (dto.getCarId() == null) return false;
                    Car car = carMapper.selectById(dto.getCarId());
                    return car != null && queryDTO.getCategoryId().equals(car.getCategoryId());
                })
                .collect(Collectors.toList());
        }
        
        Page<OrderDTO> resultPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        resultPage.setRecords(dtoList);
        return resultPage;
    }
    
    // ========== 订单状态管理流程优化 - 新增方法实现 ==========
    
    @Override
    @Transactional
    public void confirmOrder(Long orderId, Long operatorId, String operatorName, String remark) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("订单状态不正确，只有待确认的订单才能确认");
        }
        
        // 检查车辆可用性
        if (!checkCarAvailability(order.getCarId(), order.getStartTime(), order.getEndTime())) {
            throw new BusinessException("车辆在该时间段内不可用");
        }
        
        int fromStatus = order.getStatus();
        order.setStatus(1);
        order.setConfirmTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        
        // 更新车辆状态为已租
        Car car = carMapper.selectById(order.getCarId());
        if (car != null && car.getStatus() == 1) {
            car.setStatus(2);
            car.setUpdateTime(LocalDateTime.now());
            carMapper.updateById(car);
        }
        
        // 记录日志
        OrderLog log = new OrderLog();
        log.setOrderId(orderId);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setFromStatus(fromStatus);
        log.setToStatus(1);
        log.setRemark(remark);
        log.setCreateTime(LocalDateTime.now());
        orderLogMapper.insert(log);
    }
    
    @Override
    @Transactional
    public void rejectOrder(Long orderId, Long operatorId, String operatorName, String reasonCode, String remark) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("订单状态不正确，只有待确认的订单才能拒绝");
        }
        
        // 获取取消原因
        String cancelReason = remark;
        if (reasonCode != null && !reasonCode.isEmpty()) {
            CancelReason reason = cancelReasonMapper.selectOne(
                new LambdaQueryWrapper<CancelReason>().eq(CancelReason::getReasonCode, reasonCode)
            );
            if (reason != null) {
                cancelReason = reason.getReasonName() + (remark != null ? " - " + remark : "");
            }
        }
        
        int fromStatus = order.getStatus();
        order.setStatus(5);
        order.setCancelTime(LocalDateTime.now());
        order.setCancelBy(operatorName);
        order.setCancelReason(cancelReason);
        order.setCancelReasonCode(reasonCode);
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        
        // 记录日志
        OrderLog log = new OrderLog();
        log.setOrderId(orderId);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setFromStatus(fromStatus);
        log.setToStatus(5);
        log.setRemark("拒绝订单: " + cancelReason);
        log.setCreateTime(LocalDateTime.now());
        orderLogMapper.insert(log);
    }
    
    @Override
    public boolean checkCarAvailability(Long carId, LocalDateTime startTime, LocalDateTime endTime) {
        if (carId == null || startTime == null || endTime == null) {
            return false;
        }
        
        // 查询该车辆在该时间段内是否存在冲突订单
        // 改进：对于已还车的订单，使用实际还车时间(returnTime)判断
        // 对于未还车的订单，使用预约还车时间(endTime)判断
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getCarId, carId);
        wrapper.ne(Order::getStatus, 5); // 排除已取消订单
        wrapper.ne(Order::getStatus, 4); // 排除已完成订单
        
        // 时间冲突判断：
        // 订单开始时间 < 查询结束时间 AND (实际还车时间或预约还车时间) > 查询开始时间
        wrapper.lt(Order::getStartTime, endTime);
        
        // 使用自定义SQL判断还车时间：如果有实际还车时间用returnTime，否则用endTime
        wrapper.and(w -> w
            // 情况1：已还车（status=3），使用实际还车时间
            .and(w1 -> w1.eq(Order::getStatus, 3)
                         .gt(Order::getReturnTime, startTime))
            // 情况2：未还车（status=0,1,2），使用预约还车时间
            .or(w2 -> w2.in(Order::getStatus, 0, 1, 2)
                         .gt(Order::getEndTime, startTime))
        );
        
        wrapper.eq(Order::getDeleted, 0);
        
        Long count = orderMapper.selectCount(wrapper);
        return count == 0;
    }
    
    @Override
    public boolean checkUserQualification(Long userId) {
        if (userId == null) {
            return false;
        }
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        
        // 检查用户状态
        if (user.getStatus() != 1) {
            return false;
        }
        
        // 可以在这里添加更多资质检查，如驾驶证验证等
        return true;
    }
    
    @Override
    @Transactional
    public void submitCarChecks(Long orderId, List<OrderCarCheckDTO> checkItems, Long operatorId, String operatorName) {
        if (checkItems == null || checkItems.isEmpty()) {
            return;
        }
        
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        for (OrderCarCheckDTO item : checkItems) {
            OrderCarCheck check = new OrderCarCheck();
            check.setOrderId(orderId);
            check.setCheckType(item.getCheckType());
            check.setCheckItem(item.getCheckItem());
            check.setCheckResult(item.getCheckResult());
            check.setDescription(item.getDescription());
            check.setPhotos(item.getPhotos());
            check.setCheckedBy(operatorName);
            check.setCheckedTime(LocalDateTime.now());
            check.setRemark(item.getRemark());
            check.setCreateTime(LocalDateTime.now());
            check.setUpdateTime(LocalDateTime.now());
            orderCarCheckMapper.insert(check);
        }
        
        // 更新订单的车辆检查相关字段
        if (checkItems.get(0).getCheckType() == 1) {
            // 取车检查
            order.setMileageOut(getMileageFromChecks(checkItems));
            order.setFuelLevelOut(getFuelLevelFromChecks(checkItems));
        } else if (checkItems.get(0).getCheckType() == 2) {
            // 还车检查
            order.setMileageIn(getMileageFromChecks(checkItems));
            order.setFuelLevelIn(getFuelLevelFromChecks(checkItems));
            order.setDamageDescription(getDamageDescriptionFromChecks(checkItems));
            order.setDamagePhotos(getDamagePhotosFromChecks(checkItems));
        }
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }
    
    private Integer getMileageFromChecks(List<OrderCarCheckDTO> checks) {
        for (OrderCarCheckDTO check : checks) {
            if ("里程表".equals(check.getCheckItem()) || "里程".equals(check.getCheckItem())) {
                try {
                    return Integer.parseInt(check.getDescription());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }
    
    private Integer getFuelLevelFromChecks(List<OrderCarCheckDTO> checks) {
        for (OrderCarCheckDTO check : checks) {
            if ("油量".equals(check.getCheckItem()) || "燃油".equals(check.getCheckItem())) {
                try {
                    return Integer.parseInt(check.getDescription().replace("%", ""));
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }
    
    private String getDamageDescriptionFromChecks(List<OrderCarCheckDTO> checks) {
        StringBuilder sb = new StringBuilder();
        for (OrderCarCheckDTO check : checks) {
            if (check.getCheckResult() != null && check.getCheckResult() == 0) {
                sb.append(check.getCheckItem()).append(": ").append(check.getDescription()).append("; ");
            }
        }
        return sb.length() > 0 ? sb.toString() : null;
    }
    
    private String getDamagePhotosFromChecks(List<OrderCarCheckDTO> checks) {
        List<String> photos = new ArrayList<>();
        for (OrderCarCheckDTO check : checks) {
            if (check.getCheckResult() != null && check.getCheckResult() == 0 && check.getPhotos() != null) {
                photos.add(check.getPhotos());
            }
        }
        return photos.isEmpty() ? null : "[" + String.join(",", photos) + "]";
    }
    
    @Override
    public List<OrderCarCheckDTO> getCarChecks(Long orderId, Integer checkType) {
        LambdaQueryWrapper<OrderCarCheck> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderCarCheck::getOrderId, orderId);
        if (checkType != null) {
            wrapper.eq(OrderCarCheck::getCheckType, checkType);
        }
        wrapper.orderByAsc(OrderCarCheck::getCreateTime);
        
        List<OrderCarCheck> checks = orderCarCheckMapper.selectList(wrapper);
        return checks.stream().map(this::convertCarCheckToDTO).collect(Collectors.toList());
    }
    
    private OrderCarCheckDTO convertCarCheckToDTO(OrderCarCheck check) {
        OrderCarCheckDTO dto = new OrderCarCheckDTO();
        dto.setId(check.getId());
        dto.setOrderId(check.getOrderId());
        dto.setCheckType(check.getCheckType());
        dto.setCheckTypeText(check.getCheckType() == 1 ? "取车检查" : "还车检查");
        dto.setCheckItem(check.getCheckItem());
        dto.setCheckResult(check.getCheckResult());
        dto.setCheckResultText(check.getCheckResult() != null && check.getCheckResult() == 1 ? "正常" : "异常");
        dto.setDescription(check.getDescription());
        dto.setPhotos(check.getPhotos());
        dto.setCheckedBy(check.getCheckedBy());
        dto.setCheckedTime(check.getCheckedTime());
        dto.setRemark(check.getRemark());
        dto.setCreateTime(check.getCreateTime());
        dto.setUpdateTime(check.getUpdateTime());
        return dto;
    }
    
    @Override
    @Transactional
    public void reportCarIssue(Long orderId, String issueType, String description, String photos, Long userId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 2) {
            throw new BusinessException("订单状态不正确，只有已取车的订单才能上报问题");
        }
        
        // 记录问题到日志
        OrderLog log = new OrderLog();
        log.setOrderId(orderId);
        log.setOperatorId(userId);
        log.setOperatorName("用户");
        log.setFromStatus(order.getStatus());
        log.setToStatus(order.getStatus());
        log.setRemark("上报问题[" + issueType + "]: " + description);
        log.setCreateTime(LocalDateTime.now());
        orderLogMapper.insert(log);
    }
    
    @Override
    @Transactional
    public void settleOrder(Long orderId, Double settlementAmount, Long operatorId, String operatorName, String remark) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 3) {
            throw new BusinessException("订单状态不正确，只有已还车的订单才能结算");
        }
        
        // 计算结算金额（如果没有提供）
        if (settlementAmount == null) {
            settlementAmount = calculateSettlementAmount(order);
        }
        
        int fromStatus = order.getStatus();
        order.setStatus(4);
        order.setSettlementAmount(settlementAmount);
        order.setSettlementTime(LocalDateTime.now());
        order.setSettlementBy(operatorName);
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        
        // 更新车辆状态为可租
        Car car = carMapper.selectById(order.getCarId());
        if (car != null && car.getStatus() == 2) {
            car.setStatus(1);
            car.setUpdateTime(LocalDateTime.now());
            carMapper.updateById(car);
        }
        
        // 记录日志
        OrderLog log = new OrderLog();
        log.setOrderId(orderId);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setFromStatus(fromStatus);
        log.setToStatus(4);
        log.setRemark("结算金额: ¥" + settlementAmount + (remark != null ? " - " + remark : ""));
        log.setCreateTime(LocalDateTime.now());
        orderLogMapper.insert(log);
    }
    
    private Double calculateSettlementAmount(Order order) {
        double total = 0.0;
        
        // 计算实际使用天数（基于实际取车时间和实际还车时间）
        double actualDays = calculateActualRentalDays(order);
        double originalDays = order.getRentalDays() != null ? order.getRentalDays() : 1;
        double dailyRate = order.getDailyRate() != null ? order.getDailyRate() : 0;
        
        // 实际租金（按实际使用天数计算）
        double actualRentalFee = dailyRate * actualDays;
        total += actualRentalFee;
        
        // 保险费用（按实际天数重新计算）
        double originalInsuranceFee = order.getInsuranceFee() != null ? order.getInsuranceFee() : 0;
        double insuranceFeePerDay = originalDays > 0 ? originalInsuranceFee / originalDays : 0;
        double actualInsuranceFee = insuranceFeePerDay * actualDays;
        total += actualInsuranceFee;
        
        // 服务费用（按实际天数重新计算）
        double originalServiceFee = order.getServiceFee() != null ? order.getServiceFee() : 0;
        double serviceFeePerDay = originalDays > 0 ? originalServiceFee / originalDays : 0;
        double actualServiceFee = serviceFeePerDay * actualDays;
        total += actualServiceFee;
        
        // 超时费用（如果实际还车时间晚于预约还车时间）
        double overtimeFee = calculateOvertimeFee(order);
        total += overtimeFee;
        
        // 超里程费用
        if (order.getMileageFee() != null) {
            total += order.getMileageFee();
        }
        
        // 优惠折扣（按比例调整）
        if (order.getDiscountAmount() != null) {
            double discountRatio = actualDays / originalDays;
            total -= order.getDiscountAmount() * discountRatio;
        }
        
        // 更新订单的费用字段
        order.setActualRentalDays((int) Math.ceil(actualDays));
        order.setActualRentalFee(actualRentalFee);
        order.setActualInsuranceFee(actualInsuranceFee);
        order.setActualServiceFee(actualServiceFee);
        order.setActualOvertimeFee(overtimeFee);
        
        return Math.max(total, 0);
    }
    
    /**
     * 计算实际租车天数
     * 基于实际取车时间和实际还车时间计算
     */
    private double calculateActualRentalDays(Order order) {
        LocalDateTime pickupTime = order.getPickupTime();
        LocalDateTime returnTime = order.getReturnTime();
        
        if (pickupTime == null || returnTime == null) {
            // 如果没有实际时间，使用预约时间
            pickupTime = order.getStartTime();
            returnTime = order.getEndTime();
        }
        
        if (pickupTime == null || returnTime == null) {
            return 1; // 默认至少1天
        }
        
        // 计算小时数，不足24小时按1天算，超过按天数向上取整
        long hours = ChronoUnit.HOURS.between(pickupTime, returnTime);
        double days = hours / 24.0;
        return Math.max(1, Math.ceil(days));
    }
    
    /**
     * 计算超时费用
     * 当实际还车时间晚于预约还车时间时产生
     */
    private double calculateOvertimeFee(Order order) {
        LocalDateTime scheduledEndTime = order.getEndTime();
        LocalDateTime actualReturnTime = order.getReturnTime();
        double dailyRate = order.getDailyRate() != null ? order.getDailyRate() : 0;
        
        if (scheduledEndTime == null || actualReturnTime == null) {
            return 0;
        }
        
        // 如果实际还车时间晚于预约时间
        if (actualReturnTime.isAfter(scheduledEndTime)) {
            long overtimeHours = ChronoUnit.HOURS.between(scheduledEndTime, actualReturnTime);
            
            // 超时费用计算：
            // 1-4小时：按小时收费（日租金/24 * 1.5倍）
            // 5-24小时：按半天收费（日租金 * 0.6）
            // 超过24小时：按天收费
            if (overtimeHours <= 4) {
                return (dailyRate / 24) * overtimeHours * 1.5;
            } else if (overtimeHours <= 24) {
                return dailyRate * 0.6;
            } else {
                double overtimeDays = Math.ceil(overtimeHours / 24.0);
                return dailyRate * overtimeDays;
            }
        }
        
        return 0;
    }
    
    @Override
    @Transactional
    public void cancelOrderWithReason(Long orderId, String reasonCode, String reasonDetail, Long operatorId, String operatorName) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        // 只有待确认和已确认的订单可以取消
        if (order.getStatus() != 0 && order.getStatus() != 1) {
            throw new BusinessException("订单状态不正确，只有待确认或已确认的订单才能取消");
        }
        
        // 获取取消原因
        String cancelReason = reasonDetail;
        if (reasonCode != null && !reasonCode.isEmpty()) {
            CancelReason reason = cancelReasonMapper.selectOne(
                new LambdaQueryWrapper<CancelReason>().eq(CancelReason::getReasonCode, reasonCode)
            );
            if (reason != null) {
                cancelReason = reason.getReasonName() + (reasonDetail != null ? " - " + reasonDetail : "");
            }
        }
        
        int fromStatus = order.getStatus();
        order.setStatus(5);
        order.setCancelTime(LocalDateTime.now());
        order.setCancelBy(operatorName);
        order.setCancelReason(cancelReason);
        order.setCancelReasonCode(reasonCode);
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        
        // 如果订单已确认，恢复车辆状态为可租
        if (fromStatus == 1) {
            Car car = carMapper.selectById(order.getCarId());
            if (car != null && car.getStatus() == 2) {
                car.setStatus(1);
                car.setUpdateTime(LocalDateTime.now());
                carMapper.updateById(car);
            }
        }
        
        // 记录日志
        OrderLog log = new OrderLog();
        log.setOrderId(orderId);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setFromStatus(fromStatus);
        log.setToStatus(5);
        log.setRemark("取消订单: " + cancelReason);
        log.setCreateTime(LocalDateTime.now());
        orderLogMapper.insert(log);
    }
    
    @Override
    public List<CancelReasonDTO> getCancelReasons(Integer reasonType) {
        LambdaQueryWrapper<CancelReason> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CancelReason::getIsActive, 1);
        if (reasonType != null) {
            wrapper.eq(CancelReason::getReasonType, reasonType);
        }
        wrapper.orderByAsc(CancelReason::getSortOrder);
        
        List<CancelReason> reasons = cancelReasonMapper.selectList(wrapper);
        return reasons.stream().map(this::convertCancelReasonToDTO).collect(Collectors.toList());
    }
    
    private CancelReasonDTO convertCancelReasonToDTO(CancelReason reason) {
        CancelReasonDTO dto = new CancelReasonDTO();
        dto.setId(reason.getId());
        dto.setReasonCode(reason.getReasonCode());
        dto.setReasonName(reason.getReasonName());
        dto.setReasonType(reason.getReasonType());
        dto.setReasonTypeText(getCancelReasonTypeText(reason.getReasonType()));
        dto.setSortOrder(reason.getSortOrder());
        dto.setIsActive(reason.getIsActive());
        dto.setCreateTime(reason.getCreateTime());
        dto.setUpdateTime(reason.getUpdateTime());
        return dto;
    }
    
    private String getCancelReasonTypeText(Integer type) {
        switch (type) {
            case 1: return "用户取消";
            case 2: return "系统取消";
            case 3: return "管理员取消";
            default: return "其他";
        }
    }
    
    @Override
    public Map<String, Object> getOrderStatistics(LocalDateTime startTime, LocalDateTime endTime, Long storeId) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 订单总数
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getDeleted, 0);
        if (startTime != null) {
            wrapper.ge(Order::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(Order::getCreateTime, endTime);
        }
        if (storeId != null) {
            wrapper.eq(Order::getPickupStoreId, storeId);
        }
        long totalOrders = orderMapper.selectCount(wrapper);
        statistics.put("totalOrders", totalOrders);
        
        // 各状态订单数
        for (int status = 0; status <= 5; status++) {
            LambdaQueryWrapper<Order> statusWrapper = new LambdaQueryWrapper<>();
            statusWrapper.eq(Order::getDeleted, 0);
            statusWrapper.eq(Order::getStatus, status);
            if (startTime != null) {
                statusWrapper.ge(Order::getCreateTime, startTime);
            }
            if (endTime != null) {
                statusWrapper.le(Order::getCreateTime, endTime);
            }
            if (storeId != null) {
                statusWrapper.eq(Order::getPickupStoreId, storeId);
            }
            long count = orderMapper.selectCount(statusWrapper);
            statistics.put("status" + status + "Count", count);
        }
        
        // 总营收
        LambdaQueryWrapper<Order> revenueWrapper = new LambdaQueryWrapper<>();
        revenueWrapper.eq(Order::getDeleted, 0);
        revenueWrapper.eq(Order::getStatus, 4); // 已完成订单
        if (startTime != null) {
            revenueWrapper.ge(Order::getSettlementTime, startTime);
        }
        if (endTime != null) {
            revenueWrapper.le(Order::getSettlementTime, endTime);
        }
        if (storeId != null) {
            revenueWrapper.eq(Order::getPickupStoreId, storeId);
        }
        List<Order> completedOrders = orderMapper.selectList(revenueWrapper);
        double totalRevenue = completedOrders.stream()
            .mapToDouble(o -> o.getSettlementAmount() != null ? o.getSettlementAmount() : 0)
            .sum();
        statistics.put("totalRevenue", totalRevenue);
        
        return statistics;
    }
    
    @Override
    public Map<String, Object> getCancelStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 取消订单总数
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getDeleted, 0);
        wrapper.eq(Order::getStatus, 5);
        if (startTime != null) {
            wrapper.ge(Order::getCancelTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(Order::getCancelTime, endTime);
        }
        long totalCanceled = orderMapper.selectCount(wrapper);
        statistics.put("totalCanceled", totalCanceled);
        
        // 按取消原因统计
        LambdaQueryWrapper<Order> reasonWrapper = new LambdaQueryWrapper<>();
        reasonWrapper.eq(Order::getDeleted, 0);
        reasonWrapper.eq(Order::getStatus, 5);
        reasonWrapper.isNotNull(Order::getCancelReasonCode);
        if (startTime != null) {
            reasonWrapper.ge(Order::getCancelTime, startTime);
        }
        List<Order> canceledOrders = orderMapper.selectList(reasonWrapper);
        
        Map<String, Long> reasonStats = canceledOrders.stream()
            .collect(Collectors.groupingBy(
                o -> o.getCancelReasonCode() != null ? o.getCancelReasonCode() : "UNKNOWN",
                Collectors.counting()
            ));
        statistics.put("reasonStatistics", reasonStats);
        
        // 按取消人类型统计
        Map<String, Long> cancelByStats = canceledOrders.stream()
            .collect(Collectors.groupingBy(
                o -> o.getCancelBy() != null ? o.getCancelBy() : "UNKNOWN",
                Collectors.counting()
            ));
        statistics.put("cancelByStatistics", cancelByStats);
        
        return statistics;
    }
}
