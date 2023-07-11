/*
package com.jballou.shopper.commands;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.regex.Pattern;

import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;
import com.google.gson.*;

import com.jballou.shopper.event.SignUpdateCallback;

import com.jballou.shopper.storage.Stores;
import com.jballou.shopper.commands.*;
import com.jballou.shopper.*;
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
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

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
//import net.minecraft.util.SignType;
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

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import static com.mojang.brigadier.arguments.IntegerArgumentType.*;

public class FindSignsCommand {
    private String currentDatabaseName;
	private String currentDatabaseFile;
    private static String CONFIG_PATH = String.format("%s/config/%s", MinecraftClient.getInstance().runDirectory, Shopper.MOD_ID);
    private static String serverAddress = "localhost";

    static ServerInfo serverInfo = MinecraftClient.getInstance().getCurrentServerEntry();
    static String fileJson = String.format("%s/config/%s/shops.%s.json", MinecraftClient.getInstance().runDirectory, Shopper.MOD_ID, serverAddress);


    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        if (serverInfo != null)
            serverAddress = serverInfo.address;
        dispatcher.register(literal("shopper_find_signs")
            .then(argument("radius", DoubleArgumentType.doubleArg(0,2048))).executes(ctx -> updateSigns(ctx.getSource(), getInteger(ctx,"radius")))
        .executes(ctx -> updateSigns(ctx.getSource(),256)));
    }
    public static int updateSigns(FabricClientCommandSource source, int radius) throws CommandSyntaxException {
        //Shopper.CheckDatabaseConnection();
        int ssSize = Shopper.shopSigns.size();
        Shopper.getNearbyBlocks(MinecraftClient.getInstance().player.getBlockPos(), radius);
        writeJSON();
        source.sendFeedback(Text.of(String.format("Processed %d shop signs (%d new) in 256 block radius.",Shopper.shopSigns.size(),(Shopper.shopSigns.size() - ssSize))));
        return Command.SINGLE_SUCCESS;
    }
    public static void writeJSON() {
		Stores.write();
		Shopper.LOGGER.info(String.format("fileJson: %s", fileJson));
		Gson gson = new GsonBuilder()
				.setPrettyPrinting()
				.create();

		try {
			if (new File(CONFIG_PATH).mkdirs()) {
				Shopper.LOGGER.info("mkdir");
			}
			try (FileWriter writer = new FileWriter(fileJson)) {
				gson.toJson(Shopper.shopSigns.values(), writer);
				writer.flush();
				Shopper.LOGGER.info(String.format("Wrote %d signs to %s", Shopper.shopSigns.size(), fileJson));
			}

		} catch (IOException | JsonIOException e) {
			Shopper.LOGGER.catching(e);
		}
	}
}

	/*
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> {
			dispatcher.register(ClientCommandManager.literal("shopper_find_signs").then(
				ClientCommandManager.argument("radius", DoubleArgumentType.doubleArg(0,2048))).executes(context -> {
					CheckDatabaseConnection();
					int ssSize = shopSigns.size();
					getNearbyBlocks(MinecraftClient.getInstance().player.getBlockPos(), IntegerArgumentType.getInteger(context, "radius"));
					writeJSON();
					context.getSource().sendFeedback(Text.of(String.format("Processed %d shop signs (%d new) in %d block radius.",shopSigns.size(),(shopSigns.size() - ssSize), context.getArgument("radius", Integer.class))));
					return 1;
			})
			.executes(context -> {
				CheckDatabaseConnection();
				int ssSize = shopSigns.size();
				getNearbyBlocks(MinecraftClient.getInstance().player.getBlockPos(), 256);
				writeJSON();
				context.getSource().sendFeedback(Text.of(String.format("Processed %d shop signs (%d new) in 256 block radius.",shopSigns.size(),(shopSigns.size() - ssSize))));
				return 1;
			})
			);
 */