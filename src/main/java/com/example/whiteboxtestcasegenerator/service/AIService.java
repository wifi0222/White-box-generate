package com.example.whiteboxtestcasegenerator.service;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

@Service
public class AIService {
    private String apiUrl = "https://open.bigmodel.cn/api/paas/v4/chat/completions"; // 替换为具体的API URL
    private String apiKey = "1ab053b0d4194225b25b2139ba6fcd68.98mhImT6Nke9ofce"; // 用您的API Key替换

    public String callAI(String input) {
        String result = "";
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // 创建HTTP POST请求
            HttpPost post = new HttpPost(apiUrl);
            post.setHeader("Content-Type", "application/json");
            post.setHeader("Authorization", "Bearer " + apiKey);

            // 设置请求体
            String jsonRequest = String.format("{\"input\": \"%s\"}", input);
            post.setEntity(new StringEntity(jsonRequest));

            // 发送请求并获取响应
            try (CloseableHttpResponse response = httpClient.execute(post)) {
                // 处理响应
                result = EntityUtils.toString(response.getEntity());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result; // 返回API响应结果
    }
}