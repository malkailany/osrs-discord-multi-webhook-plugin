package com.moseph.discordmultiwebhook;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("discordmultiwebhook")
public interface DiscordMultiWebhookConfig extends Config
{
	@ConfigSection(
		name = "Global Settings",
		description = "Global webhook settings for all notifications",
		position = 0,
		closedByDefault = false
	)
	String globalSection = "globalSection";
	
	@ConfigItem(
		keyName = "globalWebhookUrl",
		name = "Global Webhook URL",
		description = "Discord webhook URL used for all events unless a custom one is specified",
		position = 1,
		section = globalSection
	)
	default String globalWebhookUrl()
	{
		return "";
	}
	
	@ConfigSection(
		name = "Notification Handling",
		description = "Settings for handling overlapping notifications",
		position = 5,
		closedByDefault = false
	)
	String notificationHandlingSection = "notificationHandlingSection";
	
	@ConfigItem(
		keyName = "smartNotifications",
		name = "Smart Notifications",
		description = "Intelligently combine related notifications to avoid spam",
		position = 6,
		section = notificationHandlingSection
	)
	default boolean smartNotifications()
	{
		return true;
	}
	
	@ConfigItem(
		keyName = "notificationDelay",
		name = "Notification Delay (seconds)",
		description = "Wait this many seconds before sending a notification to catch related events",
		position = 7,
		section = notificationHandlingSection
	)
	default int notificationDelay()
	{
		return 3;
	}
	
	@ConfigItem(
		keyName = "combinedMessageFormat",
		name = "Combined Message Format",
		description = "How to format messages when multiple events happen together. Use %EVENTS% to show all event types, %VALUE% for item value (if available).",
		position = 8,
		section = notificationHandlingSection
	)
	default String combinedMessageFormat()
	{
		return "%USERNAME% received %ITEM% worth %VALUE% GP! (%EVENTS%)";
	}
	
	@ConfigItem(
		keyName = "preferCollectionLog",
		name = "Prioritize Collection Log",
		description = "Highlight Collection Log events when they occur with other events",
		position = 9,
		section = notificationHandlingSection
	)
	default boolean preferCollectionLog()
	{
		return true;
	}
	
	// COLLECTION LOG SECTION
	@ConfigSection(
		name = "Collection Log",
		description = "Settings for Collection Log notifications",
		position = 10,
		closedByDefault = true
	)
	String collectionLogSection = "collectionLogSection";

	@ConfigItem(
		keyName = "includeCollectionLog",
		name = "Enable Notifications",
		description = "Enable notifications for Collection Log entries",
		position = 11,
		section = collectionLogSection
	)
	default boolean includeCollectionLog()
	{
		return true;
	}

	@ConfigItem(
		keyName = "useCustomCollectionLogWebhook",
		name = "Use Custom Webhook",
		description = "Use a custom webhook URL for Collection Log notifications instead of the global one",
		position = 12,
		section = collectionLogSection
	)
	default boolean useCustomCollectionLogWebhook()
	{
		return false;
	}

	@ConfigItem(
		keyName = "collectionLogWebhookUrl",
		name = "Custom Webhook URL",
		description = "Custom Discord webhook URL for Collection Log notifications",
		position = 13,
		section = collectionLogSection
	)
	default String collectionLogWebhookUrl()
	{
		return "";
	}

	@ConfigItem(
		keyName = "collectionLogSendImage",
		name = "Send Image",
		description = "Send a screenshot of your game along with the notification",
		position = 14,
		section = collectionLogSection
	)
	default boolean collectionLogSendImage()
	{
		return false;
	}

	@ConfigItem(
		keyName = "collectionLogMessage",
		name = "Custom Message",
		description = "Custom message sent with collection log notifications. Use %USERNAME%, %ITEM% as variables. Note: @everyone/@here mentions and markdown formatting will be escaped for security.",
		position = 15,
		section = collectionLogSection
	)
	default String collectionLogMessage()
	{
		return "%USERNAME% just added a new item to their collection log: %ITEM%!";
	}
	
	// PET SECTION
	@ConfigSection(
		name = "Pet",
		description = "Settings for Pet notifications",
		position = 20,
		closedByDefault = true
	)
	String petSection = "petSection";

	@ConfigItem(
		keyName = "includePet",
		name = "Enable Notifications",
		description = "Enable notifications for pet drops",
		position = 21,
		section = petSection
	)
	default boolean includePet()
	{
		return true;
	}

	@ConfigItem(
		keyName = "useCustomPetWebhook",
		name = "Use Custom Webhook",
		description = "Use a custom webhook URL for pet notifications instead of the global one",
		position = 22,
		section = petSection
	)
	default boolean useCustomPetWebhook()
	{
		return false;
	}

