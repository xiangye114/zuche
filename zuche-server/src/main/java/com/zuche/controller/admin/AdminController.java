package com.zuche.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zuche.common.Result;
import com.zuche.common.PageResult;
import com.zuche.common.UserContext;
import com.zuche.common.UserRole;
import com.zuche.entity.Car;
import com.zuche.entity.Order;
import com.zuche.entity.Review;
import com.zuche.entity.User;
import com.zuche.entity.Brand;
import com.zuche.entity.Category;
import com.zuche.entity.Store;
import com.zuche.entity.KnowledgeBase;
import com.zuche.mapper.CarMapper;
import com.zuche.mapper.OrderMapper;
import com.zuche.mapper.ReviewMapper;
import com.zuche.mapper.UserMapper;
import com.zuche.mapper.BrandMapper;
import com.zuche.mapper.CategoryMapper;
import com.zuche.mapper.StoreMapper;
import com.zuche.mapper.KnowledgeBaseMapper;
import com.zuche.utils.ExcelExportUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {
    
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private CarMapper carMapper;
    
    @Resource
    private OrderMapper orderMapper;
    
    @Resource
    private ReviewMapper reviewMapper;
    
    @Resource
    private BrandMapper brandMapper;
    
    @Resource
    private CategoryMapper categoryMapper;
    
    @Resource
    private StoreMapper storeMapper;
    
    @Resource
    private KnowledgeBaseMapper knowledgeBaseMapper;
    
    /**
     * 检查是否为超级管理员
     */
    private void checkSuperAdmin() {
        if (!UserContext.isSuperAdmin()) {
            throw new RuntimeException("无权限执行此操作");
        }
    }
    
    /**
     * 检查是否为管理员（超级管理员或门店管理员）
     */
    private void checkAdmin() {
        if (!UserContext.isAdmin()) {
            throw new RuntimeException("无权限执行此操作");
        }
    }
    
    @GetMapping("/dashboard")
    public Result<?> getDashboard() {
        // 检查用户是否登录
        if (UserContext.getCurrentUser() == null) {
            return Result.error("请先登录");
        }
        
        UserRole currentRole = UserContext.getCurrentUserRole();
        Long storeId = null;
        
        // 门店管理员只能查看本门店数据
        if (currentRole != null && currentRole.isStoreAdmin()) {
            // 这里需要获取当前门店管理员的门店ID
            // 简化处理，实际应该从用户-门店关联表中获取
            storeId = getCurrentStoreId();
        }
        
        Map<String, Object> data = new HashMap<>();
        
        // 用户统计
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getDeleted, 0);
        data.put("totalUsers", userMapper.selectCount(userWrapper));
        
        // 车辆统计
        LambdaQueryWrapper<Car> carWrapper = new LambdaQueryWrapper<>();
        carWrapper.eq(Car::getDeleted, 0);
        if (storeId != null) {
            carWrapper.eq(Car::getStoreId, storeId);
        }
        data.put("totalCars", carMapper.selectCount(carWrapper));
        
        // 订单统计
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getDeleted, 0);
        if (storeId != null) {
            // 订单需要根据车辆所属门店来筛选
            // 简化处理，实际应该关联订单和门店
        }
        data.put("totalOrders", orderMapper.selectCount(orderWrapper));
        
        // 计算总收入
        LambdaQueryWrapper<Order> revenueWrapper = new LambdaQueryWrapper<>();
        revenueWrapper.eq(Order::getDeleted, 0)
                     .eq(Order::getStatus, 3);
        List<Order> completedOrders = orderMapper.selectList(revenueWrapper);
        double totalRevenue = completedOrders.stream()
                .mapToDouble(o -> o.getTotalPrice() != null ? o.getTotalPrice() : 0)
                .sum();
        data.put("totalRevenue", totalRevenue);
        
        // 车辆状态统计
        LambdaQueryWrapper<Car> availableWrapper = new LambdaQueryWrapper<>();
        availableWrapper.eq(Car::getDeleted, 0)
                        .eq(Car::getStatus, 1);
        if (storeId != null) {
            availableWrapper.eq(Car::getStoreId, storeId);
        }
        data.put("availableCars", carMapper.selectCount(availableWrapper));
        
        LambdaQueryWrapper<Car> rentedWrapper = new LambdaQueryWrapper<>();
        rentedWrapper.eq(Car::getDeleted, 0)
                     .eq(Car::getStatus, 2);
        if (storeId != null) {
            rentedWrapper.eq(Car::getStoreId, storeId);
        }
        data.put("rentedCars", carMapper.selectCount(rentedWrapper));
        
        LambdaQueryWrapper<Car> repairWrapper = new LambdaQueryWrapper<>();
        repairWrapper.eq(Car::getDeleted, 0)
                     .eq(Car::getStatus, 0);
        if (storeId != null) {
            repairWrapper.eq(Car::getStoreId, storeId);
        }
        data.put("repairCars", carMapper.selectCount(repairWrapper));
        
        return Result.success(data);
    }
    
    /**
     * 获取当前用户的门店ID
     */
    private Long getCurrentStoreId() {
        // 简化处理，实际应该从用户-门店关联表中获取
        // 这里返回null表示查看全部数据
        return null;
    }
    
    @GetMapping("/users")
    public Result<?> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            String keyword,
            Integer status) {
        checkAdmin();
        
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getDeleted, 0);
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                            .or().like(User::getPhone, keyword));
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> pageResult = userMapper.selectPage(pageParam, wrapper);
        return Result.success(PageResult.of(pageResult.getRecords(), pageResult.getTotal(), (long) page, (long) size));
    }
    
    @GetMapping("/users/{id}")
    public Result<?> getUserDetail(@PathVariable Long id) {
        checkAdmin();
        
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getUserId, id)
                   .eq(Order::getDeleted, 0)
                   .orderByDesc(Order::getCreateTime);
        data.put("orders", orderMapper.selectList(orderWrapper));
        
        return Result.success(data);
    }
    
    @PutMapping("/users/{id}/status")
    public Result<?> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        checkAdmin();
        
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setStatus(params.get("status"));
        userMapper.updateById(user);
        return Result.success("更新成功");
    }
    
    @PostMapping("/users/batch/status")
    public Result<?> batchUpdateUserStatus(@RequestBody Map<String, Object> params) {
        checkAdmin();
        
        List<Long> ids = (List<Long>) params.get("ids");
        Integer status = (Integer) params.get("status");
        
        if (ids == null || ids.isEmpty() || status == null) {
            return Result.error("参数错误");
        }
        
        for (Long id : ids) {
            User user = userMapper.selectById(id);
            if (user != null) {
                user.setStatus(status);
                userMapper.updateById(user);
            }
        }
        
        return Result.success("批量更新成功");
    }
    
    @PostMapping("/users/batch/delete")
    public Result<?> batchDeleteUsers(@RequestBody Map<String, Object> params) {
        checkSuperAdmin(); // 只有超级管理员可以批量删除
        
        List<Long> ids = (List<Long>) params.get("ids");
        
        if (ids == null || ids.isEmpty()) {
            return Result.error("参数错误");
        }
        
        for (Long id : ids) {
            User user = userMapper.selectById(id);
            if (user != null) {
                user.setDeleted(1);
                userMapper.updateById(user);
            }
        }
        
        return Result.success("批量删除成功");
    }
    
    @PostMapping("/users")
    public Result<?> addUser(@RequestBody User user) {
        checkSuperAdmin();
        
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return Result.error("用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            return Result.error("密码不能为空");
        }
        
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername().trim())
               .eq(User::getDeleted, 0);
        if (userMapper.selectCount(wrapper) > 0) {
            return Result.error("用户名已存在");
        }
        
        user.setUsername(user.getUsername().trim());
        user.setPassword(user.getPassword());
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setDeleted(0);
        userMapper.insert(user);
        
        return Result.success("创建成功");
    }
    
    @PutMapping("/users/{id}")
    public Result<?> updateUser(@PathVariable Long id, @RequestBody User userData) {
        checkSuperAdmin();
        
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        if (userData.getUsername() != null && !userData.getUsername().trim().isEmpty()) {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUsername, userData.getUsername().trim())
                   .ne(User::getId, id)
                   .eq(User::getDeleted, 0);
            if (userMapper.selectCount(wrapper) > 0) {
                return Result.error("用户名已存在");
            }
            user.setUsername(userData.getUsername().trim());
        }
        
        if (userData.getPassword() != null && !userData.getPassword().trim().isEmpty()) {
            user.setPassword(userData.getPassword());
        }
        
        if (userData.getPhone() != null) {
            user.setPhone(userData.getPhone());
        }
        
        if (userData.getEmail() != null) {
            user.setEmail(userData.getEmail());
        }
        
        if (userData.getRole() != null) {
            user.setRole(userData.getRole());
        }
        
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        
        return Result.success("更新成功");
    }
    
    @GetMapping("/users/export")
    public void exportUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            HttpServletResponse response) throws IOException {
        checkAdmin();
        
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getDeleted, 0);
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                            .or().like(User::getPhone, keyword));
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        
        List<User> users = userMapper.selectList(wrapper);
        ExcelExportUtil.exportUsers(users, response);
    }
    
    @GetMapping("/reviews")
    public Result<?> getReviewList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            Long carId,
            Integer rating) {
        checkAdmin();
        
        Page<Review> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getDeleted, 0);
        
        if (carId != null) {
            wrapper.eq(Review::getCarId, carId);
        }
        if (rating != null) {
            wrapper.eq(Review::getRating, rating);
        }
        
        wrapper.orderByDesc(Review::getCreateTime);
        Page<Review> pageResult = reviewMapper.selectPage(pageParam, wrapper);
        return Result.success(PageResult.of(pageResult.getRecords(), pageResult.getTotal(), (long) page, (long) size));
    }
    
    @DeleteMapping("/reviews/{id}")
    public Result<?> deleteReview(@PathVariable Long id) {
        checkSuperAdmin(); // 只有超级管理员可以删除
        
        Review review = reviewMapper.selectById(id);
        if (review == null) {
            return Result.error("评价不存在");
        }
        review.setDeleted(1);
        reviewMapper.updateById(review);
        return Result.success("删除成功");
    }
    
    // ==================== 车辆管理 ====================
    
    @GetMapping("/cars")
    public Result<?> getCarList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            String keyword,
            Long storeId,
            Long brandId,
            Integer status) {
        checkAdmin();
        
        Page<Car> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Car> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Car::getDeleted, 0);
        
        // 门店管理员只能查看本门店车辆
        if (UserContext.isStoreAdmin()) {
            Long currentStoreId = getCurrentStoreId();
            if (currentStoreId != null) {
                wrapper.eq(Car::getStoreId, currentStoreId);
            }
        } else if (storeId != null) {
            wrapper.eq(Car::getStoreId, storeId);
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Car::getName, keyword);
        }
        if (brandId != null) {
            wrapper.eq(Car::getBrandId, brandId);
        }
        if (status != null) {
            wrapper.eq(Car::getStatus, status);
        }
        
        wrapper.orderByDesc(Car::getCreateTime);
        Page<Car> pageResult = carMapper.selectPage(pageParam, wrapper);
        
        return Result.success(PageResult.of(pageResult.getRecords(), pageResult.getTotal(), (long) page, (long) size));
    }
    
    @PostMapping("/cars/sync-status")
    public Result<?> syncCarStatus() {
        checkAdmin();
        
        int updatedToRented = 0;
        int updatedToAvailable = 0;
        LocalDateTime now = LocalDateTime.now();
        
        List<Car> allCars = carMapper.selectList(
            new LambdaQueryWrapper<Car>().eq(Car::getDeleted, 0)
        );
        
        for (Car car : allCars) {
            LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
            orderWrapper.eq(Order::getCarId, car.getId())
                       .eq(Order::getDeleted, 0)
                       .in(Order::getStatus, 0, 1, 2, 3)
                       .orderByDesc(Order::getCreateTime)
                       .last("LIMIT 1");
            Order activeOrder = orderMapper.selectOne(orderWrapper);
            
            boolean hasActiveOrder = activeOrder != null;
            
            if (hasActiveOrder && car.getStatus() != 2) {
                car.setStatus(2);
                car.setUpdateTime(now);
                carMapper.updateById(car);
                updatedToRented++;
            } else if (!hasActiveOrder && car.getStatus() == 2) {
                car.setStatus(1);
                car.setUpdateTime(now);
                carMapper.updateById(car);
                updatedToAvailable++;
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("updatedToRented", updatedToRented);
        result.put("updatedToAvailable", updatedToAvailable);
        result.put("totalCars", allCars.size());
        
        return Result.success(result);
    }
    
    @PostMapping("/cars")
    public Result<?> addCar(@RequestBody Car car) {
        checkAdmin();
        
        // 门店管理员只能添加本门店车辆
        if (UserContext.isStoreAdmin()) {
            Long currentStoreId = getCurrentStoreId();
            if (currentStoreId != null) {
                car.setStoreId(currentStoreId);
            }
        }
        
        car.setCreateTime(LocalDateTime.now());
        car.setUpdateTime(LocalDateTime.now());
        car.setDeleted(0);
        carMapper.insert(car);
        return Result.success("添加成功");
    }
    
    @PutMapping("/cars/{id}")
    public Result<?> updateCar(@PathVariable Long id, @RequestBody Car car) {
        checkAdmin();
        
        Car existing = carMapper.selectById(id);
        if (existing == null) {
            return Result.error("车辆不存在");
        }
        
        // 门店管理员只能修改本门店车辆
        if (UserContext.isStoreAdmin()) {
            Long currentStoreId = getCurrentStoreId();
            if (currentStoreId != null && !currentStoreId.equals(existing.getStoreId())) {
                return Result.error("无权限修改其他门店车辆");
            }
        }
        
        car.setId(id);
        car.setUpdateTime(LocalDateTime.now());
        carMapper.updateById(car);
        return Result.success("更新成功");
    }
    
    @DeleteMapping("/cars/{id}")
    public Result<?> deleteCar(@PathVariable Long id) {
        checkSuperAdmin(); // 只有超级管理员可以删除
        
        Car car = carMapper.selectById(id);
        if (car == null) {
            return Result.error("车辆不存在");
        }
        car.setDeleted(1);
        car.setUpdateTime(LocalDateTime.now());
        carMapper.updateById(car);
        return Result.success("删除成功");
    }
    
    @PostMapping("/cars/batch/status")
    public Result<?> batchUpdateCarStatus(@RequestBody Map<String, Object> params) {
        checkAdmin();
        
        List<Long> ids = (List<Long>) params.get("ids");
        Integer status = (Integer) params.get("status");
        
        if (ids == null || ids.isEmpty() || status == null) {
            return Result.error("参数错误");
        }
        
        for (Long id : ids) {
            Car car = carMapper.selectById(id);
            if (car != null) {
                // 门店管理员只能修改本门店车辆
                if (UserContext.isStoreAdmin()) {
                    Long currentStoreId = getCurrentStoreId();
                    if (currentStoreId != null && !currentStoreId.equals(car.getStoreId())) {
                        continue; // 跳过非本门店车辆
                    }
                }
                car.setStatus(status);
                car.setUpdateTime(LocalDateTime.now());
                carMapper.updateById(car);
            }
        }
        
        return Result.success("批量更新成功");
    }
    
    @PostMapping("/cars/batch/delete")
    public Result<?> batchDeleteCars(@RequestBody Map<String, Object> params) {
        checkSuperAdmin(); // 只有超级管理员可以批量删除
        
        List<Long> ids = (List<Long>) params.get("ids");
        
        if (ids == null || ids.isEmpty()) {
            return Result.error("参数错误");
        }
        
        for (Long id : ids) {
            Car car = carMapper.selectById(id);
            if (car != null) {
                car.setDeleted(1);
                car.setUpdateTime(LocalDateTime.now());
                carMapper.updateById(car);
            }
        }
        
        return Result.success("批量删除成功");
    }
    
    @GetMapping("/cars/export")
    public void exportCars(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Integer status,
            HttpServletResponse response) throws IOException {
        checkAdmin();
        
        LambdaQueryWrapper<Car> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Car::getDeleted, 0);
        
        // 门店管理员只能导出本门店车辆
        if (UserContext.isStoreAdmin()) {
            Long currentStoreId = getCurrentStoreId();
            if (currentStoreId != null) {
                wrapper.eq(Car::getStoreId, currentStoreId);
            }
        } else if (storeId != null) {
            wrapper.eq(Car::getStoreId, storeId);
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Car::getName, keyword);
        }
        if (brandId != null) {
            wrapper.eq(Car::getBrandId, brandId);
        }
        if (status != null) {
            wrapper.eq(Car::getStatus, status);
        }
        
        List<Car> cars = carMapper.selectList(wrapper);
        ExcelExportUtil.exportCars(cars, response);
    }
    
    // ==================== 品牌管理 ====================
    
    @GetMapping("/brands")
    public Result<?> getBrandList() {
        LambdaQueryWrapper<Brand> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Brand::getDeleted, 0);
        List<Brand> list = brandMapper.selectList(wrapper);
        return Result.success(list);
    }
    
    @PostMapping("/brands")
    public Result<?> addBrand(@RequestBody Brand brand) {
        checkSuperAdmin(); // 只有超级管理员可以添加
        
        brand.setDeleted(0);
        brandMapper.insert(brand);
        return Result.success("添加成功");
    }
    
    @PutMapping("/brands/{id}")
    public Result<?> updateBrand(@PathVariable Long id, @RequestBody Brand brand) {
        checkSuperAdmin(); // 只有超级管理员可以修改
        
        Brand existing = brandMapper.selectById(id);
        if (existing == null) {
            return Result.error("品牌不存在");
        }
        brand.setId(id);
        brandMapper.updateById(brand);
        return Result.success("更新成功");
    }
    
    @DeleteMapping("/brands/{id}")
    public Result<?> deleteBrand(@PathVariable Long id) {
        checkSuperAdmin(); // 只有超级管理员可以删除
        
        Brand brand = brandMapper.selectById(id);
        if (brand == null) {
            return Result.error("品牌不存在");
        }
        brand.setDeleted(1);
        brandMapper.updateById(brand);
        return Result.success("删除成功");
    }
    
    // ==================== 分类管理 ====================
    
    @GetMapping("/categories")
    public Result<?> getCategoryList() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getDeleted, 0);
        List<Category> list = categoryMapper.selectList(wrapper);
        return Result.success(list);
    }
    
    @PostMapping("/categories")
    public Result<?> addCategory(@RequestBody Category category) {
        checkSuperAdmin(); // 只有超级管理员可以添加
        
        category.setDeleted(0);
        categoryMapper.insert(category);
        return Result.success("添加成功");
    }
    
    @PutMapping("/categories/{id}")
    public Result<?> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        checkSuperAdmin(); // 只有超级管理员可以修改
        
        Category existing = categoryMapper.selectById(id);
        if (existing == null) {
            return Result.error("分类不存在");
        }
        category.setId(id);
        categoryMapper.updateById(category);
        return Result.success("更新成功");
    }
    
    @DeleteMapping("/categories/{id}")
    public Result<?> deleteCategory(@PathVariable Long id) {
        checkSuperAdmin(); // 只有超级管理员可以删除
        
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            return Result.error("分类不存在");
        }
        category.setDeleted(1);
        categoryMapper.updateById(category);
        return Result.success("删除成功");
    }
    
    // ==================== 门店管理 ====================
    
    @GetMapping("/stores")
    public Result<?> getStoreList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            String keyword) {
        checkAdmin();
        
        Page<Store> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Store> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Store::getDeleted, 0);
        
        // 门店管理员只能查看自己门店
        if (UserContext.isStoreAdmin()) {
            Long currentStoreId = getCurrentStoreId();
            if (currentStoreId != null) {
                wrapper.eq(Store::getId, currentStoreId);
            }
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Store::getName, keyword);
        }
        
        wrapper.orderByDesc(Store::getCreateTime);
        Page<Store> pageResult = storeMapper.selectPage(pageParam, wrapper);
        return Result.success(PageResult.of(pageResult.getRecords(), pageResult.getTotal(), (long) page, (long) size));
    }
    
    @PostMapping("/stores")
    public Result<?> addStore(@RequestBody Store store) {
        checkSuperAdmin(); // 只有超级管理员可以添加门店
        
        store.setCreateTime(LocalDateTime.now());
        store.setDeleted(0);
        storeMapper.insert(store);
        return Result.success("添加成功");
    }
    
    @PutMapping("/stores/{id}")
    public Result<?> updateStore(@PathVariable Long id, @RequestBody Store store) {
        checkSuperAdmin(); // 只有超级管理员可以修改门店
        
        Store existing = storeMapper.selectById(id);
        if (existing == null) {
            return Result.error("门店不存在");
        }
        store.setId(id);
        storeMapper.updateById(store);
        return Result.success("更新成功");
    }
    
    @DeleteMapping("/stores/{id}")
    public Result<?> deleteStore(@PathVariable Long id) {
        checkSuperAdmin(); // 只有超级管理员可以删除门店
        
        Store store = storeMapper.selectById(id);
        if (store == null) {
            return Result.error("门店不存在");
        }
        store.setDeleted(1);
        storeMapper.updateById(store);
        return Result.success("删除成功");
    }
    
    // ==================== 知识库管理 ====================
    
    @GetMapping("/knowledge")
    public Result<?> getKnowledgeList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            String keyword,
            String category) {
        checkAdmin();
        
        Page<KnowledgeBase> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(KnowledgeBase::getQuestion, keyword)
                            .or().like(KnowledgeBase::getAnswer, keyword));
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq(KnowledgeBase::getCategory, category);
        }
        
        wrapper.orderByDesc(KnowledgeBase::getPriority);
        wrapper.orderByDesc(KnowledgeBase::getCreateTime);
        Page<KnowledgeBase> pageResult = knowledgeBaseMapper.selectPage(pageParam, wrapper);
        return Result.success(PageResult.of(pageResult.getRecords(), pageResult.getTotal(), (long) page, (long) size));
    }
    
    @PostMapping("/knowledge")
    public Result<?> addKnowledge(@RequestBody KnowledgeBase knowledge) {
        checkAdmin();
        
        knowledge.setCreateTime(LocalDateTime.now());
        if (knowledge.getStatus() == null) {
            knowledge.setStatus(1);
        }
        if (knowledge.getPriority() == null) {
            knowledge.setPriority(0);
        }
        knowledgeBaseMapper.insert(knowledge);
        return Result.success("添加成功");
    }
    
    @PutMapping("/knowledge/{id}")
    public Result<?> updateKnowledge(@PathVariable Long id, @RequestBody KnowledgeBase knowledge) {
        checkAdmin();
        
        KnowledgeBase existing = knowledgeBaseMapper.selectById(id);
        if (existing == null) {
            return Result.error("知识条目不存在");
        }
        knowledge.setId(id);
        knowledgeBaseMapper.updateById(knowledge);
        return Result.success("更新成功");
    }
    
    @DeleteMapping("/knowledge/{id}")
    public Result<?> deleteKnowledge(@PathVariable Long id) {
        checkAdmin();
        
        KnowledgeBase knowledge = knowledgeBaseMapper.selectById(id);
        if (knowledge == null) {
            return Result.error("知识条目不存在");
        }
        knowledgeBaseMapper.deleteById(id);
        return Result.success("删除成功");
    }
}
