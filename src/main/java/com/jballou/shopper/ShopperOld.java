package com.jballou.shopper;

import com.google.gson.*;

import com.jballou.shopper.event.SignUpdateCallback;

import com.jballou.shopper.storage.Stores;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import java.lang.reflect.*;
import java.io.*;
import java.util.*;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Iterator;

import com.mojang.brigadier.arguments.*;
import com.sun.jdi.connect.Connector;
import net.fabricmc.api.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.*;

import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.client.*;
import net.minecraft.client.network.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.*;
import net.minecraft.client.util.math.*;
import net.minecraft.entity.*;
import net.minecraft.entity.decoration.*;
import net.minecraft.entity.projectile.*;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerMetadata;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.*;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;


import net.minecraft.world.dimension.DimensionType;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/**
 * Shopper class
 * This mod looks at entities to find signs, and if the text rows match a shop format, then process the data and add
 * it to the global list. Also emits a JSON array of those objects.
 * TODO: Load JSON data into list at connection
 * TODO: Store blocks indexed by world and server, as there is currently only one list which is not great.
 * TODO: allow searching at arbitrary points, allowing updates of shop districts from any location
 */
public class ShopperOld implements ClientModInitializer {

	public static final String MOD_ID = "shopper";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	public static Hashtable<Integer, ShopSign> shopSigns = new Hashtable<Integer, ShopSign>();
	public static Hashtable<Integer, SignBlockEntity> signs = new Hashtable<Integer, SignBlockEntity>();
	private String CONFIG_PATH = String.format("%s/config/%s", MinecraftClient.getInstance().runDirectory, this.MOD_ID);
	private String serverAddress = "localhost";
	private String currentDatabaseName;
	private String currentDatabaseFile;

	private ServerInfo serverInfo;
	private Db databaseObject;
	public static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	@Override
	/**
	 * Setup code for mod, mostly sets up data structures and commands to do checks.
	 */
	public void onInitializeClient() {}
		// Stores.reload();
		// SignUpdateCallback.EVENT.register(ShopperOld::addSign);
		// ClientLifecycleEvents.CLIENT_STOPPING.register(this::gameClosing);

		// ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((blockEntity, world) -> {
		// 	if (blockEntity instanceof SignBlockEntity) {
		// 		removeSign((SignBlockEntity) blockEntity);
		// 	}
		// });
// 		ClientCommandManager.DISPATCHER.register(
// 				literal("shopper_find_signs")
// 						.then(
// 								argument("radius", IntegerArgumentType.integer(0,2048))
// 										.executes(context -> {
// 											CheckDatabaseConnection();
// 											int ssSize = shopSigns.size();
// 											getNearbyBlocks(MinecraftClient.getInstance().player.getBlockPos(), IntegerArgumentType.getInteger(context, "radius"));
// 											writeJSON();
// 											context.getSource().sendFeedback(new LiteralText(String.format("Processed %d shop signs (%d new) in %d block radius.",shopSigns.size(),(shopSigns.size() - ssSize), context.getArgument("radius", Integer.class))));
// 											return 1;
// 										})
// 						)
// 						.executes(context -> {
// 							CheckDatabaseConnection();
// 							int ssSize = shopSigns.size();
// 							getNearbyBlocks(MinecraftClient.getInstance().player.getBlockPos(), 256);
// 							writeJSON();
// 							context.getSource().sendFeedback(new LiteralText(String.format("Processed %d shop signs (%d new) in 256 block radius.",shopSigns.size(),(shopSigns.size() - ssSize))));
// 							return 1;
// 						})
// 		);
// 		ClientCommandManager.DISPATCHER.register(
// 				literal("shopper_find_item")
// 						.then(
// 								argument("itemCode", StringArgumentType.string())
// 										.executes(context -> {
// 											CheckDatabaseConnection();
// 											List<ShopSign> setMatches = shopSigns.entrySet()
// 													.stream()
// 													.filter(entry -> entry.getValue().itemCode.toLowerCase(Locale.ROOT).contains(StringArgumentType.getString(context, "itemCode").toLowerCase(Locale.ROOT)))
// 													.map(Map.Entry::getValue)
// 													.sorted(Comparator.comparing(ShopSign::getItemCode))
// 													.sorted(Comparator.comparing(ShopSign::getPriceBuyEach))
// 													.collect(Collectors.toList());
// //											List<Person> personList = personSet.stream().sorted((e1, e2) ->
// //													e1.getName().compareTo(e2.getName())).collect(Collectors.toList());
// 											StringBuilder results = new StringBuilder();
// 											Iterator<ShopSign> itr = setMatches.iterator();
// 											String thisItem = "";
// 											while(itr.hasNext()){
// 												ShopSign thisSign = itr.next();
// 												if (!thisItem.equals(thisSign.itemCode)) {
// 													thisItem = thisSign.itemCode;
// 													results.append(String.format("---------- %s ----------\n",thisItem));
// 												}
// 												results.append(String.format("%s (%s) Qty: %d", thisSign.posString,thisSign.sellerName,thisSign.itemQuantity));
// 												if (thisSign.canBuy)
// 													results.append(String.format(" Buy: %.2f",thisSign.priceBuy));
// 												if (thisSign.canSell)
// 													results.append(String.format(" Sell: %.2f",thisSign.priceSell));
// 												results.append("\n");
// 											}
// 											context.getSource().sendFeedback(new LiteralText(String.format("--------------------\nFound %d shops matching query '%s'\n--------------------\n\n%s",setMatches.size(), context.getArgument("itemCode", String.class),results)));
// 											return 1;
// 										})
// 						)
// 		);