	@ConfigItem(
		keyName = "petWebhookUrl",
		name = "Custom Webhook URL",
		description = "Custom Discord webhook URL for pet notifications",
		position = 23,
		section = petSection
	)
	default String petWebhookUrl()
	{
		return "";
	}

	@ConfigItem(
		keyName = "petSendImage",
		name = "Send Image",
		description = "Send a screenshot of your game along with the notification",
		position = 24,
		section = petSection
	)
	default boolean petSendImage()
	{
		return false;
	}

	@ConfigItem(
		keyName = "petMessage",
		name = "Custom Message",
		description = "Custom message sent with pet notifications. Use %USERNAME%, %PET% as variables. Note: @everyone/@here mentions and markdown formatting will be escaped for security.",
		position = 25,
		section = petSection
	)
	default String petMessage()
	{
		return "%USERNAME% just received a new pet: %PET%!";
	}

	// LEVEL UPS SECTION
	@ConfigSection(
		name = "Level Ups",
		description = "Settings for Level Up notifications",
		position = 30,
		closedByDefault = true
	)
	String levelUpSection = "levelUpSection";

	@ConfigItem(
		keyName = "includeLevelUps",
		name = "Enable Notifications",
		description = "Enable notifications for level up events",
		position = 31,
		section = levelUpSection
	)
	default boolean includeLevelUps()
	{
		return false;
	}

	@ConfigItem(
		keyName = "useCustomLevelUpWebhook",
		name = "Use Custom Webhook",
		description = "Use a custom webhook URL for level up notifications instead of the global one",
		position = 32,
		section = levelUpSection
	)
	default boolean useCustomLevelUpWebhook()
	{
		return false;
	}

	@ConfigItem(
		keyName = "levelUpWebhookUrl",
		name = "Custom Webhook URL",
		description = "Custom Discord webhook URL for level up notifications",
		position = 33,
		section = levelUpSection
	)
	default String levelUpWebhookUrl()
	{
		return "";
	}

	@ConfigItem(
		keyName = "levelUpSendImage",
		name = "Send Image",
		description = "Send a screenshot of your game along with the notification",
		position = 34,
		section = levelUpSection
	)
	default boolean levelUpSendImage()
	{
		return false;
	}

	@ConfigItem(
		keyName = "levelUpMessage",
		name = "Custom Message",
		description = "Custom message sent with level up notifications. Use %USERNAME%, %SKILL%, %LEVEL% as variables.",
		position = 35,
		section = levelUpSection
	)
	default String levelUpMessage()
	{
		return "%USERNAME% just achieved level %LEVEL% in %SKILL%!";
	}

	// LOOT SECTION
	@ConfigSection(
		name = "Loot",
		description = "Settings for Loot notifications",
		position = 40,
		closedByDefault = true
	)
	String lootSection = "lootSection";

	@ConfigItem(
		keyName = "includeLoot",
		name = "Enable Notifications",
		description = "Enable notifications for valuable loot drops",
		position = 41,
		section = lootSection
	)
	default boolean includeLoot()
	{
		return false;
	}

	@ConfigItem(
		keyName = "lootValueThreshold",
		name = "Minimum Value (GP)",
		description = "Only send notifications for loot drops worth at least this much. Set to 0 to notify for all valuable drops.",
		position = 42,
		section = lootSection
	)
	default int lootValueThreshold()
	{
		return 1000000;
	}

	@ConfigItem(
		keyName = "useCustomLootWebhook",
		name = "Use Custom Webhook",
		description = "Use a custom webhook URL for loot notifications instead of the global one",
		position = 43,
		section = lootSection
	)
	default boolean useCustomLootWebhook()
	{
		return false;
	}

	@ConfigItem(
		keyName = "lootWebhookUrl",
		name = "Custom Webhook URL",
		description = "Custom Discord webhook URL for loot notifications",
		position = 44,
		section = lootSection
	)
	default String lootWebhookUrl()
	{
		return "";
	}

	@ConfigItem(
		keyName = "lootSendImage",
		name = "Send Image",
		description = "Send a screenshot of your game along with the notification",
		position = 45,
		section = lootSection
	)
	default boolean lootSendImage()
	{
		return false;
	}

	@ConfigItem(
		keyName = "lootMessage",
		name = "Custom Message",
		description = "Custom message sent with loot notifications. Use %USERNAME%, %ITEM%, %VALUE% as variables.",
		position = 46,
		section = lootSection
	)
	default String lootMessage()
	{
		return "%USERNAME% just received a valuable drop: %ITEM% worth %VALUE% GP!";
	}

	// DEATH SECTION
	@ConfigSection(
		name = "Death",
		description = "Settings for Death notifications",
		position = 50,
		closedByDefault = true
	)
	String deathSection = "deathSection";

