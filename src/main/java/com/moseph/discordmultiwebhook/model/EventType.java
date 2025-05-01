package com.moseph.discordmultiwebhook.model;

/**
 * Represents different types of in-game events that can trigger notifications
 */
public enum EventType {
    COLLECTION_LOG("Collection Log", "https://oldschool.runescape.wiki/images/Collection_log.png?7b468"),
    PET("Pet", "https://oldschool.runescape.wiki/images/Pet_rock.png"),
    LEVEL_UP("Level Up", "https://oldschool.runescape.wiki/images/Stats_icon.png"),
    LOOT("Valuable Loot", "https://oldschool.runescape.wiki/images/Casket.png"),
    DEATH("Death", "https://oldschool.runescape.wiki/images/Death.png"),
    QUEST("Quest", "https://oldschool.runescape.wiki/images/Quest_point_cape.png"),
    CLUE_SCROLL("Clue Scroll", "https://oldschool.runescape.wiki/images/Clue_scroll_(master).png");
    
    private final String displayName;
    private final String avatarUrl;
    
    EventType(String displayName, String avatarUrl) {
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
} 