	// }

	public Integer CheckDatabaseConnection() {
		if (serverInfo == null)
			serverInfo = MinecraftClient.getInstance().getCurrentServerEntry();
		if (serverInfo != null) {
			if (serverAddress != serverInfo.address) {
				serverAddress = serverInfo.address;
			}
		}
		return UpdateDatabaseConnection();
	}
	public Integer UpdateDatabaseConnection() {
		if ((currentDatabaseName == serverAddress) || (databaseObject == null)) {
			return 0;
		}
		if (databaseObject != null) {
			//databaseObject.closeConnection();
		}
		signs.clear();
		shopSigns.clear();
		currentDatabaseFile = String.format("%s/shops.%s.sqlite", CONFIG_PATH, serverAddress);
		databaseObject = new Db(currentDatabaseFile);
		databaseObject.open();
		if (databaseObject.checkConnection()) {
			// TODO: Load records from database into the signs/shopSigns hashtables
			currentDatabaseName = serverAddress;
		}
		return 1;
	}

	/**
	 * Upon game closing, attempt to save our json stores. This means we can be a little lazy with how
	 * we go about saving throughout the rest of the mod
	 * @param client the client whose session is ending
	 */
	private void gameClosing(MinecraftClient client) {
		LOGGER.info("gameClosing");
		Stores.write();
	}
	/**
		Write the shopSigns hashtable to JSON
	 */
	public void writeJSON() {
		Stores.write();
		ServerInfo serverInfo = MinecraftClient.getInstance().getCurrentServerEntry();
		String serverAddress = "localhost";
		if (serverInfo != null)
			serverAddress = serverInfo.address;
		String fileJson = String.format("%s/shops.%s.json", CONFIG_PATH, serverAddress);
		LOGGER.info(String.format("fileJson: %s", fileJson));
		Gson gson = new GsonBuilder()
				.setPrettyPrinting()
				.create();

		try {
			if (new File(CONFIG_PATH).mkdirs()) {
				LOGGER.info("mkdir");
			}
			try (FileWriter writer = new FileWriter(fileJson)) {
				gson.toJson(shopSigns.values(), writer);
				writer.flush();
				LOGGER.info(String.format("Wrote %d signs to %s", shopSigns.size(), fileJson));
			}

		} catch (IOException | JsonIOException e) {
			LOGGER.catching(e);
		}
	}

	/**
	 *
	 * @param location location to scan from
	 * @param radius how many blocks to scan (does +/- from location in X/Z, Y is always from bedrock to sky limit)
	 * @return nothing now, the return value ends up being a null list because the logic is handled in-loop
	 */
	public static List<Block> getNearbyBlocks(BlockPos location, int radius) {
		Stores.reload();
		List<Block> blocks = new ArrayList<Block>();
		for(int x = location.getX() - radius; x <= location.getX() + radius; x++) {
			for(int y = 0; y <= 256; y++) {
				for(int z = location.getZ() - radius; z <= location.getZ() + radius; z++) {
					BlockEntity blockEntity = MinecraftClient.getInstance().world.getBlockEntity(new BlockPos(x, y, z));
					if (blockEntity instanceof SignBlockEntity) {
						addSign((SignBlockEntity) blockEntity);
					}
				}
			}
		}
		return blocks;
	}

	/**
	 * Parse a sign block, attempt to create a ShopSign
	 * @param signBlockEntity the entity to process/check
	 */
	public static void parseSign(SignBlockEntity signBlockEntity) {
		// String[] signText = new String[4];
		// for (int i=0; i<4; i++) {
		// 	StringBuilder lineText = new StringBuilder();
		// 	signBlockEntity.getTextOnRow(i).visit((part) -> {
		// 			lineText.append(part);
		// 			return Optional.empty();
		// 		});
		// 	signText[i] = lineText.toString();
		// }
		// ShopSign shopSign = new ShopSign(signBlockEntity.getPos(),signText);
		// if (shopSign.sellerName != "")
		// 	shopSigns.put(signBlockEntity.getPos().hashCode(), shopSign);
	}

	/**
	 * Called whenever a sign entity is created. Not working presently.
	 * @param sign the entity to process
	 */
	public static void addSign(SignBlockEntity sign) {
		try {
			if (signs.containsKey(sign.hashCode())) {
				return;
			}
			LOGGER.info("addSign " + sign.hashCode());
			signs.put(sign.hashCode(), sign);
			parseSign(sign);
		}
		catch (Exception e) {
			LOGGER.error("OOPS! " + e);
		}
	}

	/**
	 * Remove a sign from the global list
	 * @param sign entity to remove
	 */
	public static void removeSign(SignBlockEntity sign) {
		signs.remove(sign);
	}
}
