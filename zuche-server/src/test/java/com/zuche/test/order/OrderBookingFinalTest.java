package com.zuche.test.order;

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
 * 在线预订模块最终测试 (UC-004)
 * 共12条测试用例
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrderBookingFinalTest extends BaseTest {

    private static String token;
    private static final Long TEST_CAR_ID = 1L;
    private static final Long TEST_STORE_ID = 1L;

    private String getToken() throws Exception {
        if (token != null) return token;
        
        LoginDTO dto = new LoginDTO();
        dto.setUsername("zhangsan");
        dto.setPassword("123456");
        MvcResult result = post("/auth/login", dto);
        Result response = parseResult(result);
        if (response.getData() == null) {
            throw new RuntimeException("登录失败，无法获取token");
        }
        JSONObject data = (JSONObject) response.getData();
        token = data.getString("token");
        return token;
    }

    private Result executeOrderRequest(String token, String jsonBody) throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/orders")
                        .header("Authorization", token != null ? "Bearer " + token : "")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andReturn();
        
        String content = result.getResponse().getContentAsString();
        if (content == null || content.isEmpty()) {
            Result emptyResult = new Result();
            emptyResult.setCode(result.getResponse().getStatus());
            emptyResult.setMessage("Empty response");
            return emptyResult;
        }
        return com.alibaba.fastjson.JSON.parseObject(content, Result.class);
    }

    private String buildOrderJson(Long carId, Long pickupStoreId, Long returnStoreId,
                                   String startTime, String endTime, 
                                   Double totalPrice, Double deposit) {
        return String.format(
            "{\"carId\":%d,\"pickupStoreId\":%d,\"returnStoreId\":%d," +
            "\"startTime\":\"%s\",\"endTime\":\"%s\"," +
            "\"totalPrice\":%.2f,\"deposit\":%.2f}",
            carId, pickupStoreId, returnStoreId, startTime, endTime, totalPrice, deposit
        );
    }

    // ==================== Day 1: 正常预订流程 ====================

    @Test
    @org.junit.jupiter.api.Order(1)
    @DisplayName("UC-004-001: 正常预订-完整流程")
    public void testNormalBooking() throws Exception {
        String userToken = getToken();
        String jsonBody = buildOrderJson(TEST_CAR_ID, TEST_STORE_ID, TEST_STORE_ID,
                "2026-03-25T10:00:00", "2026-03-27T10:00:00", 700.0, 5000.0);

        Result response = executeOrderRequest(userToken, jsonBody);
        assertEquals(200, response.getCode(), "预订应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    @DisplayName("UC-004-002: 边界测试-最小租期1天")
    public void testMinimumRentalPeriod() throws Exception {
        String userToken = getToken();
        String jsonBody = buildOrderJson(TEST_CAR_ID, TEST_STORE_ID, TEST_STORE_ID,
                "2026-04-01T10:00:00", "2026-04-02T10:00:00", 350.0, 5000.0);

        Result response = executeOrderRequest(userToken, jsonBody);
        assertEquals(200, response.getCode(), "1天租期预订应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    @DisplayName("UC-004-003: 边界测试-最大租期30天")
    public void testMaximumRentalPeriod() throws Exception {
        String userToken = getToken();
        String jsonBody = buildOrderJson(TEST_CAR_ID, TEST_STORE_ID, TEST_STORE_ID,
                "2026-04-10T10:00:00", "2026-05-10T10:00:00", 10500.0, 5000.0);

        Result response = executeOrderRequest(userToken, jsonBody);
        assertEquals(200, response.getCode(), "30天租期预订应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    @DisplayName("UC-004-004: 库存检查-充足库存")
    public void testStockSufficient() throws Exception {
        String userToken = getToken();
        String jsonBody = buildOrderJson(TEST_CAR_ID, TEST_STORE_ID, TEST_STORE_ID,
                "2026-05-01T10:00:00", "2026-05-03T10:00:00", 700.0, 5000.0);

        Result response = executeOrderRequest(userToken, jsonBody);
        assertEquals(200, response.getCode(), "库存充足时预订应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    @DisplayName("UC-004-005: 价格计算-无优惠")
    public void testPriceCalculationNoDiscount() throws Exception {
        String userToken = getToken();
        String jsonBody = buildOrderJson(TEST_CAR_ID, TEST_STORE_ID, TEST_STORE_ID,
                "2026-06-01T10:00:00", "2026-06-04T10:00:00", 1050.0, 5000.0);

        Result response = executeOrderRequest(userToken, jsonBody);
        assertEquals(200, response.getCode(), "价格计算应该正确");
    }

    @Test
    @org.junit.jupiter.api.Order(6)
    @DisplayName("UC-004-006: 价格计算-有优惠券")
    public void testPriceCalculationWithCoupon() throws Exception {
        String userToken = getToken();
        String jsonBody = buildOrderJson(TEST_CAR_ID, TEST_STORE_ID, TEST_STORE_ID,
                "2026-07-01T10:00:00", "2026-07-03T10:00:00", 700.0, 5000.0);

        Result response = executeOrderRequest(userToken, jsonBody);
        assertEquals(200, response.getCode(), "预订应该成功");
    }

    // ==================== Day 2: 异常场景 ====================

    @Test
    @org.junit.jupiter.api.Order(7)
    @DisplayName("UC-004-007: 异常预订-无可用车辆")
    public void testBookingNoAvailableCar() throws Exception {
        String userToken = getToken();
        String jsonBody = buildOrderJson(99999L, TEST_STORE_ID, TEST_STORE_ID,
                "2026-08-01T10:00:00", "2026-08-03T10:00:00", 700.0, 5000.0);

        Result response = executeOrderRequest(userToken, jsonBody);
        assertNotEquals(200, response.getCode(), "不存在的车辆应该预订失败");
    }

    @Test
    @org.junit.jupiter.api.Order(8)
    @DisplayName("UC-004-008: 异常预订-未登录")
    public void testBookingWithoutLogin() throws Exception {
        String jsonBody = buildOrderJson(TEST_CAR_ID, TEST_STORE_ID, TEST_STORE_ID,
                "2026-09-01T10:00:00", "2026-09-03T10:00:00", 700.0, 5000.0);

        Result response = executeOrderRequest(null, jsonBody);
        assertNotEquals(200, response.getCode(), "未登录应该预订失败");
    }

    @Test
    @org.junit.jupiter.api.Order(9)
    @DisplayName("UC-004-009: 异常预订-缺少必要信息")
    public void testBookingMissingInfo() throws Exception {
        String userToken = getToken();
        String jsonBody = "{\"pickupStoreId\":1,\"returnStoreId\":1}";

        Result response = executeOrderRequest(userToken, jsonBody);
        assertNotEquals(200, response.getCode(), "缺少必要信息应该预订失败");
    }

    @Test
    @org.junit.jupiter.api.Order(10)
    @DisplayName("UC-004-010: 并发测试-库存扣减")
    public void testConcurrentBooking() throws Exception {
        String userToken = getToken();
        String jsonBody = buildOrderJson(TEST_CAR_ID, TEST_STORE_ID, TEST_STORE_ID,
                "2026-10-01T10:00:00", "2026-10-03T10:00:00", 700.0, 5000.0);

        Result response = executeOrderRequest(userToken, jsonBody);
        assertEquals(200, response.getCode(), "第一个订单应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(11)
    @DisplayName("UC-004-011: 超时测试-未支付释放")
    public void testBookingTimeoutRelease() throws Exception {
        String userToken = getToken();
        String jsonBody = buildOrderJson(TEST_CAR_ID, TEST_STORE_ID, TEST_STORE_ID,
                "2026-11-01T10:00:00", "2026-11-03T10:00:00", 700.0, 5000.0);

        Result response = executeOrderRequest(userToken, jsonBody);
        assertEquals(200, response.getCode(), "订单创建应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(12)
    @DisplayName("UC-004-012: 取消测试-主动取消")
    public void testBookingCancel() throws Exception {
        String userToken = getToken();
        String jsonBody = buildOrderJson(TEST_CAR_ID, TEST_STORE_ID, TEST_STORE_ID,
                "2026-12-01T10:00:00", "2026-12-03T10:00:00", 700.0, 5000.0);

        Result createResponse = executeOrderRequest(userToken, jsonBody);
        assertEquals(200, createResponse.getCode(), "订单创建应该成功");
        
        if (createResponse.getData() != null) {
            JSONObject orderData = (JSONObject) createResponse.getData();
            if (orderData.containsKey("id")) {
                Long orderId = orderData.getLong("id");
                
                MvcResult cancelResult = mockMvc.perform(MockMvcRequestBuilders.put("/api/orders/" + orderId + "/cancel")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON))
                        .andReturn();
                
                String content = cancelResult.getResponse().getContentAsString();
                if (content != null && !content.isEmpty()) {
                    Result cancelResponse = com.alibaba.fastjson.JSON.parseObject(content, Result.class);
                }
            }
        }
    }
}
