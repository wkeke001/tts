package com.example.demo.controller;

import com.example.demo.common.ApiResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/scene")
public class SceneController {

    @Value("${dify.api-url}")
    private String difyApiUrl;

    @Value("${dify.api-token}")
    private String difyApiToken;

    @PostMapping("/agent")
    public String callAgent(@RequestBody String body) {
        try {
            URL url = new URL(difyApiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + difyApiToken);
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            try (InputStream is = responseCode >= 200 && responseCode < 300
                    ? conn.getInputStream() : conn.getErrorStream()) {
                String response = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                if (responseCode >= 200 && responseCode < 300) {
                    return response;
                } else {
                    throw new RuntimeException("Dify API error: " + responseCode + " - " + response);
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
