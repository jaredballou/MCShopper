package com.jballou.shopper;

import java.util.regex.*;
import java.util.*;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.util.math.*;

/**
 * Individual entities for tracking shop signs.
 */
public class ShopSign {
    public String serverAddress;
    public String serverDimension;
    public String posString = "";
    public Integer posHashCode = 0;
    public Integer x, y, z;
    public String sellerName = "";
    public String itemCode = "";
    public Integer itemQuantity = 0;
    public float priceBuy = 0;
    public float priceSell = 0;
    public Boolean canBuy = false;
    public Boolean canSell = false;
    public float priceBuyEach = 0;
    public float priceSellEach = 0;

    public transient BlockPos blockPos;
    public transient String[] signText = new String[4];

    public ShopSign(BlockPos blockPos, String[] signText) {
        ServerInfo serverInfo = MinecraftClient.getInstance().getCurrentServerEntry();
        if (serverInfo != null)
            this.serverAddress = MinecraftClient.getInstance().getCurrentServerEntry().address;
        else
            this.serverAddress = "localhost";
        this.serverDimension = "overworld"; //TODO: make this work
        this.posHashCode = blockPos.hashCode();
        this.posString = String.format("%d %d %d", blockPos.getX(), blockPos.getY(), blockPos.getZ());
        this.setBlockPos(blockPos);
        this.setSignText(signText);
    }

    public void setBlockPos(BlockPos blockPos) {
        this.blockPos = blockPos;
        this.x = blockPos.getX();
        this.y = blockPos.getY();
        this.z = blockPos.getZ();
    }
    public void setSignText(String[] signText) {
        if (this.signText != null && Arrays.deepEquals(this.signText, signText)) {
            //Shopper.LOGGER.info("bailing out, text unchanged");
            return;
        }
        this.signText = signText;
        String regex = "(B ([0-9.]+))?[ ]*(:)?[ ]*(([0-9.]+) S)?";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(signText[2]);
        if (!matcher.matches()) {
            //Shopper.LOGGER.info("no matches");
            return;
        }
        if (matcher.group(2) == null && matcher.group(5) == null) {
            //Shopper.LOGGER.info("Price line not formatted as shop");
            return;
        }
        this.setSellerName(signText[0]);
        this.setItemQuantity(Integer.parseInt(signText[1]));
        this.setItemCode(signText[3]);

        if (matcher.group(2) == null)
            this.setPriceBuy(0.0f);
        else
            this.setPriceBuy(Float.parseFloat(matcher.group(2)));
        this.setCanBuy((matcher.group(2) != null));

        if (matcher.group(5) == null)
            this.setPriceSell(0.0f);
        else
            this.setPriceSell(Float.parseFloat(matcher.group(5)));
        this.setCanSell(matcher.group(5) != null);
    }
    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }
    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }
    public void setItemQuantity(Integer itemQuantity) {
        this.itemQuantity = itemQuantity;
    }
    public void setPriceBuy(float priceBuy) {
        this.priceBuy = priceBuy;
        if ((priceBuy > 0.0) && (this.itemQuantity > 0))
            this.priceBuyEach = priceBuy / this.itemQuantity;
    }
    public void setPriceSell(float priceSell) {
        this.priceSell = priceSell;
        if ((priceSell > 0.0) && (this.itemQuantity > 0))
            this.priceSellEach = priceSell / this.itemQuantity;
    }
    public void setCanBuy(Boolean canBuy) {
        this.canBuy = canBuy;
    }
    public void setCanSell(Boolean canSell) {
        this.canSell = canSell;
    }

    public String getSellerName() {
        return this.sellerName;
    }
    public String getItemCode() {
        return this.itemCode;
    }
    public Integer getItemQuantity() {
        return this.itemQuantity;
    }
    public float getPriceBuy() {
        return this.priceBuy;
    }
    public float getPriceSell() {
        return this.priceSell;
    }
    public float getPriceBuyEach() { return this.priceBuyEach; }
    public float getPriceSellEach() {
        return this.priceSellEach;
    }
    public Boolean getCanBuy() {
        return this.canBuy;
    }
    public Boolean getCanSell() {
        return this.canSell;
    }

}