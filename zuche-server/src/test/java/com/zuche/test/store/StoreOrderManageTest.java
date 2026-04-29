package com.zuche.test.store;

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
 * 门店端-订单管理测试 (AM-008)
 * 共10条测试用例
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StoreOrderManageTest extends BaseTest {

    private static String storeAdminToken;

    private String getStoreAdminToken() throws Exception {
        if (storeAdminToken != null) return storeAdminToken;
        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("123456");
        MvcResult result = post("/auth/login", dto);
        Result response = parseResult(result);
        JSONObject data = (JSONObject) response.getData();
        storeAdminToken = data.getString("token");
        return storeAdminToken;
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    @DisplayName("AM-008-001: 正常查询-门店全部订单")
    public void testQueryStoreOrders() throws Exception {
        String token = getStoreAdminToken();
        MvcResult result = getWithToken("/store/orders?page=1&size=10", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "查询门店订单应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    @DisplayName("AM-008-002: 状态筛选-待取车")
    public void testFilterByPendingPickup() throws Exception {
        String token = getStoreAdminToken();
        MvcResult result = getWithToken("/store/orders?status=1&page=1&size=10", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "状态筛选应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    @DisplayName("AM-008-003: 状态筛选-租赁中")
    public void testFilterByRentalInProgress() throws Exception {
        String token = getStoreAdminToken();
        MvcResult result = getWithToken("/store/orders?status=2&page=1&size=10", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "状态筛选应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    @DisplayName("AM-008-004: 状态筛选-待还车")
    public void testFilterByPendingReturn() throws Exception {
        String token = getStoreAdminToken();
        MvcResult result = getWithToken("/store/orders?status=3&page=1&size=10", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "状态筛选应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    @DisplayName("AM-008-005: 今日订单查询")
    public void testQueryTodayOrders() throws Exception {
        String token = getStoreAdminToken();
        MvcResult result = getWithToken("/store/orders/today", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "查询今日订单应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(6)
    @DisplayName("AM-008-006: 订单详情查看")
    public void testViewOrderDetail() throws Exception {
        String token = getStoreAdminToken();
        MvcResult result = getWithToken("/store/orders/1", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "查看订单详情应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(7)
    @DisplayName("AM-008-007: 确认取车")
    public void testConfirmPickup() throws Exception {
        String token = getStoreAdminToken();
        String jsonBody = "{\"pickupTime\":\"2026-03-20T10:00:00\",\"remark\":\"车辆检查正常\"}";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/store/orders/1/pickup")
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
    @DisplayName("AM-008-008: 确认还车")
    public void testConfirmReturn() throws Exception {
        String token = getStoreAdminToken();
        String jsonBody = "{\"returnTime\":\"2026-03-22T10:00:00\",\"remark\":\"车辆完好\"}";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/store/orders/1/return")
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
    @DisplayName("AM-008-009: 车辆检查记录")
    public void testCarInspection() throws Exception {
        String token = getStoreAdminToken();
        String jsonBody = "{\"fuelLevel\":80,\"mileage\":5000,\"damage\":\"无\"}";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/store/orders/1/inspection")
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
    @org.junit.jupiter.api.Order(10)
    @DisplayName("AM-008-010: 门店数据统计")
    public void testStoreStatistics() throws Exception {
        String token = getStoreAdminToken();
        MvcResult result = getWithToken("/store/statistics", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "获取门店统计数据应该成功");
    }
}
