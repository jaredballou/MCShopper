package com.jballou.shopper.command;

import com.mojang.brigadier.CommandDispatcher;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;

public final class FindItem
{
	public static void listener(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess)
	{
		dispatcher.register(ClientCommandManager.literal("finditem").executes(context ->
		{
			context.getSource().sendFeedback(Text.literal("Finding item..."));
			return 1;
		}));
	}
}
