package com.moseph.discordmultiwebhook.simulation;

import com.moseph.discordmultiwebhook.DiscordMultiWebhookConfig;
import com.moseph.discordmultiwebhook.DiscordMultiWebhookPlugin;
import com.moseph.discordmultiwebhook.model.EventType;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Random;

/**
 * Handles simulation of in-game events for testing purposes
 */
@Slf4j
public class EventSimulator {
    private final Client client;
    private final ClientThread clientThread;
    private final ScheduledExecutorService executorService;
    private final DiscordMultiWebhookPlugin plugin;
    private final DiscordMultiWebhookConfig config;
    private final Random random = new Random();
    
    public EventSimulator(
            Client client,
            ClientThread clientThread,
            ScheduledExecutorService executorService,
            DiscordMultiWebhookPlugin plugin,
            DiscordMultiWebhookConfig config) {
        this.client = client;
        this.clientThread = clientThread;
        this.executorService = executorService;
        this.plugin = plugin;
        this.config = config;
    }
    
    /**
     * Simulates collection log notification
     */
    public void simulateCollectionLogEvent(boolean withImage) {
        Map<String, String> variables = new HashMap<>();
        variables.put("ITEM", "Dragon Warhammer");
        variables.put("USERNAME", getPlayerName());
        
        log.info("Simulating Collection Log notification for: Dragon Warhammer (with image: {})", withImage);
        clientThread.invoke(() -> client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", 
            "Discord webhook: Collection log - Dragon Warhammer", null));
        
