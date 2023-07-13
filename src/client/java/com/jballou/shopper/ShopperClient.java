package com.jballou.shopper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jballou.shopper.command.FindItem;
import com.jballou.shopper.command.Scan;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

public class ShopperClient implements ClientModInitializer
{

	public static final String MOD_ID = "shopper";
	public static final Logger LOG = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient()
	{
		ClientCommandRegistrationCallback.EVENT.register(Scan::listener);
		ClientCommandRegistrationCallback.EVENT.register(FindItem::listener);
	}
	
}
