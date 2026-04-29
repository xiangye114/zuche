package com.zuche.test.base;

import com.alibaba.fastjson.JSON;
import com.zuche.common.Result;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 测试基类
 */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class BaseTest {

    @Autowired
    protected MockMvc mockMvc;

    protected String baseUrl = "";

    /**
     * 发送POST请求
     */
    protected MvcResult post(String url, Object params) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(baseUrl + url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(params)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andReturn();
    }

    /**
     * 发送GET请求
     */
    protected MvcResult get(String url) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get(baseUrl + url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andReturn();
    }

    /**
     * 发送带参数的GET请求
     */
    protected MvcResult get(String url, String paramName, String paramValue) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get(baseUrl + url)
                        .param(paramName, paramValue)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andReturn();
    }

    /**
     * 发送带Token的POST请求
     */
    protected MvcResult postWithToken(String url, Object params, String token) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(baseUrl + url)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(params)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andReturn();
    }

    /**
     * 发送带Token的GET请求
     */
    protected MvcResult getWithToken(String url, String token) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get(baseUrl + url)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andReturn();
    }

    /**
     * 解析响应结果
     */
    protected <T> Result<T> parseResult(MvcResult mvcResult, Class<T> clazz) throws Exception {
        String content = mvcResult.getResponse().getContentAsString();
        return JSON.parseObject(content, new com.alibaba.fastjson.TypeReference<Result<T>>() {});
    }

    /**
     * 解析响应结果（不指定类型）
     */
    protected Result parseResult(MvcResult mvcResult) throws Exception {
        String content = mvcResult.getResponse().getContentAsString();
        return JSON.parseObject(content, Result.class);
    }
}
