package com.jballou.shopper.command;

import com.jballou.shopper.util.Msg;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;

public final class Scan
{
	public static void listener(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess)
	{
		dispatcher.register(ClientCommandManager.literal("scan")
		.then(ClientCommandManager.argument("range", IntegerArgumentType.integer(0, 2048))
			.executes(context ->
			{
				int range = IntegerArgumentType.getInteger(context, "range");
				Msg.info(context, "used arg");
				scan(context, range);
				return 1;
			}))
		.executes(context ->
		{
			Msg.info(context, "no arg");
			scan(context, 0);
			return 1;
		}));
	}

	private static void scan(CommandContext<FabricClientCommandSource> context, int range)
	{
		Msg.beginScan(context, range);
	}
}
