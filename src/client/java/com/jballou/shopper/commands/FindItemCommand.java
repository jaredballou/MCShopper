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
import com.jballou.shopper.*;

import com.jballou.shopper.storage.Stores;
import com.jballou.shopper.commands.*;
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

public class FindItemCommand {
    //private static final Logger LOGGER = LogUtils.getLogger();

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("shopper_find_item")
            .then(argument("itemCode", StringArgumentType.string())).executes(context -> {
                Shopper.CheckDatabaseConnection();
                List<ShopSign> setMatches = Shopper.shopSigns.entrySet()
                        .stream()
                        .filter(entry -> entry.getValue().itemCode.toLowerCase(Locale.ROOT).contains(StringArgumentType.getString(context, "itemCode").toLowerCase(Locale.ROOT)))
                        .map(Map.Entry::getValue)
                        .sorted(Comparator.comparing(ShopSign::getItemCode))
                        .sorted(Comparator.comparing(ShopSign::getPriceBuyEach))
                        .collect(Collectors.toList());
//											List<Person> personList = personSet.stream().sorted((e1, e2) ->
//													e1.getName().compareTo(e2.getName())).collect(Collectors.toList());
                StringBuilder results = new StringBuilder();
                Iterator<ShopSign> itr = setMatches.iterator();
                String thisItem = "";
                while(itr.hasNext()){
                    ShopSign thisSign = itr.next();
                    if (!thisItem.equals(thisSign.itemCode)) {
                        thisItem = thisSign.itemCode;
                        results.append(String.format("---------- %s ----------\n",thisItem));
                    }
                    results.append(String.format("%s (%s) Qty: %d", thisSign.posString,thisSign.sellerName,thisSign.itemQuantity));
                    if (thisSign.canBuy)
                        results.append(String.format(" Buy: %.2f",thisSign.priceBuy));
                    if (thisSign.canSell)
                        results.append(String.format(" Sell: %.2f",thisSign.priceSell));
                    results.append("\n");
                }
                context.getSource().sendFeedback(Text.of(String.format("--------------------\nFound %d shops matching query '%s'\n--------------------\n\n%s",setMatches.size(), context.getArgument("itemCode", String.class),results)));
                return 1;
            })
        );
    }
}

/*
 			dispatcher.register(ClientCommandManager.literal("shopper_find_item").then(
				ClientCommandManager.argument("itemCode", StringArgumentType.string())).executes(context -> {
					CheckDatabaseConnection();
					List<ShopSign> setMatches = shopSigns.entrySet()
							.stream()
							.filter(entry -> entry.getValue().itemCode.toLowerCase(Locale.ROOT).contains(StringArgumentType.getString(context, "itemCode").toLowerCase(Locale.ROOT)))
							.map(Map.Entry::getValue)
							.sorted(Comparator.comparing(ShopSign::getItemCode))
							.sorted(Comparator.comparing(ShopSign::getPriceBuyEach))
							.collect(Collectors.toList());
	//											List<Person> personList = personSet.stream().sorted((e1, e2) ->
	//													e1.getName().compareTo(e2.getName())).collect(Collectors.toList());
					StringBuilder results = new StringBuilder();
					Iterator<ShopSign> itr = setMatches.iterator();
					String thisItem = "";
					while(itr.hasNext()){
						ShopSign thisSign = itr.next();
						if (!thisItem.equals(thisSign.itemCode)) {
							thisItem = thisSign.itemCode;
							results.append(String.format("---------- %s ----------\n",thisItem));
						}
						results.append(String.format("%s (%s) Qty: %d", thisSign.posString,thisSign.sellerName,thisSign.itemQuantity));
						if (thisSign.canBuy)
							results.append(String.format(" Buy: %.2f",thisSign.priceBuy));
						if (thisSign.canSell)
							results.append(String.format(" Sell: %.2f",thisSign.priceSell));
						results.append("\n");
					}
					context.getSource().sendFeedback(Text.of(String.format("--------------------\nFound %d shops matching query '%s'\n--------------------\n\n%s",setMatches.size(), context.getArgument("itemCode", String.class),results)));
					return 1;
				})
			);
 */