package com.moseph.discordmultiwebhook.service;

import com.moseph.discordmultiwebhook.utils.DiscordUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Service for handling Discord webhook interactions
 */
@Slf4j
public class DiscordService {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    
    private final OkHttpClient httpClient;
    
    public DiscordService(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }
    
    /**
     * Sends a text message to a Discord webhook
     */
    public void sendWebhook(String webhookUrl, String avatarUrl, String content) {
        log.debug("Sending webhook to URL: {}", webhookUrl);
        log.debug("Webhook content: {}", content);
        
        HttpUrl url = HttpUrl.parse(webhookUrl);
        if (url == null) {
            log.warn("Invalid Discord webhook URL: {}", webhookUrl);
            return;
        }
        
        // Build the webhook payload - simplified to only send content
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"content\":\"").append(DiscordUtils.escapeJsonString(content)).append("\"");
        json.append("}");
        
        String jsonPayload = json.toString();
        log.debug("Webhook JSON payload: {}", jsonPayload);
        
        RequestBody body = RequestBody.create(JSON, jsonPayload);
        Request request = new Request.Builder()
            .url(url)
            .post(body)
            .build();
        
        log.debug("Sending webhook request...");
        
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.warn("Error sending Discord webhook", e);
            }
            
            @Override
            public void onResponse(Call call, Response response) {
                try (response) {
                    if (!response.isSuccessful()) {
                        log.warn("Error sending Discord webhook: {} - {}", response.code(), response.message());
                        String responseBody = response.body() != null ? response.body().string() : "no response body";
                        log.warn("Response body: {}", responseBody);
                        return;
                    }
                    
                    log.info("Successfully sent Discord webhook");
                } catch (IOException e) {
                    log.warn("Error reading webhook response", e);
                }
            }
        });
    }
    
    /**
     * Converts a BufferedImage to a byte array
     */
    public static byte[] convertImageToByteArray(BufferedImage bufferedImage) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
    
    /**
     * Sends a webhook with an image attachment
     */
    public void sendWebhookWithImage(String webhookUrl, String avatarUrl, String content, BufferedImage image) {
        try {
            byte[] imageBytes = convertImageToByteArray(image);
            sendWebhookWithImageBytes(webhookUrl, avatarUrl, content, imageBytes);
        } catch (IOException e) {
            log.warn("Error converting image to byte array", e);
            sendWebhook(webhookUrl, avatarUrl, content);
        }
    }
    
    /**
     * Sends a webhook with image bytes as an attachment
     */
    public void sendWebhookWithImageBytes(String webhookUrl, String avatarUrl, String content, byte[] imageBytes) {
        HttpUrl url = HttpUrl.parse(webhookUrl);
        if (url == null) {
            log.warn("Invalid Discord webhook URL: {}", webhookUrl);
            return;
        }
        
        log.debug("Creating multipart request with image and JSON payload");
        
        // Create JSON payload without embedding the image
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"content\":\"").append(DiscordUtils.escapeJsonString(content)).append("\"");
        json.append("}");
        
        String jsonPayload = json.toString();
        log.debug("Image webhook JSON payload: {}", jsonPayload);
        
        // Create a multipart request to include both the JSON payload and the image
        MultipartBody.Builder requestBuilder = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("payload_json", jsonPayload);
        
        // Add the image as a form data part
        RequestBody imageBody = RequestBody.create(
            MediaType.parse("image/png"),
            imageBytes
        );
        requestBuilder.addFormDataPart("file", "screenshot.png", imageBody);
        
        Request request = new Request.Builder()
            .url(url)
            .post(requestBuilder.build())
            .build();
        
        log.debug("Sending webhook request with image as regular attachment...");
        
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.warn("Error sending Discord webhook with image", e);
            }
            
            @Override
            public void onResponse(Call call, Response response) {
                try (response) {
                    if (!response.isSuccessful()) {
                        log.warn("Error sending Discord webhook with image: {} - {}", response.code(), response.message());
                        String responseBody = response.body() != null ? response.body().string() : "no response body";
                        log.warn("Response body: {}", responseBody);
                        return;
                    }
                    
                    log.info("Successfully sent Discord webhook with image");
                } catch (IOException e) {
                    log.warn("Error reading webhook with image response", e);
                }
            }
        });
    }
} 