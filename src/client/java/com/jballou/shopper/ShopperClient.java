package com.jballou.shopper;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jballou.shopper.command.FindItem;
import com.jballou.shopper.command.Scan;
import com.jballou.shopper.data.ShopCache;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;

public class ShopperClient implements ClientModInitializer
{
	public static final String MOD_ID = "shopper";
	public static final Logger LOG = LoggerFactory.getLogger(MOD_ID);
	public static final String CONFIG_PATH = String.format(Locale.ROOT, "%s/config/%s", MinecraftClient.getInstance().runDirectory, MOD_ID);

	@Override
	public void onInitializeClient()
	{
		ClientCommandRegistrationCallback.EVENT.register(Scan::listener);
		ClientCommandRegistrationCallback.EVENT.register(FindItem::listener);
		ClientPlayConnectionEvents.JOIN.register(ShopCache::joinListener);
		ClientPlayConnectionEvents.DISCONNECT.register(ShopCache::disconnectListener);
	}
	
}