	@ConfigItem(
		keyName = "includeDeath",
		name = "Enable Notifications",
		description = "Enable notifications for player deaths",
		position = 51,
		section = deathSection
	)
	default boolean includeDeath()
	{
		return false;
	}

	@ConfigItem(
		keyName = "useCustomDeathWebhook",
		name = "Use Custom Webhook",
		description = "Use a custom webhook URL for death notifications instead of the global one",
		position = 52,
		section = deathSection
	)
	default boolean useCustomDeathWebhook()
	{
		return false;
	}

	@ConfigItem(
		keyName = "deathWebhookUrl",
		name = "Custom Webhook URL",
		description = "Custom Discord webhook URL for death notifications",
		position = 53,
		section = deathSection
	)
	default String deathWebhookUrl()
	{
		return "";
	}

	@ConfigItem(
		keyName = "deathSendImage",
		name = "Send Image",
		description = "Send a screenshot of your game along with the notification",
		position = 54,
		section = deathSection
	)
	default boolean deathSendImage()
	{
		return false;
	}

	@ConfigItem(
		keyName = "deathMessage",
		name = "Custom Message",
		description = "Custom message sent with death notifications. Use %USERNAME% as variables.",
		position = 55,
		section = deathSection
	)
	default String deathMessage()
	{
		return "%USERNAME% has died";
	}

	// QUESTS SECTION
	@ConfigSection(
		name = "Quests",
		description = "Settings for Quest notifications",
		position = 60,
		closedByDefault = true
	)
	String questSection = "questSection";

	@ConfigItem(
		keyName = "includeQuest",
		name = "Enable Notifications",
		description = "Enable notifications for quest completions",
		position = 61,
		section = questSection
	)
	default boolean includeQuest()
	{
		return false;
	}

	@ConfigItem(
		keyName = "useCustomQuestWebhook",
		name = "Use Custom Webhook",
		description = "Use a custom webhook URL for quest notifications instead of the global one",
		position = 62,
		section = questSection
	)
	default boolean useCustomQuestWebhook()
	{
		return false;
	}

	@ConfigItem(
		keyName = "questWebhookUrl",
		name = "Custom Webhook URL",
		description = "Custom Discord webhook URL for quest notifications",
		position = 63,
		section = questSection
	)
	default String questWebhookUrl()
	{
		return "";
	}

	@ConfigItem(
		keyName = "questSendImage",
		name = "Send Image",
		description = "Send a screenshot of your game along with the notification",
		position = 64,
		section = questSection
	)
	default boolean questSendImage()
	{
		return false;
	}

	@ConfigItem(
		keyName = "questMessage",
		name = "Custom Message",
		description = "Custom message sent with quest notifications. Use %USERNAME%, %QUEST% as variables.",
		position = 65,
		section = questSection
	)
	default String questMessage()
	{
		return "%USERNAME% just completed the quest: %QUEST%!";
	}

	// CLUE SCROLLS SECTION
	@ConfigSection(
		name = "Clue Scrolls",
		description = "Settings for Clue Scroll notifications",
		position = 70,
		closedByDefault = true
	)
	String clueScrollSection = "clueScrollSection";

	@ConfigItem(
		keyName = "includeClueScroll",
		name = "Enable Notifications",
		description = "Enable notifications for clue scroll completions",
		position = 71,
		section = clueScrollSection
	)
	default boolean includeClueScroll()
	{
		return false;
	}

	@ConfigItem(
		keyName = "useCustomClueScrollWebhook",
		name = "Use Custom Webhook",
		description = "Use a custom webhook URL for clue scroll notifications instead of the global one",
		position = 72,
		section = clueScrollSection
	)
	default boolean useCustomClueScrollWebhook()
	{
		return false;
	}

	@ConfigItem(
		keyName = "clueScrollWebhookUrl",
		name = "Custom Webhook URL",
		description = "Custom Discord webhook URL for clue scroll notifications",
		position = 73,
		section = clueScrollSection
	)
	default String clueScrollWebhookUrl()
	{
		return "";
	}

	@ConfigItem(
		keyName = "clueScrollSendImage",
		name = "Send Image",
		description = "Send a screenshot of your game along with the notification",
		position = 74,
		section = clueScrollSection
	)
	default boolean clueScrollSendImage()
	{
		return false;
	}

	@ConfigItem(
		keyName = "clueScrollMessage",
		name = "Custom Message",
		description = "Custom message sent with clue scroll notifications. Use %USERNAME%, %CLUETYPE%, %REWARD% as variables.",
		position = 75,
		section = clueScrollSection
	)
	default String clueScrollMessage()
	{
		return "%USERNAME% just completed a %CLUETYPE% clue scroll and received: %REWARD%!";
	}
} 