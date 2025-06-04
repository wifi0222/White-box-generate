package com.example.llm;

import okhttp3.*;

import java.io.IOException;

public class LLMTestCaseGenerator {

    private static final String API_KEY = "你的OpenAI_API密钥";
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    public static String generateTest(String methodSignature, String language) throws IOException {
        OkHttpClient client = new OkHttpClient();
        String prompt = "为以下函数生成一个高质量的白盒测试用例，语言为" + language + "：\n" + methodSignature;

        String json = "{\n" +
                "  \"model\": \"gpt-3.5-turbo\",\n" +
                "  \"messages\": [\n" +
                "    {\"role\": \"user\", \"content\": \"" + prompt + "\"}\n" +
                "  ]\n" +
                "}";

        Request request = new Request.Builder()
                .url(API_URL)
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer " + API_KEY)
                .build();

        Response response = client.newCall(request).execute();
        return response.body() != null ? response.body().string() : "生成失败";
    }
}