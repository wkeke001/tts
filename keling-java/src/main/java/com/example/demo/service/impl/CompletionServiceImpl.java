package com.example.demo.service.impl;

import com.example.demo.service.CompletionService;
import com.google.gson.Gson;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class CompletionServiceImpl implements CompletionService {

    @Value("${dashscope.api-key}")
    private String dashscopeApiKey;

    @Override
    public String getCompletion(String prompt) {
        try {
            Gson gson = new Gson();
            Map<String, Object> bodyMap = new HashMap<>();
            Map<String, Object> inputMap = new HashMap<>();
            inputMap.put("prompt", prompt);
            bodyMap.put("input", inputMap);
            bodyMap.put("parameters", new HashMap<>());
            bodyMap.put("debug", new HashMap<>());

            String requestBody = gson.toJson(bodyMap);

            URL url = new URL("https://dashscope.aliyuncs.com/api/v1/apps/3e778320cf7d44a2802c3aece5c46695/completion");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + dashscopeApiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            try (InputStream is = responseCode >= 200 && responseCode < 300
                    ? conn.getInputStream() : conn.getErrorStream()) {
                String responseString = new String(is.readAllBytes(), StandardCharsets.UTF_8);

                if (responseCode >= 200 && responseCode < 300) {
                    try {
                        JSONObject jsonResponse = new JSONObject(responseString);
                        JSONObject output = jsonResponse.getJSONObject("output");
                        return output.getString("text");
                    } catch (Exception e) {
                        return responseString;
                    }
                } else {
                    throw new RuntimeException("API 调用失败: " + responseCode + " - " + responseString);
                }
            } finally {
                conn.disconnect();
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("请求失败: " + e.getMessage(), e);
        }
    }
}
