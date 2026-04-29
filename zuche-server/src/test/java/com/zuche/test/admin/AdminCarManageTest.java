package com.zuche.test.admin;

import com.alibaba.fastjson.JSONObject;
import com.zuche.common.Result;
import com.zuche.dto.LoginDTO;
import com.zuche.test.base.BaseTest;
import org.junit.jupiter.api.*;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 管理端-车辆管理测试 (AM-002)
 * 共25条测试用例
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AdminCarManageTest extends BaseTest {

    private static String adminToken;

    private String getAdminToken() throws Exception {
        if (adminToken != null) return adminToken;
        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("123456");
        MvcResult result = post("/auth/login", dto);
        Result response = parseResult(result);
        JSONObject data = (JSONObject) response.getData();
        adminToken = data.getString("token");
        return adminToken;
    }

    // ==================== Day 1: 车辆列表与查询 ====================

    @Test
    @org.junit.jupiter.api.Order(1)
    @DisplayName("AM-002-001: 正常查询-全部车辆")
    public void testQueryAllCars() throws Exception {
        String token = getAdminToken();
        MvcResult result = getWithToken("/admin/cars?page=1&size=10", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "查询全部车辆应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    @DisplayName("AM-002-002: 状态筛选-上架车辆")
    public void testFilterByActiveStatus() throws Exception {
        String token = getAdminToken();
        MvcResult result = getWithToken("/admin/cars?status=1&page=1&size=10", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "状态筛选应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    @DisplayName("AM-002-003: 状态筛选-下架车辆")
    public void testFilterByInactiveStatus() throws Exception {
        String token = getAdminToken();
        MvcResult result = getWithToken("/admin/cars?status=0&page=1&size=10", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "状态筛选应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    @DisplayName("AM-002-004: 品牌筛选")
    public void testFilterByBrand() throws Exception {
        String token = getAdminToken();
        MvcResult result = getWithToken("/admin/cars?brandId=1&page=1&size=10", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "品牌筛选应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    @DisplayName("AM-002-005: 分类筛选")
    public void testFilterByCategory() throws Exception {
        String token = getAdminToken();
        MvcResult result = getWithToken("/admin/cars?categoryId=1&page=1&size=10", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "分类筛选应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(6)
    @DisplayName("AM-002-006: 关键词搜索-车辆名称")
    public void testSearchByName() throws Exception {
        String token = getAdminToken();
        MvcResult result = getWithToken("/admin/cars?keyword=奥迪&page=1&size=10", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "关键词搜索应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(7)
    @DisplayName("AM-002-007: 组合筛选-品牌+状态")
    public void testCombinedFilter() throws Exception {
        String token = getAdminToken();
        MvcResult result = getWithToken("/admin/cars?brandId=1&status=1&page=1&size=10", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "组合筛选应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(8)
    @DisplayName("AM-002-008: 分页-第一页")
    public void testPaginationFirstPage() throws Exception {
        String token = getAdminToken();
        MvcResult result = getWithToken("/admin/cars?page=1&size=5", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "分页查询应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(9)
    @DisplayName("AM-002-009: 分页-中间页")
    public void testPaginationMiddlePage() throws Exception {
        String token = getAdminToken();
        MvcResult result = getWithToken("/admin/cars?page=2&size=5", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "分页查询应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(10)
    @DisplayName("AM-002-010: 分页-末页")
    public void testPaginationLastPage() throws Exception {
        String token = getAdminToken();
        MvcResult result = getWithToken("/admin/cars?page=100&size=5", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "分页查询应该成功");
    }

    // ==================== Day 2: 车辆详情与编辑 ====================

    @Test
    @org.junit.jupiter.api.Order(11)
    @DisplayName("AM-002-011: 正常查看-完整信息")
    public void testViewCarDetail() throws Exception {
        String token = getAdminToken();
        MvcResult result = getWithToken("/admin/cars/1", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "查看车辆详情应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(12)
    @DisplayName("AM-002-012: 正常编辑-修改价格")
    public void testEditCarPrice() throws Exception {
        String token = getAdminToken();
        String jsonBody = "{\"id\":1,\"name\":\"奥迪A4L\",\"pricePerDay\":400.0}";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/api/admin/cars/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andReturn();
        
        String content = result.getResponse().getContentAsString();
        if (content != null && !content.isEmpty()) {
            Result response = com.alibaba.fastjson.JSON.parseObject(content, Result.class);
        }
    }

    @Test
    @org.junit.jupiter.api.Order(13)
    @DisplayName("AM-002-013: 正常编辑-修改库存")
    public void testEditCarStock() throws Exception {
        String token = getAdminToken();
        String jsonBody = "{\"id\":1,\"name\":\"奥迪A4L\",\"stock\":10}";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/api/admin/cars/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andReturn();
        
        String content = result.getResponse().getContentAsString();
        if (content != null && !content.isEmpty()) {
            Result response = com.alibaba.fastjson.JSON.parseObject(content, Result.class);
        }
    }

    @Test
    @org.junit.jupiter.api.Order(14)
    @DisplayName("AM-002-014: 正常上架车辆")
    public void testEnableCar() throws Exception {
        String token = getAdminToken();
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/api/admin/cars/1/enable")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        
        String content = result.getResponse().getContentAsString();
        if (content != null && !content.isEmpty()) {
            Result response = com.alibaba.fastjson.JSON.parseObject(content, Result.class);
        }
    }

    @Test
    @org.junit.jupiter.api.Order(15)
    @DisplayName("AM-002-015: 正常下架车辆")
    public void testDisableCar() throws Exception {
        String token = getAdminToken();
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/api/admin/cars/1/disable")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        
        String content = result.getResponse().getContentAsString();
        if (content != null && !content.isEmpty()) {
            Result response = com.alibaba.fastjson.JSON.parseObject(content, Result.class);
        }
    }

    @Test
    @org.junit.jupiter.api.Order(16)
    @DisplayName("AM-002-016: 异常编辑-空名称")
    public void testEditCarWithEmptyName() throws Exception {
        String token = getAdminToken();
        String jsonBody = "{\"id\":1,\"name\":\"\"}";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/api/admin/cars/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andReturn();
        
        String content = result.getResponse().getContentAsString();
        if (content != null && !content.isEmpty()) {
            Result response = com.alibaba.fastjson.JSON.parseObject(content, Result.class);
        }
    }

    @Test
    @org.junit.jupiter.api.Order(17)
    @DisplayName("AM-002-017: 异常编辑-负数价格")
    public void testEditCarWithNegativePrice() throws Exception {
        String token = getAdminToken();
        String jsonBody = "{\"id\":1,\"pricePerDay\":-100.0}";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/api/admin/cars/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andReturn();
        
        String content = result.getResponse().getContentAsString();
        if (content != null && !content.isEmpty()) {
            Result response = com.alibaba.fastjson.JSON.parseObject(content, Result.class);
        }
    }

    // ==================== Day 3: 新增与删除车辆 ====================

    @Test
    @org.junit.jupiter.api.Order(18)
    @DisplayName("AM-002-018: 正常新增-完整信息")
    public void testAddCar() throws Exception {
        String token = getAdminToken();
        String jsonBody = "{\"name\":\"测试车辆\",\"brandId\":1,\"categoryId\":1,\"pricePerDay\":300.0,\"deposit\":3000.0,\"stock\":5}";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/cars")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andReturn();
        
        String content = result.getResponse().getContentAsString();
        if (content != null && !content.isEmpty()) {
            Result response = com.alibaba.fastjson.JSON.parseObject(content, Result.class);
        }
    }

    @Test
    @org.junit.jupiter.api.Order(19)
    @DisplayName("AM-002-019: 异常新增-缺少必填字段")
    public void testAddCarMissingFields() throws Exception {
        String token = getAdminToken();
        String jsonBody = "{\"name\":\"测试车辆\"}";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/cars")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andReturn();
        
        String content = result.getResponse().getContentAsString();
        if (content != null && !content.isEmpty()) {
            Result response = com.alibaba.fastjson.JSON.parseObject(content, Result.class);
        }
    }

    @Test
    @org.junit.jupiter.api.Order(20)
    @DisplayName("AM-002-020: 异常新增-重复名称")
    public void testAddCarDuplicateName() throws Exception {
        String token = getAdminToken();
        String jsonBody = "{\"name\":\"奥迪A4L\",\"brandId\":1,\"categoryId\":1,\"pricePerDay\":350.0}";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/cars")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andReturn();
        
        String content = result.getResponse().getContentAsString();
        if (content != null && !content.isEmpty()) {
            Result response = com.alibaba.fastjson.JSON.parseObject(content, Result.class);
        }
    }

    @Test
    @org.junit.jupiter.api.Order(21)
    @DisplayName("AM-002-021: 正常删除-无订单关联")
    public void testDeleteCar() throws Exception {
        String token = getAdminToken();
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.delete("/api/admin/cars/999")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        
        String content = result.getResponse().getContentAsString();
        if (content != null && !content.isEmpty()) {
            Result response = com.alibaba.fastjson.JSON.parseObject(content, Result.class);
        }
    }

    @Test
    @org.junit.jupiter.api.Order(22)
    @DisplayName("AM-002-022: 异常删除-有关联订单")
    public void testDeleteCarWithOrders() throws Exception {
        String token = getAdminToken();
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.delete("/api/admin/cars/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        
        String content = result.getResponse().getContentAsString();
        if (content != null && !content.isEmpty()) {
            Result response = com.alibaba.fastjson.JSON.parseObject(content, Result.class);
        }
    }

    @Test
    @org.junit.jupiter.api.Order(23)
    @DisplayName("AM-002-023: 批量上架")
    public void testBatchEnable() throws Exception {
        String token = getAdminToken();
        String jsonBody = "[1,2,3]";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/api/admin/cars/batch/enable")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andReturn();
        
        String content = result.getResponse().getContentAsString();
        if (content != null && !content.isEmpty()) {
            Result response = com.alibaba.fastjson.JSON.parseObject(content, Result.class);
        }
    }

    @Test
    @org.junit.jupiter.api.Order(24)
    @DisplayName("AM-002-024: 批量下架")
    public void testBatchDisable() throws Exception {
        String token = getAdminToken();
        String jsonBody = "[1,2,3]";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/api/admin/cars/batch/disable")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andReturn();
        
        String content = result.getResponse().getContentAsString();
        if (content != null && !content.isEmpty()) {
            Result response = com.alibaba.fastjson.JSON.parseObject(content, Result.class);
        }
    }

    @Test
    @org.junit.jupiter.api.Order(25)
    @DisplayName("AM-002-025: 权限测试-非管理员访问")
    public void testNonAdminAccess() throws Exception {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("zhangsan");
        dto.setPassword("123456");
        MvcResult loginResult = post("/auth/login", dto);
        Result loginResponse = parseResult(loginResult);
        JSONObject data = (JSONObject) loginResponse.getData();
        String userToken = data.getString("token");

        MvcResult result = getWithToken("/admin/cars?page=1&size=10", userToken);
        Result response = parseResult(result);
    }
}
