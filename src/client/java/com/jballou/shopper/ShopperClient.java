package com.jballou.shopper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jballou.shopper.command.FindItem;
import com.jballou.shopper.command.Scan;
import com.jballou.shopper.data.ShopCache;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class ShopperClient implements ClientModInitializer
{

	public static final String MOD_ID = "shopper";
	public static final Logger LOG = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient()
	{
		ClientCommandRegistrationCallback.EVENT.register(Scan::listener);
		ClientCommandRegistrationCallback.EVENT.register(FindItem::listener);
		ClientPlayConnectionEvents.JOIN.register(ShopCache::joinListener);
		ClientPlayConnectionEvents.DISCONNECT.register(ShopCache::disconnectListener);
	}
	
}
