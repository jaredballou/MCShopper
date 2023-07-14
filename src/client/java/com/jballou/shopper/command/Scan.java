package com.jballou.shopper.command;

import java.util.ArrayList;
import java.util.List;

import com.jballou.shopper.data.BuySellParser;
import com.jballou.shopper.data.ShopSign;
import com.jballou.shopper.util.Msg;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.Block;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;

public final class Scan
{
	private static int MAX_RANGE = 32;
	private static TagKey<Block> SIGN_BLOCKS = TagKey.of(RegistryKeys.BLOCK, new Identifier("minecraft", "all_signs"));

	public static void listener(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess)
	{
		dispatcher.register(ClientCommandManager.literal("scan")
		.then(ClientCommandManager.argument("range", IntegerArgumentType.integer(0, MAX_RANGE))
			.executes(context ->
			{
				int range = IntegerArgumentType.getInteger(context, "range");
				scan(context, range);
				return 1;
			}))
		.executes(context ->
		{
			scan(context, 0);
			return 1;
		}));
	}

	/**
	 * Perform a scan for any ShopSigns in the area & cache the result
	 * @param context Brigadier context
	 * @param range search range (in chunks)
	 */
	private static void scan(CommandContext<FabricClientCommandSource> context, int range)
	{
		FabricClientCommandSource source = context.getSource();
		int radius = getScanRadius(source.getClient(), range);
		Msg.beginScan(context, radius);

		List<SignBlockEntity> signs = getSignBlocks(getChunks(source.getClient(), source.getPosition(), radius));

		Msg.info(context, "Checking " + signs.size() + " signs...");

		// parser reads & stores the buy/sell data
		// reusing this object for each iter is cheaper than making many false-positive ShopSigns
		BuySellParser parser = new BuySellParser();
		int numFound = 0;
		for (SignBlockEntity sign : signs)
		{ 
			// ShopperClient.LOG.info("{}: {}", n, sign.getPos().toShortString());
			if(parser.parseSign(sign))
			{
				// Msg.info(context, "found a shop sign!");
				ShopSign ss = new ShopSign(sign, parser.buyPrice, parser.sellPrice, parser.isFrontSide);
				numFound += 1;
			}
		}

		Msg.endScan(context, numFound);
	}

	/**
	 * Determine the scan radius in chunks. Clamps the input to the client view distance.
	 * If zero, defaults to client view distance.
	 * @param client
	 * @param range desired range
	 * @return radius
	 */
	private static int getScanRadius(MinecraftClient client, int range)
	{
		int viewDist = client.options.getClampedViewDistance();
		return range > 0 ? Math.min(range, viewDist) : viewDist;
	}

	private static List<WorldChunk> getChunks(MinecraftClient client, Vec3d pos, int radius)
	{
		ClientChunkManager chman = client.world.getChunkManager();
		BlockPos playerPos = BlockPos.ofFloored(pos.x, pos.y, pos.z);
		ChunkPos start = new ChunkPos(playerPos);
		ArrayList<WorldChunk> result = new ArrayList<>();

		// searches a square area, but checks that chunks exist anyway
		for(int x = -radius; x <= radius; x++)
		{
			for(int z = -radius; z <= radius; z++)
			{
				int u = start.x + x;
				int v = start.z + z;
				if(chman.isChunkLoaded(u, v))
				{
					result.add(chman.getWorldChunk(u, v));
				}
			}
		}
		return result;
	}

	private static List<SignBlockEntity> getSignBlocks(List<WorldChunk> chunks)
	{
		List<SignBlockEntity> result = new ArrayList<>();
		for (WorldChunk chunk : chunks)
		{
			chunk.forEachBlockMatchingPredicate(blockstate ->
			{
				return blockstate.isIn(SIGN_BLOCKS);
			},
			(blockpos, blockstate) ->
			{
				result.add((SignBlockEntity)chunk.getWorld().getBlockEntity(blockpos));
			});
		}
		return result;
	}
}
