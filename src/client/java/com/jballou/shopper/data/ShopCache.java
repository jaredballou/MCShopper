package com.jballou.shopper.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;

public final class ShopCache
{
	private static class ItemList
	{
		public final List<ShopSign> signs = new ArrayList<>();

		public ShopSign add(ShopSign sign)
		{
			signs.add(sign);
			return sign;
		}

		public ShopSign remove(ShopSign sign)
		{
			signs.remove(sign);
			return sign;
		}
	}

	private static Map<BlockPos, ShopSign> SIGNS = new HashMap<>();
	private static Map<String, ItemList> ITEMS = new HashMap<>();

	private ShopCache() {}

	public static void cache(ShopSign sign)
	{
		ShopSign oldSign = SIGNS.get(sign.pos);
		if(oldSign != null && !sign.compare(oldSign))
		{
			SIGNS.replace(sign.pos, sign);
			ITEMS.get(oldSign.getComparableItemName()).remove(oldSign);
		}

		String name = sign.getComparableItemName();
		if(!ITEMS.containsKey(name))
		{
			ITEMS.put(name, new ItemList());
		}
		ITEMS.get(name).add(sign);
	}

	public static Pair<ShopSign, ShopSign> findBestPrices(String itemName)
	{
		Pair<ShopSign, ShopSign> result = new Pair<>(null, null);
		ItemList list = ITEMS.get(itemName.toLowerCase(Locale.ROOT));
		
		if(list == null || list.signs.size() == 0)
		{
			return result;
		}

		ShopSign bestBuyer = list.signs.get(0);
		ShopSign bestSeller = list.signs.get(0);
		for(ShopSign sign : list.signs)
		{
			bestBuyer = sign.buyPrice > bestBuyer.buyPrice ? sign : bestBuyer;
			bestSeller = sign.sellPrice < bestSeller.sellPrice ? sign : bestSeller;
		}

		result.setLeft(bestBuyer);
		result.setRight(bestSeller);
		return result;
	}

	public static void joinListener(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client)
	{
		loadJson();
	}

	public static void disconnectListener(ClientPlayNetworkHandler handler, MinecraftClient client)
	{
		saveJson();
	}

	private static void loadJson()
	{

	}

	private static void saveJson()
	{

	}
}
