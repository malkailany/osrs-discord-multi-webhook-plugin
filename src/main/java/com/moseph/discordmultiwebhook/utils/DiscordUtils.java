package com.moseph.discordmultiwebhook.utils;

import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;

/**
 * Utility class for Discord webhook functionality
 */
@Slf4j
public class DiscordUtils {

    /**
     * Sanitizes text for Discord to prevent exploits like @everyone/@here mentions
     * and other markdown exploits
     */
    public static String sanitizeForDiscord(String input) {
        if (input == null) {
            return "";
        }
        
        // Escape common Discord markdown syntax characters
        String sanitized = input;
        
        // Prevent @everyone and @here mentions
        sanitized = sanitized.replace("@everyone", "@\u200Beveryone");
        sanitized = sanitized.replace("@here", "@\u200Bhere");
        
        // Prevent role mentions (generalized approach)
        sanitized = sanitized.replaceAll("@([\\w-]+)", "@\u200B$1");
        
        // Escape markdown characters to prevent formatting exploits
        sanitized = sanitized.replace("\\", "\\\\"); // Must be first to prevent escaping the escapes
        sanitized = sanitized.replace("*", "\\*");
        sanitized = sanitized.replace("_", "\\_");
        sanitized = sanitized.replace("`", "\\`");
        sanitized = sanitized.replace("~", "\\~");
        sanitized = sanitized.replace(">", "\\>");
        
        // Limit excessive length
        int maxLength = 1900; // Discord's max is 2000, leaving some room for other JSON
        if (sanitized.length() > maxLength) {
            sanitized = sanitized.substring(0, maxLength) + "...";
        }
        
        return sanitized;
    }
    
    /**
     * Escapes special characters in a string for use in JSON
     */
    public static String escapeJsonString(String input) {
        if (input == null) {
            return "";
        }
        
        StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '\"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '/':
                    escaped.append("\\/");
                    break;
                case '\b':
                    escaped.append("\\b");
                    break;
                case '\f':
                    escaped.append("\\f");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    escaped.append(c);
            }
        }
        return escaped.toString();
    }
    
    /**
     * Validates and sanitizes webhook URLs to ensure they are valid Discord webhook URLs.
     * Returns empty string if invalid.
     */
    public static String validateWebhookUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }
        
        // Basic structure validation for Discord webhook URLs
        // Discord webhook URLs should be in the format: https://discord.com/api/webhooks/{id}/{token}
        // or https://discordapp.com/api/webhooks/{id}/{token}
        if (!url.startsWith("https://discord.com/api/webhooks/") && 
            !url.startsWith("https://discordapp.com/api/webhooks/")) {
            log.warn("Invalid Discord webhook URL format: {}", url);
            return "";
        }
        
        // Parse URL to validate it
        HttpUrl httpUrl = HttpUrl.parse(url);
        if (httpUrl == null) {
            log.warn("Could not parse webhook URL: {}", url);
            return "";
        }
        
        // Minimum path segments check
        if (httpUrl.pathSegments().size() < 4) {
            log.warn("Discord webhook URL has insufficient path segments: {}", url);
            return "";
        }
        
        return url;
    }
} 