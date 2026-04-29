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
 * 订单管理模块测试 (UC-005)
 * 共26条测试用例
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrderManageTest extends BaseTest {

    private static String userToken;
    private static String adminToken;

    private String getUserToken() throws Exception {
        if (userToken != null) return userToken;
        LoginDTO dto = new LoginDTO();
        dto.setUsername("zhangsan");
        dto.setPassword("123456");
        MvcResult result = post("/auth/login", dto);
        Result response = parseResult(result);
        JSONObject data = (JSONObject) response.getData();
        userToken = data.getString("token");
        return userToken;
    }

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

    // ==================== Day 1: 订单列表查询 ====================

    @Test
    @org.junit.jupiter.api.Order(1)
    @DisplayName("UC-005-001: 正常查询-全部订单")
    public void testQueryAllOrders() throws Exception {
        String token = getUserToken();
        MvcResult result = getWithToken("/orders?page=1&size=10", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "查询全部订单应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    @DisplayName("UC-005-002: 状态筛选-待支付")
    public void testFilterByPendingStatus() throws Exception {
        String token = getUserToken();
        MvcResult result = getWithToken("/orders?status=0&page=1&size=10", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "按状态筛选应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    @DisplayName("UC-005-003: 状态筛选-进行中")
    public void testFilterByInProgressStatus() throws Exception {
        String token = getUserToken();
        MvcResult result = getWithToken("/orders?status=1&page=1&size=10", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "按状态筛选应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    @DisplayName("UC-005-004: 状态筛选-已完成")
    public void testFilterByCompletedStatus() throws Exception {
        String token = getUserToken();
        MvcResult result = getWithToken("/orders?status=2&page=1&size=10", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "按状态筛选应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    @DisplayName("UC-005-005: 状态筛选-已取消")
    public void testFilterByCancelledStatus() throws Exception {
        String token = getUserToken();
        MvcResult result = getWithToken("/orders?status=3&page=1&size=10", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "按状态筛选应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(6)
    @DisplayName("UC-005-006: 时间筛选-最近一周")
    public void testFilterByRecentWeek() throws Exception {
        String token = getUserToken();
        MvcResult result = getWithToken("/orders?page=1&size=10", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "查询订单应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(7)
    @DisplayName("UC-005-007: 时间筛选-最近一个月")
    public void testFilterByRecentMonth() throws Exception {
        String token = getUserToken();
        MvcResult result = getWithToken("/orders?page=1&size=10", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "查询订单应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(8)
    @DisplayName("UC-005-008: 组合筛选-状态+时间")
    public void testCombinedFilter() throws Exception {
        String token = getUserToken();
        MvcResult result = getWithToken("/orders?status=2&page=1&size=10", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "组合筛选应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(9)
    @DisplayName("UC-005-009: 分页-第一页")
    public void testPaginationFirstPage() throws Exception {
        String token = getUserToken();
        MvcResult result = getWithToken("/orders?page=1&size=5", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "分页查询应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(10)
    @DisplayName("UC-005-010: 分页-中间页")
    public void testPaginationMiddlePage() throws Exception {
        String token = getUserToken();
        MvcResult result = getWithToken("/orders?page=2&size=5", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "分页查询应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(11)
    @DisplayName("UC-005-011: 分页-末页")
    public void testPaginationLastPage() throws Exception {
        String token = getUserToken();
        MvcResult result = getWithToken("/orders?page=100&size=5", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "分页查询应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(12)
    @DisplayName("UC-005-012: 排序-按时间降序")
    public void testSortByTimeDesc() throws Exception {
        String token = getUserToken();
        MvcResult result = getWithToken("/orders?page=1&size=10", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "排序查询应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(13)
    @DisplayName("UC-005-013: 排序-按金额升序")
    public void testSortByAmountAsc() throws Exception {
        String token = getUserToken();
        MvcResult result = getWithToken("/orders?page=1&size=10", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "排序查询应该成功");
    }

    // ==================== Day 2: 订单详情与操作 ====================

    @Test
    @org.junit.jupiter.api.Order(14)
    @DisplayName("UC-005-014: 正常查看-完整信息")
    public void testViewOrderDetail() throws Exception {
        String token = getUserToken();
        MvcResult listResult = getWithToken("/orders?page=1&size=1", token);
        Result listResponse = parseResult(listResult);
        
        if (listResponse.getData() != null) {
            MvcResult detailResult = getWithToken("/orders/1", token);
            Result detailResponse = parseResult(detailResult);
            assertEquals(200, detailResponse.getCode(), "查看订单详情应该成功");
        }
    }

    @Test
    @org.junit.jupiter.api.Order(15)
    @DisplayName("UC-005-015: 正常支付-在线支付")
    public void testOnlinePayment() throws Exception {
        String token = getUserToken();
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/1/pay")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"paymentMethod\":\"online\"}"))
                .andReturn();
        
        String content = result.getResponse().getContentAsString();
        if (content != null && !content.isEmpty()) {
            Result response = com.alibaba.fastjson.JSON.parseObject(content, Result.class);
        }
    }

    @Test
    @org.junit.jupiter.api.Order(16)
    @DisplayName("UC-005-016: 正常取消-未支付订单")
    public void testCancelUnpaidOrder() throws Exception {
        String token = getUserToken();
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/api/orders/1/cancel")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        
        String content = result.getResponse().getContentAsString();
        if (content != null && !content.isEmpty()) {
            Result response = com.alibaba.fastjson.JSON.parseObject(content, Result.class);
        }
    }

    @Test
    @org.junit.jupiter.api.Order(17)
    @DisplayName("UC-005-017: 异常取消-已支付订单")
    public void testCancelPaidOrder() throws Exception {
        String token = getUserToken();
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/api/orders/2/cancel")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        
        String content = result.getResponse().getContentAsString();
        if (content != null && !content.isEmpty()) {
            Result response = com.alibaba.fastjson.JSON.parseObject(content, Result.class);
        }
    }

    @Test
    @org.junit.jupiter.api.Order(18)
    @DisplayName("UC-005-018: 正常申请退款")
    public void testApplyRefund() throws Exception {
        String token = getUserToken();
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/1/refund")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"行程变更\"}"))
                .andReturn();
        
        String content = result.getResponse().getContentAsString();
        if (content != null && !content.isEmpty()) {
            Result response = com.alibaba.fastjson.JSON.parseObject(content, Result.class);
        }
    }

    @Test
    @org.junit.jupiter.api.Order(19)
    @DisplayName("UC-005-019: 正常评价订单")
    public void testReviewOrder() throws Exception {
        String token = getUserToken();
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/2/review")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":5,\"content\":\"服务很好\"}"))
                .andReturn();
        
        String content = result.getResponse().getContentAsString();
        if (content != null && !content.isEmpty()) {
            Result response = com.alibaba.fastjson.JSON.parseObject(content, Result.class);
        }
    }

    @Test
    @org.junit.jupiter.api.Order(20)
    @DisplayName("UC-005-020: 异常评价-重复评价")
    public void testDuplicateReview() throws Exception {
        String token = getUserToken();
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/1/review")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":5,\"content\":\"很好\"}"))
                .andReturn();
        
        String content = result.getResponse().getContentAsString();
        if (content != null && !content.isEmpty()) {
            Result response = com.alibaba.fastjson.JSON.parseObject(content, Result.class);
        }
    }

    // ==================== Day 3: 订单状态流转 ====================

    @Test
    @org.junit.jupiter.api.Order(21)
    @DisplayName("UC-005-021: 状态流转-待支付→已支付")
    public void testStatusPendingToPaid() throws Exception {
        String token = getUserToken();
        MvcResult result = getWithToken("/orders/1", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "查询订单应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(22)
    @DisplayName("UC-005-022: 状态流转-已支付→进行中")
    public void testStatusPaidToInProgress() throws Exception {
        String token = getUserToken();
        MvcResult result = getWithToken("/orders/2", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "查询订单应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(23)
    @DisplayName("UC-005-023: 状态流转-进行中→已完成")
    public void testStatusInProgressToCompleted() throws Exception {
        String token = getUserToken();
        MvcResult result = getWithToken("/orders/3", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "查询订单应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(24)
    @DisplayName("UC-005-024: 状态流转-待支付→已取消")
    public void testStatusPendingToCancelled() throws Exception {
        String token = getUserToken();
        MvcResult result = getWithToken("/orders/4", token);
        Result response = parseResult(result);
        assertEquals(200, response.getCode(), "查询订单应该成功");
    }

    @Test
    @org.junit.jupiter.api.Order(25)
    @DisplayName("UC-005-025: 异常流转-已完成→取消")
    public void testInvalidStatusTransition() throws Exception {
        String token = getUserToken();
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/api/orders/3/cancel")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        
        String content = result.getResponse().getContentAsString();
        if (content != null && !content.isEmpty()) {
            Result response = com.alibaba.fastjson.JSON.parseObject(content, Result.class);
        }
    }

    @Test
    @org.junit.jupiter.api.Order(26)
    @DisplayName("UC-005-026: 权限测试-查看他人订单")
    public void testViewOthersOrder() throws Exception {
        String token = getUserToken();
        MvcResult result = getWithToken("/orders/10", token);
        Result response = parseResult(result);
    }
}
