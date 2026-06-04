package com.example.demo.controller;

import com.example.demo.common.ApiResult;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/emojis")
public class EmojiController {

    private static final String EMOJI_DIR_PATH = "static/emoji";

    @GetMapping
    public ApiResult<Map<String, Object>> search(@RequestParam("keyword") String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("关键词不能为空");
        }

        File emojiDir = new File(EMOJI_DIR_PATH);
        if (!emojiDir.exists() || !emojiDir.isDirectory()) {
            throw new RuntimeException("表情目录不存在");
        }

        File[] files = emojiDir.listFiles();
        if (files == null || files.length == 0) {
            Map<String, Object> data = new HashMap<>();
            data.put("keyword", keyword);
            data.put("totalCount", 0);
            data.put("files", new ArrayList<>());
            return ApiResult.success(data);
        }

        String lowerKeyword = keyword.toLowerCase();
        List<Map<String, Object>> matchedFiles = new ArrayList<>();

        for (File file : files) {
            if (!file.isFile()) continue;

            String fileName = file.getName();
            String lowerFileName = fileName.toLowerCase();

            int score = calculateScore(lowerFileName, lowerKeyword);
            if (score > 0) {
                Map<String, Object> fileInfo = new HashMap<>();
                fileInfo.put("fileName", fileName);
                fileInfo.put("url", "static/emoji/" + fileName);
                fileInfo.put("score", score);
                matchedFiles.add(fileInfo);
            }
        }

        matchedFiles.sort((a, b) -> ((Integer) b.get("score")).compareTo((Integer) a.get("score")));

        Map<String, Object> data = new HashMap<>();
        data.put("keyword", keyword);
        data.put("totalCount", matchedFiles.size());
        data.put("files", matchedFiles);
        return ApiResult.success(data);
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<Resource> download(@PathVariable("fileName") String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String safeFileName = new File(fileName).getName();
        File file = new File(EMOJI_DIR_PATH, safeFileName);

        if (!file.exists() || !file.isFile()) {
            return ResponseEntity.notFound().build();
        }

        try {
            String canonicalPath = file.getCanonicalPath();
            String canonicalDirPath = new File(EMOJI_DIR_PATH).getCanonicalPath();
            if (!canonicalPath.startsWith(canonicalDirPath)) {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

        Resource resource = new FileSystemResource(file);
        String contentType = determineContentType(safeFileName);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + URLEncoder.encode(safeFileName, StandardCharsets.UTF_8) + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(file.length())
                .body(resource);
    }

    private int calculateScore(String fileName, String keyword) {
        if (fileName.equals(keyword)) {
            return 100;
        } else if (fileName.startsWith(keyword)) {
            return 80;
        } else if (fileName.contains(keyword)) {
            return 60;
        } else {
            for (char c : keyword.toCharArray()) {
                if (fileName.indexOf(c) == -1) {
                    return 0;
                }
            }
            return 40;
        }
    }

    private String determineContentType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".mp4")) return "video/mp4";
        if (lower.endsWith(".webm")) return "video/webm";
        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
}
