package com.example.demo.service.impl;

import com.aliyun.oss.OSS;
import com.example.demo.config.OSSConfig;
import com.example.demo.service.FileService;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private OSS ossClient;

    @Autowired
    private OSSConfig ossConfig;

    @Value("${dashscope.api-key}")
    private String dashscopeApiKey;

    @Override
    public Map<String, Object> uploadAndProcessGif(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        File tempGifFile = null;
        File tempMp4File = null;
        File finalMp4File = null;
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("文件不能为空");
            }

            String fileName = file.getOriginalFilename();
            if (fileName == null || !fileName.toLowerCase().endsWith(".gif")) {
                throw new IllegalArgumentException("请上传 GIF 格式文件");
            }

            tempGifFile = File.createTempFile("temp", ".gif");
            file.transferTo(tempGifFile);

            tempMp4File = File.createTempFile("temp", ".mp4");
            convertGifToMp4(tempGifFile, tempMp4File);

            finalMp4File = File.createTempFile("final", ".mp4");
            concatenateVideo(tempMp4File, finalMp4File, 6);

            String objectName = "processed-" + UUID.randomUUID() + ".mp4";
            String bucketName = ossConfig.getBucketName();

            try (InputStream inputStream = new FileInputStream(finalMp4File)) {
                ossClient.putObject(bucketName, objectName, inputStream);
            }

            String fileUrl = "https://" + bucketName + "." + ossConfig.getEndpoint() + "/" + objectName;

            String analysisResult = analyzeImage(fileUrl);

            result.put("fileUrl", fileUrl);
            result.put("generatedFileName", analysisResult);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("文件处理失败: " + e.getMessage(), e);
        } finally {
            deleteQuietly(tempGifFile);
            deleteQuietly(tempMp4File);
            deleteQuietly(finalMp4File);
        }
        return result;
    }

    @Override
    public String analyzeImage(String fileUrl) {
        try {
            String requestBody = "{\n" +
                    "    \"model\": \"qwen3.5-plus\",\n" +
                    "    \"parameters\": {\n" +
                    "        \"enable_thinking\": false\n" +
                    "    },\n" +
                    "    \"input\": {\n" +
                    "        \"messages\": [\n" +
                    "            {\n" +
                    "                \"role\": \"user\",\n" +
                    "                \"content\": [\n" +
                    "                    {\"image\": \"" + fileUrl + "\"},\n" +
                    "                    {\"text\": \"简洁描述图中卡通人物的动作状态四个中文，人物心情四个中文,只需输出XXXX-XXXX 即可\"}\n" +
                    "                ]\n" +
                    "            }\n" +
                    "        ]\n" +
                    "    }\n" +
                    "}";

            URL url = new URL("https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + dashscopeApiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.getBytes(StandardCharsets.UTF_8));
            }

            try (InputStream is = conn.getInputStream()) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            } finally {
                conn.disconnect();
            }
        } catch (Exception e) {
            throw new RuntimeException("图片分析失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> uploadGifFirstFrame(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        File tempGifFile = null;
        File firstFrameFile = null;
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("文件不能为空");
            }

            String fileName = file.getOriginalFilename();
            if (fileName == null || !fileName.toLowerCase().endsWith(".gif")) {
                throw new IllegalArgumentException("请上传 GIF 格式文件");
            }

            tempGifFile = File.createTempFile("temp", ".gif");
            file.transferTo(tempGifFile);

            firstFrameFile = extractFirstFrameFromGif(tempGifFile);

            String objectName = "first-frame-" + UUID.randomUUID().toString().substring(0, 5) + ".jpg";
            String bucketName = ossConfig.getBucketName();

            try (InputStream inputStream = new FileInputStream(firstFrameFile)) {
                ossClient.putObject(bucketName, objectName, inputStream);
            }

            String fileUrl = "https://" + bucketName + "." + ossConfig.getEndpoint() + "/" + objectName;

            String modelResponse = analyzeImage(fileUrl);

            String extractedText = fileName;
            try {
                JSONObject jsonResponse = new JSONObject(modelResponse);
                JSONObject output = jsonResponse.getJSONObject("output");
                JSONObject choices = output.getJSONArray("choices").getJSONObject(0);
                JSONObject message = choices.getJSONObject("message");
                JSONObject content = message.getJSONArray("content").getJSONObject(0);
                extractedText = content.getString("text");
            } catch (Exception ignored) {
            }

            String fileNameWithExtension = extractedText;
            if (!fileNameWithExtension.toLowerCase().endsWith(".gif")) {
                fileNameWithExtension += ".gif";
            }

            saveFileToDDrive(fileNameWithExtension, tempGifFile);

            result.put("fileUrl", fileUrl);
            result.put("generatedFileName", fileNameWithExtension);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("处理失败: " + e.getMessage(), e);
        } finally {
            deleteQuietly(tempGifFile);
            deleteQuietly(firstFrameFile);
        }
        return result;
    }

    private void convertGifToMp4(File gifFile, File mp4File) throws IOException {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(gifFile)) {
            grabber.start();
            int width = grabber.getImageWidth();
            int height = grabber.getImageHeight();
            double frameRate = grabber.getFrameRate();

            try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(mp4File, width, height)) {
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                recorder.setFormat("mp4");
                recorder.setFrameRate(frameRate > 0 ? frameRate : 10);
                recorder.setVideoBitrate(2000000);
                recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
                recorder.start();

                Frame frame;
                while ((frame = grabber.grabFrame()) != null) {
                    recorder.record(frame);
                }
                recorder.stop();
            }
            grabber.stop();
        }
    }

    private void concatenateVideo(File inputFile, File outputFile, int times) throws IOException {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile)) {
            grabber.start();
            int width = grabber.getImageWidth();
            int height = grabber.getImageHeight();
            double frameRate = grabber.getFrameRate();

            try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, width, height)) {
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                recorder.setFormat("mp4");
                recorder.setFrameRate(frameRate > 0 ? frameRate : 10);
                recorder.setVideoBitrate(2000000);
                recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
                recorder.start();

                Frame frame;
                for (int i = 0; i < times; i++) {
                    grabber.setFrameNumber(0);
                    while ((frame = grabber.grabFrame()) != null) {
                        recorder.record(frame);
                    }
                }
                recorder.stop();
            }
            grabber.stop();
        }
    }

    private File extractFirstFrameFromGif(File gifFile) throws IOException {
        File outputFile = File.createTempFile("first-frame", ".jpg");
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(gifFile)) {
            grabber.start();
            Frame frame = grabber.grabFrame();
            if (frame != null) {
                Java2DFrameConverter converter = new Java2DFrameConverter();
                java.awt.image.BufferedImage bufferedImage = converter.convert(frame);
                ImageIO.write(bufferedImage, "jpg", outputFile);
            }
            grabber.stop();
        }
        return outputFile;
    }

    private void saveFileToDDrive(String fileName, File file) throws IOException {
        String savePath = "static/emoji/" + fileName;
        File saveFile = new File(savePath);
        File parentDir = saveFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        java.nio.file.Files.copy(file.toPath(), saveFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    private void deleteQuietly(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }
}
