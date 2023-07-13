package com.jballou.shopper.util;

import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Utility for sending formatted messages to chat from commands.
 * Should reduce some garbage by reusing common Text objects
 */
public final class Msg
{
	private Msg() {}

	private static final Text BEGIN_SCAN = Text.literal("Beginning scan with radius ").formatted(Formatting.WHITE);
	private static final Text BEGIN_ITEM_SEARCH = Text.literal("Beginning item search for ");

	private static MutableText mkTxt()
	{
		return Text.literal("[Shopper]").formatted(Formatting.AQUA);
	}

	public static void info(CommandContext<FabricClientCommandSource> context, String str)
	{
		context.getSource().sendFeedback(mkTxt().append(Text.literal(str).formatted(Formatting.WHITE)));
	}

	public static void beginScan(CommandContext<FabricClientCommandSource> context, Integer radius)
	{
		MutableText txt = mkTxt().append(BEGIN_SCAN).append(Text.literal(radius.toString()).formatted(Formatting.GREEN));
		context.getSource().sendFeedback(txt);
	}
}
