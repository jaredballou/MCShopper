package com.jballou.shopper.command;

import java.util.Locale;

import com.jballou.shopper.data.ShopCache;
import com.jballou.shopper.data.ShopSign;
import com.jballou.shopper.util.Msg;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.util.Pair;

public final class FindItem
{
	public static void listener(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess)
	{
		dispatcher.register(ClientCommandManager.literal("finditem")
		.then(ClientCommandManager.argument("itemName", StringArgumentType.string())
			.executes(context ->
			{
				findPrices(context);
				return 1;
			}))
		// .executes(context ->
		// {
		// 	context.getSource().sendFeedback(Text.literal("Finding item..."));
		// 	return 1;
		// })
		);
	}

	private static void findPrices(CommandContext<FabricClientCommandSource> context)
	{
		String name = StringArgumentType.getString(context, "itemName");
		Pair<ShopSign, ShopSign> signs = ShopCache.findBestPrices(name.toLowerCase(Locale.ROOT));

		ShopSign buyer = signs.getLeft();
		ShopSign seller = signs.getRight();

		if(buyer != null)
		{
			Msg.itemSearchResult(context, name, buyer.pos, buyer.buyPrice, true);
		}
		else
		{
			Msg.itemSearchFail(context, name, true);
		}

		if(seller != null)
		{
			Msg.itemSearchResult(context, name, seller.pos, seller.sellPrice, false);
		}
		else
		{
			Msg.itemSearchFail(context, name, false);
		}
	}
}
