package com.jballou.shopper.data;

import java.util.Locale;

import com.jballou.shopper.ShopperClient;

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
	public int buyPrice;
	public int sellPrice;
	public String itemName;

	public ShopSign(SignBlockEntity sign, int buyPrice, int sellPrice, boolean useFrontSide)
	{
		pos = sign.getPos();
		dimension = sign.getWorld().getDimensionKey().getValue();

		SignText text = sign.getText(useFrontSide);
		sellerName = text.getMessage(0, false).getString();
		amount = Integer.parseInt(text.getMessage(1, false).getString());
		this.buyPrice = buyPrice;
		this.sellPrice = sellPrice;
		itemName = text.getMessage(3, useFrontSide).getString();

		ShopperClient.LOG.info("New sign: {} {} {} {}", sellerName, amount, buyPrice, sellPrice);
	}

	public String getComparableItemName()
	{
		return itemName.toLowerCase(Locale.ROOT);
	}

	public boolean compare(ShopSign sign)
	{
		return false;
	}
}
