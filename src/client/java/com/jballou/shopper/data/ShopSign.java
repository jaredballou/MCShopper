package com.jballou.shopper.data;

import java.util.Locale;

import com.google.gson.JsonObject;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class ShopSign
{
	public BlockPos pos;
	public Identifier dimension;
	public String sellerName;
	public int amount;
	public float buyPrice;
	public float sellPrice;
	public String itemName;
	public int stock;

	public ShopSign(JsonObject json, Identifier dimension)
	{
		pos = new BlockPos(json.get("x").getAsInt(), json.get("y").getAsInt(), json.get("z").getAsInt());
		sellerName = json.get("sellerName").getAsString();
		amount = json.get("amount").getAsInt();
		buyPrice = json.get("buyPrice").getAsFloat();
		sellPrice = json.get("sellPrice").getAsFloat();
		itemName = json.get("itemName").getAsString();
		stock = json.get("stock").getAsInt();

		this.dimension = dimension;
	}

	public ShopSign(SignBlockEntity sign, float buyPrice, float sellPrice, boolean useFrontSide)
	{
		pos = sign.getPos();
		dimension = sign.getWorld().getDimensionKey().getValue();

		SignText text = sign.getText(useFrontSide);
		sellerName = text.getMessage(0, false).getString();
		amount = Integer.parseInt(text.getMessage(1, false).getString());
		this.buyPrice = buyPrice;
		this.sellPrice = sellPrice;
		itemName = text.getMessage(3, useFrontSide).getString();
		stock = findStock(sign, itemName);
	}

	private int findStock(SignBlockEntity sign, String itemName)
	{
		// This function could be impossible to write.
		// I have outlined the issues and strategy here

		// find the item Identifer from the itemName
		//		this is tricky
		//		depends on how the shop plugin works
		//		need to convert the item name string:
		//			swap spaces for underscores
		//			prefix a tag namespace
		//			if there are custom blocks, this is an impossible task
		//		use converted string to make an Identifer
		// however, if the chests always contain 1 kind of item, we don't need an Identifier

		// find the chest block
		//		check each direction for a chest block
		//		BlockPos has funcs like BlockPos.up(1) that makes this easy
		//		Difficulty here is to know which chest is associated with the sign:
		//			an Identifier would help alleviate this
		//			if an Identifier is a solution, we can get the ID from the ItemStacks and strip out the path
		//			replace stripped out path underscores/slashes with spaces. Compare with itemName
		//			if itemName matches name derived from tag, we have found the chest:
		//				Issue: does not work for adjacent shops selling the same items
		// query the inventory, for each item stack add the count to an accumulator
		// return the accumulator
		return 0;
	}

	public float getBuyValue()
	{
		return buyPrice / amount;
	}

	public float getSellValue()
	{
		return sellPrice / amount;
	}

	public String getComparableItemName()
	{
		return itemName.toLowerCase(Locale.ROOT);
	}

	public boolean compare(ShopSign sign)
	{
		return false;
	}

	public JsonObject toJson()
	{
		JsonObject obj = new JsonObject();

		obj.addProperty("x", pos.getX());
		obj.addProperty("y", pos.getY());
		obj.addProperty("z", pos.getZ());
		obj.addProperty("sellerName", sellerName);
		obj.addProperty("amount", amount);
		obj.addProperty("buyPrice", buyPrice);
		obj.addProperty("sellPrice", sellPrice);
		obj.addProperty("itemName", itemName);
		obj.addProperty("stock", stock);

		return obj;
	}
}
