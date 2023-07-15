package com.jballou.shopper.util;

import java.util.Set;

import com.jballou.shopper.data.ShopSign;
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
	private static final Text END_SCAN_A = Text.literal("Scan complete. Found ").formatted(Formatting.WHITE);
	private static final Text END_SCAN_B = Text.literal(" shop signs").formatted(Formatting.WHITE);
	private static final Text ITEM_FAIL_BUYER = Text.literal("No buyer found for ").formatted(Formatting.RED);
	private static final Text ITEM_FAIL_SELLER = Text.literal("No seller found for ").formatted(Formatting.RED);
	private static final Text ITEM_PRICE_BUYER = Text.literal("Best buyer for ").formatted(Formatting.WHITE);
	private static final Text ITEM_PRICE_SELLER = Text.literal("Best seller for ").formatted(Formatting.WHITE);
	private static final Text ITEM_PRICE_POS = Text.literal(" is at ").formatted(Formatting.WHITE);
	private static final Text ITEM_PRICE_AMT = Text.literal(" for ").formatted(Formatting.WHITE);
	private static final Text BEGIN_LIST = Text.literal("Shop Listings for ").formatted(Formatting.WHITE);
	private static final Text AMOUNT = Text.literal(" amount: ").formatted(Formatting.WHITE);
	private static final Text BUY_FOR = Text.literal(" buy: ").formatted(Formatting.WHITE);
	private static final Text SELL_FOR = Text.literal(" sell: ").formatted(Formatting.WHITE);

	private static MutableText mkTxt()
	{
		return Text.literal("[Shopper] ").formatted(Formatting.AQUA);
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

	public static void endScan(CommandContext<FabricClientCommandSource> context, Integer numFound)
	{
		MutableText txt = mkTxt().append(END_SCAN_A).append(Text.literal(numFound.toString()).formatted(Formatting.GREEN)).append(END_SCAN_B);
		context.getSource().sendFeedback(txt);
	}

	public static void itemSearchFail(CommandContext<FabricClientCommandSource> context, String itemName, boolean isBuyer)
	{
		Text msg = isBuyer ? ITEM_FAIL_BUYER : ITEM_FAIL_SELLER;
		MutableText txt = mkTxt().append(msg).append(Text.literal(itemName).formatted(Formatting.GREEN));
		context.getSource().sendFeedback(txt);
	}

	public static void itemSearchResult(CommandContext<FabricClientCommandSource> context, ShopSign shop, Float price, boolean isBuyer)
	{
		Text msg = isBuyer ? ITEM_PRICE_BUYER : ITEM_PRICE_SELLER;
		String plurality = shop.amount > 1 ? "s" : "";
		MutableText txt = mkTxt()
			.append(msg)
			.append(Text.literal(shop.itemName).formatted(Formatting.GREEN))
			.append(ITEM_PRICE_POS)
			.append(Text.literal(shop.sellerName + " shop ").formatted(Formatting.AQUA))
			.append(Text.literal("(" + shop.dimension.getPath() + ")\n             " + shop.pos.toShortString()).formatted(Formatting.GREEN))
			.append(ITEM_PRICE_AMT)
			.append(Text.literal(price.toString()).formatted(Formatting.GOLD))
			.append(Text.literal(" for " + shop.amount + " ").formatted(Formatting.WHITE))
			.append(Text.literal(shop.itemName + plurality).formatted(Formatting.GREEN));
		context.getSource().sendFeedback(txt);
	}

	public static void shopList(CommandContext<FabricClientCommandSource> context, String itemName, Set<ShopSign> set)
	{
		MutableText txt = mkTxt().append(BEGIN_LIST).append(Text.literal(":\n").formatted(Formatting.WHITE));
		for(ShopSign sign : set)
		{
			txt.append(Text.literal("(" + sign.dimension.getPath() + ") " + sign.pos.toShortString()).formatted(Formatting.GREEN))
			.append(Text.literal(" " + sign.sellerName).formatted(Formatting.AQUA))
			.append(AMOUNT)
			.append(Text.literal(Integer.toString(sign.amount)).formatted(Formatting.WHITE))
			.append(BUY_FOR)
			.append(Text.literal(Float.toString(sign.buyPrice)).formatted(Formatting.GOLD))
			.append(SELL_FOR)
			.append(Text.literal(Float.toString(sign.sellPrice) + "\n").formatted(Formatting.GOLD));
		}
		context.getSource().sendFeedback(txt);
	}
}
