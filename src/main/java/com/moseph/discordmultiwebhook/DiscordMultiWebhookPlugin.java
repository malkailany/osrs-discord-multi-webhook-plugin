package com.moseph.discordmultiwebhook;

import com.google.inject.Provides;
import com.moseph.discordmultiwebhook.model.EventType;
import com.moseph.discordmultiwebhook.model.NotificationEvent;
import com.moseph.discordmultiwebhook.service.DiscordService;
import com.moseph.discordmultiwebhook.service.NotificationService;
import com.moseph.discordmultiwebhook.simulation.EventSimulator;
import com.moseph.discordmultiwebhook.utils.DiscordUtils;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ScriptID;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.util.Text;
import okhttp3.OkHttpClient;

@Slf4j
@PluginDescriptor(
	name = "Discord Multi Webhook Notifier",
	description = "Sends notifications to multiple Discord webhooks",
	tags = {"discord", "webhook", "notification", "collection", "log", "pet", "level", "drop"}
)
public class DiscordMultiWebhookPlugin extends Plugin
{
	private static final Pattern COLLECTION_LOG_ITEM_PATTERN = Pattern.compile("New item added to your collection log: (.*)");
	private static final Pattern LEVEL_UP_PATTERN = Pattern.compile(".*Your ([a-zA-Z]+) (?:level is|are now) (\\d+)\\.");
	private static final Pattern PET_PATTERN = Pattern.compile("You have a funny feeling like you.*");
	private static final Pattern VALUABLE_DROP_PATTERN = Pattern.compile(".*Valuable drop:.*");
	private static final Pattern VALUABLE_DROP_VALUE_PATTERN = Pattern.compile(".*Valuable drop: .* \\((.*) coins\\)");
	private static final Pattern QUEST_COMPLETE_PATTERN = Pattern.compile("You have completed.*[Qq]uest(?:!|\\.)(?: \\(\\d+ Quest Points?\\))?");
	private static final Pattern CLUE_SCROLL_REWARD_PATTERN = Pattern.compile("You have completed (\\d+) ([\\w\\s]+) Treasure Trails.");
	private static final Pattern DEATH_PATTERN = Pattern.compile("Oh dear, you are dead!");
	
	// Command strings
	private static final String WEBHOOK_TEST_COMMAND = "dwtest";
	
	@Inject
	private Client client;

	@Inject
	private DiscordMultiWebhookConfig config;

	@Inject
	private OkHttpClient okHttpClient;
	
	@Inject
	private DrawManager drawManager;
	
	@Inject
	private ScheduledExecutorService executorService;
	
	@Inject
	private ClientThread clientThread;
	
	// Services
	private DiscordService discordService;
	private NotificationService notificationService;
	private EventSimulator eventSimulator;
	
	// Queue for pending notifications
	private final List<NotificationEvent> pendingNotifications = new ArrayList<>();
	private boolean processingNotifications = false;
	
	// Tracked notification variables
	private String deathLocation = null;

	@Provides
	DiscordMultiWebhookConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DiscordMultiWebhookConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		log.info("Discord Webhook plugin started");
		
		// Initialize services
		discordService = new DiscordService(okHttpClient);
		notificationService = new NotificationService(config);
		eventSimulator = new EventSimulator(client, clientThread, executorService, this);
		
		// Validate global webhook URL
		String globalUrl = DiscordUtils.validateWebhookUrl(config.globalWebhookUrl());
		log.info("Global webhook URL configured: {}", !globalUrl.isEmpty() ? "Yes (masked for security)" : "No");
		
		// Log which notification types are enabled
		log.info("Notification types enabled: Collection Log={}, Pet={}, Level Ups={}, Loot={}, Death={}, Quest={}, Clue Scroll={}",
			config.includeCollectionLog(), config.includePet(), config.includeLevelUps(), 
			config.includeLoot(), config.includeDeath(), config.includeQuest(), config.includeClueScroll());
		
		// Log image configuration
		log.info("Images will be sent as regular attachments (not embedded)");
		
