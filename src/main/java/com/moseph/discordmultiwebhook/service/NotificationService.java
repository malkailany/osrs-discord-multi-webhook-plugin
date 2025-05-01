package com.moseph.discordmultiwebhook.service;

import com.moseph.discordmultiwebhook.DiscordMultiWebhookConfig;
import com.moseph.discordmultiwebhook.model.EventType;
import com.moseph.discordmultiwebhook.model.NotificationEvent;
import com.moseph.discordmultiwebhook.utils.DiscordUtils;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service for formatting, queuing, and processing notifications
 */
@Slf4j
public class NotificationService {
    private final DiscordMultiWebhookConfig config;

    @Inject
    public NotificationService(DiscordMultiWebhookConfig config) {
        this.config = config;
    }

    /**
     * Formats a notification message based on the event type and variables
     */
    public String formatMessage(EventType type, Map<String, String> variables) {
        String messageTemplate;
        
        switch (type) {
            case COLLECTION_LOG:
                messageTemplate = config.collectionLogMessage();
                break;
            case PET:
                messageTemplate = config.petMessage();
                break;
            case LEVEL_UP:
                messageTemplate = config.levelUpMessage();
                break;
            case LOOT:
                messageTemplate = config.lootMessage();
                break;
            case DEATH:
                messageTemplate = config.deathMessage();
                break;
            case QUEST:
                messageTemplate = config.questMessage();
                break;
            case CLUE_SCROLL:
                messageTemplate = config.clueScrollMessage();
                break;
            default:
                messageTemplate = "%USERNAME% received a notification!";
        }
        
        // Replace variables in the template
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            messageTemplate = messageTemplate.replace("%" + entry.getKey() + "%", DiscordUtils.sanitizeForDiscord(entry.getValue()));
        }
        
        // Sanitize the final message to prevent Discord exploits
        return DiscordUtils.sanitizeForDiscord(messageTemplate);
    }
    
    /**
     * Formats a combined message for multiple related events
     */
    public String formatCombinedMessage(List<NotificationEvent> events, Map<String, String> variables) {
        // Build event types list
        StringBuilder eventsStr = new StringBuilder();
        for (NotificationEvent event : events) {
            if (eventsStr.length() > 0) {
                eventsStr.append(", ");
            }
            eventsStr.append(event.getType().getDisplayName());
        }
        variables.put("EVENTS", eventsStr.toString());
        
        // Format the message
        String message = config.combinedMessageFormat();
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", DiscordUtils.sanitizeForDiscord(entry.getValue()));
        }
        
        // Sanitize the final message to prevent Discord exploits
        return DiscordUtils.sanitizeForDiscord(message);
    }
    
    /**
     * Gets the appropriate webhook URL for the event type
     */
    public String getWebhookUrl(EventType type) {
        String result = "";
        String source = "global";
        
        switch (type) {
            case COLLECTION_LOG:
                if (config.useCustomCollectionLogWebhook() && !config.collectionLogWebhookUrl().isEmpty()) {
                    result = DiscordUtils.validateWebhookUrl(config.collectionLogWebhookUrl());
                    source = "custom collection log";
                }
                break;
            case PET:
                if (config.useCustomPetWebhook() && !config.petWebhookUrl().isEmpty()) {
                    result = DiscordUtils.validateWebhookUrl(config.petWebhookUrl());
                    source = "custom pet";
                }
                break;
            case LEVEL_UP:
                if (config.useCustomLevelUpWebhook() && !config.levelUpWebhookUrl().isEmpty()) {
                    result = DiscordUtils.validateWebhookUrl(config.levelUpWebhookUrl());
                    source = "custom level up";
                }
                break;
            case LOOT:
                if (config.useCustomLootWebhook() && !config.lootWebhookUrl().isEmpty()) {
                    result = DiscordUtils.validateWebhookUrl(config.lootWebhookUrl());
                    source = "custom loot";
                }
                break;
            case DEATH:
                if (config.useCustomDeathWebhook() && !config.deathWebhookUrl().isEmpty()) {
                    result = DiscordUtils.validateWebhookUrl(config.deathWebhookUrl());
                    source = "custom death";
                }
                break;
            case QUEST:
                if (config.useCustomQuestWebhook() && !config.questWebhookUrl().isEmpty()) {
                    result = DiscordUtils.validateWebhookUrl(config.questWebhookUrl());
                    source = "custom quest";
                }
                break;
            case CLUE_SCROLL:
                if (config.useCustomClueScrollWebhook() && !config.clueScrollWebhookUrl().isEmpty()) {
                    result = DiscordUtils.validateWebhookUrl(config.clueScrollWebhookUrl());
                    source = "custom clue scroll";
                }
                break;
        }
        
        // Default to global webhook URL if no custom URL is set
        if (result.isEmpty()) {
            result = DiscordUtils.validateWebhookUrl(config.globalWebhookUrl());
            source = "global";
        }
        
        log.debug("Using {} webhook URL for {} notification: {}", 
            source, 
            type.getDisplayName(), 
            !result.isEmpty() ? "configured" : "NOT CONFIGURED");
            
        return result;
    }
    
    /**
     * Checks if an image should be sent with the notification
     */
    public boolean shouldSendImage(EventType type) {
        switch (type) {
            case COLLECTION_LOG:
                return config.collectionLogSendImage();
            case PET:
                return config.petSendImage();
            case LEVEL_UP:
                return config.levelUpSendImage();
            case LOOT:
                return config.lootSendImage();
            case DEATH:
                return config.deathSendImage();
            case QUEST:
                return config.questSendImage();
            case CLUE_SCROLL:
                return config.clueScrollSendImage();
            default:
                return false;
        }
    }
    
    /**
     * Determines which event type to prioritize when handling multiple related events
     */
    public EventType determinePrimaryEventType(List<NotificationEvent> events) {
        EventType primaryType = events.get(0).getType();
        
        if (config.preferCollectionLog()) {
            // Check if collection log is in the events
            for (NotificationEvent event : events) {
                if (event.getType() == EventType.COLLECTION_LOG) {
                    primaryType = EventType.COLLECTION_LOG;
                    break;
                }
            }
        }
        
        return primaryType;
    }
    
    /**
     * Groups related notifications together
     */
    public List<List<NotificationEvent>> groupRelatedNotifications(List<NotificationEvent> pendingNotifications) {
        List<List<NotificationEvent>> groupedNotifications = new ArrayList<>();
        List<NotificationEvent> currentGroup = new ArrayList<>();
        
        for (NotificationEvent event : pendingNotifications) {
            if (currentGroup.isEmpty()) {
                currentGroup.add(event);
            } else if (event.isRelatedTo(currentGroup.get(0))) {
                currentGroup.add(event);
            } else {
                groupedNotifications.add(new ArrayList<>(currentGroup));
                currentGroup.clear();
                currentGroup.add(event);
            }
        }
        
        if (!currentGroup.isEmpty()) {
            groupedNotifications.add(currentGroup);
        }
        
        log.debug("Grouped into {} notification groups", groupedNotifications.size());
        return groupedNotifications;
    }
} 