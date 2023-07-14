package com.jballou.shopper.data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.jballou.shopper.ShopperClient;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.WorldSavePath;

public final class ShopCache
{
	private static String REGEX_STR = ".*[\\\\/](.+)[\\\\/].+\\.dat";
	private static final Pattern REGEX_PATTERN = Pattern.compile(REGEX_STR);
	
	private static final HashMap<Identifier, ShopList> CACHE = new HashMap<>();

	private static class CacheSerializer implements JsonSerializer<ShopCache>
	{

		@Override
		public JsonElement serialize(ShopCache cache, Type typeOfSrc, JsonSerializationContext context)
		{
			JsonArray worlds = new JsonArray();
			
			for(Map.Entry<Identifier, ShopList> entry : CACHE.entrySet())
			{
				JsonObject world = new JsonObject();
				world.addProperty("id", entry.getKey().toString());
				world.add("signs", entry.getValue().toJson());
				worlds.add(world);
			}

			return worlds;
		}

	}

	// private static class CacheDeserializer

	private ShopCache() {}

	public static void add(ShopSign sign)
	{
		ShopList list = CACHE.get(sign.dimension);
		if(list == null)
		{
			CACHE.put(sign.dimension, new ShopList());
			list = CACHE.get(sign.dimension);
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
		String fname = getFileNameForLevel(client);
	}

	public static void disconnectListener(ClientPlayNetworkHandler handler, MinecraftClient client)
	{
		String fname = getFileNameForLevel(client);
		client.execute(() ->
		{
			saveToJson(fname);
		});
	}

	private static void loadFromJson(String fname)
	{

	}

	private static void saveToJson(String fname)
	{
		Gson gson = new GsonBuilder()
			.setPrettyPrinting()
			.registerTypeAdapter(ShopCache.class, new CacheSerializer())
			.create();

		String path = String.format(Locale.ROOT, "%s/shops.%s.json", ShopperClient.CONFIG_PATH, fname);
		ShopperClient.LOG.info(path);
		ShopperClient.LOG.info(fname);
		try 
		{
			if(new File(ShopperClient.CONFIG_PATH).mkdirs())
			{
				ShopperClient.LOG.info("Created dir {}", ShopperClient.CONFIG_PATH);
			}

			FileWriter writer = new FileWriter(path);
			
			// new is a bit hacky here, not sure how to serialise a static class
			gson.toJson(new ShopCache(), writer);
			writer.flush();
			ShopperClient.LOG.info("Saved Shopper cache to {}", path);
			writer.close();
		}
		catch (IOException | JsonIOException e)
		{
			ShopperClient.LOG.warn("Could not save cache {}!", path);
		}
	}

	private static String getFileNameForLevel(MinecraftClient client)
	{
		if(client.isConnectedToLocalServer())
		{
			// /run/media/crocomire/fabric/shopper/run/saves/New World (1)/level.dat
			Path p = client.getServer().getSavePath(WorldSavePath.LEVEL_DAT);
			Matcher matcher = REGEX_PATTERN.matcher(p.toString());
			if(matcher.matches())
			{
				return matcher.group(1);
			}
		}
		
		return client.getCurrentServerEntry().address;
	}
}
