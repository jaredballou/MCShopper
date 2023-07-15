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

	public ShopSign(JsonObject json, Identifier dimension)
	{
		pos = new BlockPos(json.get("x").getAsInt(), json.get("y").getAsInt(), json.get("z").getAsInt());
		sellerName = json.get("sellerName").getAsString();
		amount = json.get("amount").getAsInt();
		buyPrice = json.get("buyPrice").getAsFloat();
		sellPrice = json.get("sellPrice").getAsFloat();
		itemName = json.get("itemName").getAsString();

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

		return obj;
	}
}
