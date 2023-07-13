package com.jballou.shopper.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jballou.shopper.ShopperClient;
import com.jballou.shopper.util.Msg;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
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
	private static TagKey<Block> SIGN_BLOCKS = TagKey.of(RegistryKeys.BLOCK, new Identifier("minecraft", "all_signs"));

	public static void listener(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess)
	{
		dispatcher.register(ClientCommandManager.literal("scan")
		.then(ClientCommandManager.argument("range", IntegerArgumentType.integer(0, 32))
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

	private static void scan(CommandContext<FabricClientCommandSource> context, int range)
	{
		FabricClientCommandSource source = context.getSource();
		int radius = getScanRadius(source.getClient(), range);
		Msg.beginScan(context, radius);

		Map<BlockPos, BlockState> signs = getSignBlocks(getChunks(source.getClient(), source.getPosition(), radius));

		Msg.info(context, "found " + signs.size() + " signs!");
	}

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
					// ShopperClient.LOG.info("Chunk {}/{} : SUCCESS", u, v);
					result.add(chman.getWorldChunk(u, v));
				}
				else
				{
					// ShopperClient.LOG.info("Chunk {}/{} : FAILED", u, v);
				}
			}
		}

		return result;
	}

	private static Map<BlockPos, BlockState> getSignBlocks(List<WorldChunk> chunks)
	{
		Map<BlockPos, BlockState> result = new HashMap<>();
		for (WorldChunk chunk : chunks)
		{
			chunk.forEachBlockMatchingPredicate(blockstate ->
			{
				return blockstate.isIn(SIGN_BLOCKS);
			},
			(blockpos, blockstate) ->
			{
				result.putIfAbsent(blockpos, blockstate);
			}
			);
		}

		return result;
	}
}