		log.info("Use ::dwtest command to test Discord webhook notifications");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Discord Webhook plugin stopped");
		pendingNotifications.clear();
		deathLocation = null;
	}
	
	@Subscribe
	public void onCommandExecuted(CommandExecuted commandExecuted)
	{
		// Check if it's our command
		if (commandExecuted.getCommand().equals(WEBHOOK_TEST_COMMAND))
		{
			log.info("Discord webhook command executed with arguments: {}", Arrays.toString(commandExecuted.getArguments()));
			
			String[] args = commandExecuted.getArguments();
			if (args.length == 0)
			{
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", 
					"Discord webhook test commands: ::dwtest col | pet | lvl | loot | death | quest | clue | coldrp | all [img]", null);
				return;
			}
			
			String testType = args[0].toLowerCase();
			boolean withImage = args.length > 1 && args[1].equalsIgnoreCase("img");
			
			switch (testType)
			{
				case "col":
					testCollectionLog(withImage);
					break;
				case "pet":
					testPet(withImage);
					break;
				case "lvl":
					testLevelUp(withImage);
					break;
				case "loot":
					testLoot(withImage);
					break;
				case "death":
					testDeath(withImage);
					break;
				case "quest":
					testQuest(withImage);
					break;
				case "clue":
					testClueScroll(withImage);
					break;
				case "coldrp":
					testCollectionAndLoot(withImage);
					break;
				case "all":
					testAll(withImage);
					break;
				default:
					client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", 
						"Unknown test type: " + testType + ". Available types: col, pet, lvl, loot, death, quest, clue, coldrp, all", null);
			}
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		if (chatMessage.getType() != ChatMessageType.GAMEMESSAGE && 
			chatMessage.getType() != ChatMessageType.SPAM &&
			chatMessage.getType() != ChatMessageType.ITEM_EXAMINE)
		{
			return;
		}

		String message = chatMessage.getMessage();
		
		// Collection Log detection
		Matcher collectionLogMatcher = COLLECTION_LOG_ITEM_PATTERN.matcher(message);
		if (collectionLogMatcher.matches() && config.includeCollectionLog())
		{
			String item = collectionLogMatcher.group(1);
			
			Map<String, String> variables = new HashMap<>();
			variables.put("ITEM", item);
			variables.put("USERNAME", getPlayerName());
			
			log.info("Collection log match found: {}", item);
			queueNotification(EventType.COLLECTION_LOG, variables);
		}
		
		// Level up detection
		Matcher levelUpMatcher = LEVEL_UP_PATTERN.matcher(message);
		if (levelUpMatcher.matches() && config.includeLevelUps())
		{
			String skill = levelUpMatcher.group(1);
			String level = levelUpMatcher.group(2);
			
			Map<String, String> variables = new HashMap<>();
			variables.put("SKILL", skill);
			variables.put("LEVEL", level);
			variables.put("USERNAME", getPlayerName());
			
			log.info("Level up match found: {} level {}", skill, level);
			queueNotification(EventType.LEVEL_UP, variables);
		}
		
		// Pet detection
		Matcher petMatcher = PET_PATTERN.matcher(message);
		if (petMatcher.matches() && config.includePet())
		{
			// Try to determine the pet type based on the message
			String pet = "new pet";
			if (message.contains("following you")) {
				pet = "a follower pet";
			}
			
			Map<String, String> variables = new HashMap<>();
			variables.put("PET", pet);
			variables.put("USERNAME", getPlayerName());
			
			log.info("Pet match found!");
			queueNotification(EventType.PET, variables);
		}
		
		// Valuable drop detection
		Matcher valuableDropMatcher = VALUABLE_DROP_PATTERN.matcher(message);
		if (valuableDropMatcher.matches() && config.includeLoot())
		{
			// Extract item name and value
			String item = message.substring(message.indexOf(":") + 1).trim();
			if (item.startsWith("<col=")) {
				item = Text.removeTags(item);
			}
			
			// Try to parse the value
			int valueGp = 0;
			Matcher valueMatcher = VALUABLE_DROP_VALUE_PATTERN.matcher(message);
			if (valueMatcher.matches()) {
				String valueStr = valueMatcher.group(1).replace(",", "");
				try {
					valueGp = Integer.parseInt(valueStr);
					
					// Remove the value part from the item name
					item = item.substring(0, item.lastIndexOf(" ("));
				} catch (NumberFormatException e) {
					log.warn("Failed to parse drop value: {}", valueStr);
				}
			}
			
			// Check if the value meets the threshold
			int threshold = config.lootValueThreshold();
			if (threshold > 0 && valueGp < threshold) {
				log.debug("Skipping loot notification as value {} is below threshold {}", valueGp, threshold);
				return;
			}

			Map<String, String> variables = new HashMap<>();
			variables.put("ITEM", item);
			variables.put("USERNAME", getPlayerName());
			variables.put("VALUE", String.format("%,d", valueGp)); // Format with commas
			
			log.info("Valuable drop match found: {} worth {} GP", item, valueGp);
			queueNotification(EventType.LOOT, variables);
		}
		
		// Quest completion detection
		Matcher questMatcher = QUEST_COMPLETE_PATTERN.matcher(message);
		if (questMatcher.matches() && config.includeQuest())
		{
			// Extract quest name from "You have completed [quest]"
			String quest = message.replace("You have completed", "").replace("Quest!", "").replace("Quest.", "").trim();
			// Remove quest points if present
			if (quest.contains("(")) {
				quest = quest.substring(0, quest.indexOf("(")).trim();
			}
			
			Map<String, String> variables = new HashMap<>();
			variables.put("QUEST", quest);
			variables.put("USERNAME", getPlayerName());
			
			log.info("Quest completion match found: {}", quest);
			queueNotification(EventType.QUEST, variables);
		}
		
		// Clue scroll completion detection
		Matcher clueMatcher = CLUE_SCROLL_REWARD_PATTERN.matcher(message);
		if (clueMatcher.matches() && config.includeClueScroll())
		{
			String count = clueMatcher.group(1);
			String type = clueMatcher.group(2);
			
			Map<String, String> variables = new HashMap<>();
			variables.put("CLUETYPE", type);
			variables.put("COUNT", count);
			variables.put("USERNAME", getPlayerName());
			variables.put("REWARD", "treasure"); // Will be updated when rewards are shown
			
			log.info("Clue scroll match found: {} {}", count, type);
			queueNotification(EventType.CLUE_SCROLL, variables);
		}
		
		// Death detection
		Matcher deathMatcher = DEATH_PATTERN.matcher(message);
		if (deathMatcher.matches() && config.includeDeath())
		{
			deathLocation = "Unknown";
			if (client.getLocalPlayer() != null) {
				deathLocation = "(" + client.getLocalPlayer().getWorldLocation().getX() + 
					", " + client.getLocalPlayer().getWorldLocation().getY() + 
					", " + client.getLocalPlayer().getWorldLocation().getPlane() + ")";
			}
			
			Map<String, String> variables = new HashMap<>();
			variables.put("LOCATION", deathLocation);
			variables.put("CAUSE", "Unknown");
			variables.put("USERNAME", getPlayerName());
			
			log.info("Death match found at location: {}", deathLocation);
			queueNotification(EventType.DEATH, variables);
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN)
		{
			// Reset tracked states when going to login screen
			pendingNotifications.clear();
			deathLocation = null;
		}
	}

	@Subscribe
	public void onScriptPreFired(ScriptPreFired scriptPreFired)
	{
		// This is for notifications that use the notification system
		if (scriptPreFired.getScriptId() != ScriptID.NOTIFICATION_START)
		{
			return;
		}
	}
	
	/**
	 * Queues a notification to be sent
	 */
	public void queueNotification(EventType type, Map<String, String> variables) {
		NotificationEvent event = new NotificationEvent(type, variables, System.currentTimeMillis());
		pendingNotifications.add(event);
		
		log.debug("Queued {} notification. Total notifications in queue: {}", type.getDisplayName(), pendingNotifications.size());
		log.debug("Notification variables: {}", variables);
		
		// Only schedule processing if not already processing
		if (!processingNotifications) {
			log.debug("Scheduling notification processing in {} seconds", config.notificationDelay());
			processingNotifications = true;
			executorService.schedule(this::processNotifications, config.notificationDelay(), TimeUnit.SECONDS);
		}
	}
	
	/**
	 * Processes queued notifications
	 */
	private void processNotifications() {
		log.debug("Processing {} notifications", pendingNotifications.size());
		
		if (pendingNotifications.isEmpty()) {
			log.debug("No notifications to process");
			processingNotifications = false;
			return;
		}
		
		// Group related notifications
		List<List<NotificationEvent>> groupedNotifications = notificationService.groupRelatedNotifications(pendingNotifications);
		
		// Process each group
		for (List<NotificationEvent> group : groupedNotifications) {
			if (config.smartNotifications() && group.size() > 1) {
				// Smart notifications enabled and we have multiple related events
				log.debug("Sending combined notification for {} events", group.size());
				sendCombinedNotification(group);
			} else {
				// Send individual notifications
				log.debug("Sending {} individual notifications", group.size());
				for (NotificationEvent event : group) {
					sendNotification(event);
				}
			}
		}
		
		// Clear processed notifications
		pendingNotifications.clear();
		processingNotifications = false;
	}
	
	/**
	 * Sends a combined notification for related events
	 */
	private void sendCombinedNotification(List<NotificationEvent> events) {
		if (events.isEmpty()) {
			return;
		}
		
		// Prepare variables for the combined message
		Map<String, String> variables = new HashMap<>(events.get(0).getVariables());
		
		// Determine which event to prioritize
		EventType primaryType = notificationService.determinePrimaryEventType(events);
		
		// Format the message
		String message = notificationService.formatCombinedMessage(events, variables);
		
		// Send a webhook with the combined message
		String webhookUrl = notificationService.getWebhookUrl(primaryType);
		if (webhookUrl.isEmpty()) {
			return;
		}
		
		// Determine if we should include an image
		boolean sendImage = notificationService.shouldSendImage(primaryType);
		
		if (sendImage) {
			captureScreenshot(image -> 
				discordService.sendWebhookWithImage(webhookUrl, primaryType.getAvatarUrl(), message, image));
		} else {
			discordService.sendWebhook(webhookUrl, primaryType.getAvatarUrl(), message);
		}
	}
	
	/**
	 * Sends a single notification
	 */
	private void sendNotification(NotificationEvent event) {
		EventType type = event.getType();
		String message = notificationService.formatMessage(type, event.getVariables());
		String webhookUrl = notificationService.getWebhookUrl(type);
		
		log.debug("Preparing to send {} notification", type.getDisplayName());
		
		if (webhookUrl.isEmpty()) {
			log.warn("Cannot send {} notification - webhook URL is empty", type.getDisplayName());
			return;
		}
		
		boolean sendImage = notificationService.shouldSendImage(type);
		log.debug("Sending with image: {}", sendImage);
		
		if (sendImage) {
			captureScreenshot(image -> 
				discordService.sendWebhookWithImage(webhookUrl, type.getAvatarUrl(), message, image));
		} else {
			discordService.sendWebhook(webhookUrl, type.getAvatarUrl(), message);
		}
	}
	
	/**
	 * Captures a screenshot and passes it to the provided consumer
	 */
	public void captureScreenshot(Consumer<BufferedImage> imageConsumer) {
		log.debug("Capturing screenshot for Discord webhook...");
		clientThread.invokeLater(() -> {
			if (client.getGameState() != GameState.LOGGED_IN) {
				// Don't try to take screenshots if not logged in
				log.debug("Client not logged in, cannot capture screenshot");
				imageConsumer.accept(null);
				return;
			}
			
			drawManager.requestNextFrameListener(image -> {
				try {
					log.debug("Screenshot captured");
					BufferedImage bufferedImage = (BufferedImage) image;
					imageConsumer.accept(bufferedImage);
				} catch (Exception e) {
					log.warn("Error capturing screenshot for Discord webhook", e);
					imageConsumer.accept(null);
				}
			});
		});
	}
	
	/**
	 * Gets the current player's name
	 */
	private String getPlayerName() {
		if (client.getLocalPlayer() == null) {
			return "Unknown";
		}
		return client.getLocalPlayer().getName();
	}
	
	/**
	 * Test handlers for the commands
	 */
	private void testCollectionLog(boolean withImage) {
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Testing collection log webhook notification", null);
		eventSimulator.simulateCollectionLogEvent(withImage);
	}
	
	private void testPet(boolean withImage) {
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Testing pet webhook notification", null);
		eventSimulator.simulatePetEvent(withImage);
	}
	
	private void testLevelUp(boolean withImage) {
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Testing level up webhook notification", null);
		eventSimulator.simulateLevelUpEvent(withImage);
	}
	
	private void testLoot(boolean withImage) {
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Testing loot webhook notification", null);
		eventSimulator.simulateLootEvent(withImage);
	}
	
	private void testDeath(boolean withImage) {
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Testing death webhook notification", null);
		eventSimulator.simulateDeathEvent(withImage);
	}
	
	private void testQuest(boolean withImage) {
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Testing quest webhook notification", null);
		eventSimulator.simulateQuestEvent(withImage);
	}
	
	private void testClueScroll(boolean withImage) {
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Testing clue scroll webhook notification", null);
		eventSimulator.simulateClueEvent(withImage);
	}
	
	private void testAll(boolean withImage) {
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Testing all webhook notifications with 3 second delays", null);
		eventSimulator.simulateAllEvents(withImage);
	}
	
	private void testCollectionAndLoot(boolean withImage) {
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Testing collection log + loot webhook notification for same item", null);
		eventSimulator.simulateCollectionAndLootEvent(withImage);
	}
} 