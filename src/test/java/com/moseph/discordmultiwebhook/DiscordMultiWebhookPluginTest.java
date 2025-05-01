package com.moseph.discordmultiwebhook;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class DiscordMultiWebhookPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(DiscordMultiWebhookPlugin.class);
		RuneLite.main(args);
	}
}