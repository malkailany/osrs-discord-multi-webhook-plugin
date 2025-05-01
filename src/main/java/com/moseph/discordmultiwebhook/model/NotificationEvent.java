package com.moseph.discordmultiwebhook.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

/**
 * Represents a notification event with its type, variables and timestamp
 */
@Data
@AllArgsConstructor
public class NotificationEvent {
    private EventType type;
    private Map<String, String> variables;
    private long timestamp;
    
    /**
     * Checks if this event is related to another event (like same item in loot and collection log)
     */
    public boolean isRelatedTo(NotificationEvent other) {
        if (other == null) {
            return false;
        }
        
        // Check if both events involve the same item
        if (variables.containsKey("ITEM") && other.variables.containsKey("ITEM")) {
            return variables.get("ITEM").equals(other.variables.get("ITEM"));
        }
        
        // Check if one is a collection log event and one is a loot event with the same item
        if ((type == EventType.COLLECTION_LOG && other.type == EventType.LOOT) ||
            (type == EventType.LOOT && other.type == EventType.COLLECTION_LOG)) {
            String thisItem = variables.get("ITEM");
            String otherItem = other.variables.get("ITEM");
            return thisItem != null && otherItem != null && thisItem.equals(otherItem);
        }
        
        return false;
    }
} 