        if (withImage) {
            simulateWithImage(EventType.COLLECTION_LOG, variables);
        } else {
            plugin.queueNotification(EventType.COLLECTION_LOG, variables);
        }
    }
    
    /**
     * Simulates pet drop notification
     */
    public void simulatePetEvent(boolean withImage) {
        Map<String, String> variables = new HashMap<>();
        variables.put("PET", "Vorki");
        variables.put("USERNAME", getPlayerName());
        
        log.info("Simulating Pet notification for: Vorki (with image: {})", withImage);
        clientThread.invoke(() -> client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", 
            "Discord webhook: Pet - Vorki", null));
        
        if (withImage) {
            simulateWithImage(EventType.PET, variables);
        } else {
            plugin.queueNotification(EventType.PET, variables);
        }
    }
    
    /**
     * Simulates level up notification
     */
    public void simulateLevelUpEvent(boolean withImage) {
        // Create random skill and level for testing
        String[] skills = {"Attack", "Defence", "Strength", "Hitpoints", "Ranged", "Prayer", 
            "Magic", "Cooking", "Woodcutting", "Fletching", "Fishing", "Firemaking", "Crafting", 
            "Smithing", "Mining", "Herblore", "Agility", "Thieving", "Slayer", "Farming", 
            "Runecraft", "Hunter", "Construction"};
            
        String skill = skills[random.nextInt(skills.length)];
        int level = random.nextInt(98) + 2; // Level 2-99
        int threshold = config.levelUpThreshold();
        
        // If threshold is > 1, make the test level a multiple of the threshold
        if (threshold > 1) {
            level = (level / threshold) * threshold;
            if (level < 2) level = threshold; // Ensure at least the threshold value
            if (level > 99) level = 99; // Cap at 99
        }

        Map<String, String> variables = new HashMap<>();
        variables.put("SKILL", skill);
        variables.put("LEVEL", String.valueOf(level));
        variables.put("USERNAME", getPlayerName());
        
        log.info("Simulating level up notification: {} level {} (threshold: {})", skill, level, threshold);
        plugin.queueNotification(EventType.LEVEL_UP, variables);
        
        if (withImage) {
            plugin.captureScreenshot(image -> {
                log.info("Captured screenshot for level up test (null: {})", image == null);
            });
        }
    }
    
    /**
     * Simulates loot notification
     */
    public void simulateLootEvent(boolean withImage) {
        Map<String, String> variables = new HashMap<>();
        variables.put("ITEM", "Twisted bow");
        variables.put("USERNAME", getPlayerName());
        variables.put("VALUE", "1,200,000,000");
        
        log.info("Simulating Loot notification for: Twisted bow (with image: {})", withImage);
        clientThread.invoke(() -> client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", 
            "Discord webhook: Loot - Twisted bow worth 1,200,000,000 GP", null));
        
        if (withImage) {
            simulateWithImage(EventType.LOOT, variables);
        } else {
            plugin.queueNotification(EventType.LOOT, variables);
        }
    }
    
    /**
     * Simulates death notification
     */
    public void simulateDeathEvent(boolean withImage) {
        Map<String, String> variables = new HashMap<>();
        variables.put("LOCATION", "Wilderness");
        variables.put("CAUSE", "Player attack");
        variables.put("USERNAME", getPlayerName());
        
        log.info("Simulating Death notification at: Wilderness (with image: {})", withImage);
        clientThread.invoke(() -> client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", 
            "Discord webhook: Death - Wilderness", null));
        
        if (withImage) {
            simulateWithImage(EventType.DEATH, variables);
        } else {
            plugin.queueNotification(EventType.DEATH, variables);
        }
    }
    
    /**
     * Simulates quest completion notification
     */
    public void simulateQuestEvent(boolean withImage) {
        Map<String, String> variables = new HashMap<>();
        variables.put("QUEST", "Dragon Slayer II");
        variables.put("USERNAME", getPlayerName());
        
        log.info("Simulating Quest completion notification for: Dragon Slayer II (with image: {})", withImage);
        clientThread.invoke(() -> client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", 
            "Discord webhook: Quest - Dragon Slayer II", null));
        
        if (withImage) {
            simulateWithImage(EventType.QUEST, variables);
        } else {
            plugin.queueNotification(EventType.QUEST, variables);
        }
    }
    
    /**
     * Simulates clue scroll completion notification
     */
    public void simulateClueEvent(boolean withImage) {
        Map<String, String> variables = new HashMap<>();
        variables.put("CLUETYPE", "Master");
        variables.put("COUNT", "42");
        variables.put("REWARD", "3rd age druidic");
        variables.put("USERNAME", getPlayerName());
        
        log.info("Simulating Clue scroll completion notification for: Master clue (with image: {})", withImage);
        clientThread.invoke(() -> client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", 
            "Discord webhook: Clue scroll - Master", null));
        
        if (withImage) {
            simulateWithImage(EventType.CLUE_SCROLL, variables);
        } else {
            plugin.queueNotification(EventType.CLUE_SCROLL, variables);
        }
    }
    
    /**
     * Simulates collection log and loot notification together
     */
    public void simulateCollectionAndLootEvent(boolean withImage) {
        log.info("Simulating Collection Log + Loot notification for the same item (with image: {})", withImage);
        
        // First simulate collection log event
        Map<String, String> clVariables = new HashMap<>();
        clVariables.put("ITEM", "Dragon Warhammer");
        clVariables.put("USERNAME", getPlayerName());
        
        // Then simulate loot event for the same item
        Map<String, String> lootVariables = new HashMap<>();
        lootVariables.put("ITEM", "Dragon Warhammer");
        lootVariables.put("USERNAME", getPlayerName());
        lootVariables.put("VALUE", "55,000,000");
        
        clientThread.invoke(() -> client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", 
            "Discord webhook: Testing Collection Log + Loot for the same item (Dragon Warhammer)", null));
        
        // Add both notifications to the queue
        plugin.queueNotification(EventType.COLLECTION_LOG, clVariables);
        plugin.queueNotification(EventType.LOOT, lootVariables);
        
        // If requested, send with image instead
        if (withImage) {
            simulateWithImage(EventType.COLLECTION_LOG, clVariables);
            // Add a slight delay for the second notification
            executorService.schedule(() -> simulateWithImage(EventType.LOOT, lootVariables), 1, TimeUnit.SECONDS);
        }
    }
    
    /**
     * Simulates all notification types in sequence
     */
    public void simulateAllEvents(boolean withImage) {
        log.info("Simulating all notification types (with image: {})", withImage);
        
        // Add a small delay between each notification
        executorService.schedule(() -> simulateCollectionLogEvent(withImage), 0, TimeUnit.SECONDS);
        executorService.schedule(() -> simulatePetEvent(withImage), 3, TimeUnit.SECONDS);
        executorService.schedule(() -> simulateLevelUpEvent(withImage), 6, TimeUnit.SECONDS);
        executorService.schedule(() -> simulateLootEvent(withImage), 9, TimeUnit.SECONDS);
        executorService.schedule(() -> simulateDeathEvent(withImage), 12, TimeUnit.SECONDS);
        executorService.schedule(() -> simulateQuestEvent(withImage), 15, TimeUnit.SECONDS);
        executorService.schedule(() -> simulateClueEvent(withImage), 18, TimeUnit.SECONDS);
        
        log.info("Scheduled all test notifications with 3 second intervals");
    }
    
    /**
     * Helper method to simulate a notification with a screenshot
     */
    private void simulateWithImage(EventType type, Map<String, String> variables) {
        clientThread.invokeLater(() -> {
            plugin.captureScreenshot(image -> {
                try {
                    plugin.queueNotification(type, variables);
                } catch (Exception e) {
                    log.warn("Error capturing screenshot for test notification", e);
                    plugin.queueNotification(type, variables);
                }
            });
        });
    }
    
    /**
     * Get the current player's name
     */
    private String getPlayerName() {
        if (client.getLocalPlayer() == null) {
            return "Unknown";
        }
        return client.getLocalPlayer().getName();
    }
} 