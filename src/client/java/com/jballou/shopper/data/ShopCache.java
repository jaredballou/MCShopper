package com.jballou.shopper.data;

import java.util.HashMap;

import com.jballou.shopper.ShopperClient;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

public final class ShopCache
{
	private static final HashMap<Identifier, ShopList> CACHE = new HashMap<>();

	public static void add(ShopSign sign)
	{
		ShopList list = CACHE.get(sign.dimension);
		if(list == null)
		{
			CACHE.put(sign.dimension, new ShopList());
			list = CACHE.get(sign.dimension);
			ShopperClient.LOG.info("Added new dimension {}", sign.dimension.toString());
		}

		list.add(sign);
	}

	public static void remove(ShopSign sign)
	{
		ShopList list = CACHE.get(sign.dimension);
		if(list != null)
		{
			list.remove(sign);
		}
	}

	public static Pair<ShopSign, ShopSign> findBestPrices(String itemName)
	{
		Pair<ShopSign, ShopSign> result = new Pair<>(null, null);
		for (ShopList list : CACHE.values())
		{
			Pair<ShopSign, ShopSign> best = list.findBestPrices(itemName);

			if(result.getLeft() == null)
			{
				//first iter
				result.setLeft(best.getLeft());
				result.setRight(best.getRight());
			}
			else
			{
				ShopSign bestBuyer = best.getLeft();
				ShopSign bestSeller = best.getRight();

				if(result.getLeft().buyPrice < bestBuyer.buyPrice)
				{
					result.setLeft(bestBuyer);
				}

				if(result.getRight().sellPrice > bestSeller.sellPrice)
				{
					result.setRight(bestSeller);
				}
			}
		}
		return result;
	}

	public static void joinListener(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client)
	{
		// loadJson();
	}

	public static void disconnectListener(ClientPlayNetworkHandler handler, MinecraftClient client)
	{
		// saveJson();
	}
}
