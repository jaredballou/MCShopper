package com.jballou.shopper.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;

/**
 * Holds a list of shops.
 * This class is used by the ShopCache, and represents a single
 * World (dimension) in the cache.
 */
public final class WorldShopList
{
	private final Map<BlockPos, ShopSign> SIGNS = new HashMap<>();
	private final Map<String, ItemList> ITEMS = new HashMap<>();

	/**
	 * An ItemList holds lists of signs sorted by buy/sell value.
	 * It is used by WorldShopList in a map with the item name as
	 * the key, allowing for fast queries based on the name.
	 * 
	 * Signs with a price of -1 are ignored by this structure,
	 * which increases sort efficiency
	 */
	private static class ItemList
	{
		private boolean isDirty = true;

		public final List<ShopSign> buyers = new ArrayList<>();
		public final List<ShopSign> sellers = new ArrayList<>();

		public ShopSign add(ShopSign sign)
		{
			if(sign.buyPrice >= 0)
			{
				buyers.add(sign);
				isDirty = true;
			}

			if(sign.sellPrice >= 0)
			{
				sellers.add(sign);
				isDirty = true;
			}

			return sign;
		}

		public ShopSign remove(ShopSign sign)
		{
			if(buyers.contains(sign))
			{
				buyers.remove(sign);
				isDirty = true;
			}

			if(sellers.contains(sign))
			{
				sellers.remove(sign);
				isDirty = true;
			}
			
			return sign;
		}

		/**
		 * Sort contains the logic that does the work of determining the best
		 * value prices and is called by the /finditem command
		 * The dirty flag ensures repeated sorts with the same results are not
		 * executed, reducing workload
		 * @param playerPos
		 */
		public void sort(BlockPos playerPos)
		{
			if(!isDirty)
			{
				return;
			}

			buyers.sort((a, b) ->
			{
				float aValue = a.getBuyValue();
				float bValue = b.getBuyValue();

				if(aValue == bValue)
				{
					return findClosest(playerPos, a, b);
				}
				return aValue < bValue ? -1 : 1;
			});

			sellers.sort((a, b) ->
			{
				float aValue = a.getSellValue();
				float bValue = b.getSellValue();

				if(aValue == bValue)
				{
					return findClosest(playerPos, a, b);
				}
				return aValue > bValue ? -1 : 1;
			});

			isDirty = false;
		}

		private int findClosest(BlockPos pos, ShopSign a, ShopSign b)
		{
			boolean isACloser = pos.getSquaredDistance(a.pos) < pos.getSquaredDistance(b.pos);
			return isACloser ? -1 : 1;
		}
	}

	public WorldShopList() {}

	public WorldShopList(JsonArray arr, Identifier dimension)
	{
		for (JsonElement sign : arr.asList())
		{
			add(new ShopSign(sign.getAsJsonObject(), dimension));
		}
	}

	/**
	 * helper function tidies up the add/remove code a little
	 */
	private ItemList getItemListForSign(ShopSign sign)
	{
		String name = sign.getComparableItemName();
		if(!ITEMS.containsKey(name))
		{
			ITEMS.put(name, new ItemList());
		}

		return ITEMS.get(name);
	}

	public void add(ShopSign sign)
	{
		ShopSign oldSign = SIGNS.get(sign.pos);
		if(oldSign != null && !sign.compare(oldSign))
		{
			SIGNS.replace(sign.pos, sign);
			remove(oldSign);
		}
		else
		{
			SIGNS.put(sign.pos, sign);
		}
	
		getItemListForSign(sign).add(sign);
	}

	public void remove(ShopSign sign)
	{
		SIGNS.remove(sign.pos);
		getItemListForSign(sign).remove(sign);
	}

	/**
	 * Find the best prices for printing with the /finditem command
	 * @param itemName
	 * @param playerPos when prices are equal, reports the closest shop
	 * @return Pair of shopsigns. Either side may be null
	 */
	public Pair<ShopSign, ShopSign> findBestPrices(String itemName, BlockPos playerPos)
	{
		Pair<ShopSign, ShopSign> result = new Pair<>(null, null);
		ItemList list = ITEMS.get(itemName.toLowerCase(Locale.ROOT));
		
		if(list == null || list.buyers.size() == 0)
		{
			return result;
		}

		list.sort(playerPos);

		if(list.buyers.get(0).buyPrice >= 0)
		{
			result.setLeft(list.buyers.get(0));
		}

		if(list.sellers.get(0).sellPrice >- 0)
		{
			result.setRight(list.sellers.get(0));
		}

		return result;
	}

	public JsonArray toJson()
	{
		// ShopperClient.LOG.info("Parsing ShopList {}, {}", SIGNS.size(), SIGNS.values().size());

		JsonArray arr = new JsonArray();
		for(ShopSign sign : SIGNS.values())
		{
			// ShopperClient.LOG.info("Parsing sign {}", sign.pos);
			arr.add(sign.toJson());
		}
		return arr;
	}
}
