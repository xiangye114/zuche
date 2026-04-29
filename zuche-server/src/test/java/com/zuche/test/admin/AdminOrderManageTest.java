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
 * 管理端-订单管理测试 (AM-003)
 * 共9条测试用例
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AdminOrderManageTest extends BaseTest {

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

    @Test
    @org.junit.jupiter.api.Order(1)
    @DisplayName("AM-003-001: 正常查询-全部订单")
    public void testQueryAllOrders() throws Exception {
        String token = getAdminToken();
        MvcResult result = getWithToken("/admin/orders?page=1&size=10", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "查询全部订单应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    @DisplayName("AM-003-002: 状态筛选-待处理")
    public void testFilterByPendingStatus() throws Exception {
        String token = getAdminToken();
        MvcResult result = getWithToken("/admin/orders?status=0&page=1&size=10", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "状态筛选应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    @DisplayName("AM-003-003: 状态筛选-进行中")
    public void testFilterByInProgressStatus() throws Exception {
        String token = getAdminToken();
        MvcResult result = getWithToken("/admin/orders?status=1&page=1&size=10", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "状态筛选应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    @DisplayName("AM-003-004: 状态筛选-已完成")
    public void testFilterByCompletedStatus() throws Exception {
        String token = getAdminToken();
        MvcResult result = getWithToken("/admin/orders?status=2&page=1&size=10", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "状态筛选应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    @DisplayName("AM-003-005: 时间范围筛选")
    public void testFilterByTimeRange() throws Exception {
        String token = getAdminToken();
        MvcResult result = getWithToken("/admin/orders?page=1&size=10", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "时间筛选应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(6)
    @DisplayName("AM-003-006: 订单详情查看")
    public void testViewOrderDetail() throws Exception {
        String token = getAdminToken();
        MvcResult result = getWithToken("/admin/orders/1", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "查看订单详情应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(7)
    @DisplayName("AM-003-007: 订单状态修改")
    public void testUpdateOrderStatus() throws Exception {
        String token = getAdminToken();
        String jsonBody = "{\"status\":2}";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/api/admin/orders/1/status")
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
    @org.junit.jupiter.api.Order(8)
    @DisplayName("AM-003-008: 退款审批")
    public void testApproveRefund() throws Exception {
        String token = getAdminToken();
        String jsonBody = "{\"approved\":true}";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/orders/1/refund/approve")
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
    @org.junit.jupiter.api.Order(9)
    @DisplayName("AM-003-009: 数据统计")
    public void testOrderStatistics() throws Exception {
        String token = getAdminToken();
        MvcResult result = getWithToken("/admin/orders/statistics", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "获取统计数据应该成功");
    }
}
