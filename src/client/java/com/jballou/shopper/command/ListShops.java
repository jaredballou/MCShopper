package com.jballou.shopper.command;

import java.util.HashSet;
import java.util.Set;

import com.jballou.shopper.data.ShopCache;
import com.jballou.shopper.data.ShopSign;
import com.jballou.shopper.util.Msg;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;

public final class ListShops
{
	private ListShops() {}

	public static void listener(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess)
	{
		dispatcher.register(ClientCommandManager.literal("listshops")
		.then(ClientCommandManager.argument("itemName", StringArgumentType.string())
			.executes(context ->
			{
				listShops(context);
				return 1;
			})));
	}

	private static void listShops(CommandContext<FabricClientCommandSource> context)
	{
		String itemName = StringArgumentType.getString(context, "itemName");
		Set<ShopSign> signs = new HashSet<>();
		ShopCache.populateShopSet(signs, itemName);

		if(signs.size() == 0)
		{
			Msg.info(context, "No shops found");
		}
		else
		{
			Msg.shopList(context, itemName, signs);
		}
	}
}
