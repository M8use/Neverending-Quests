package com.m8use.questlog.quest;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Attached to each {@link net.minecraft.world.level.chunk.LevelChunk} (see
 * {@link QuestAttachments#PLACED_BLOCKS}) to remember which block positions in that chunk are
 * currently occupied by a player-placed block, as opposed to a naturally generated one.
 *
 * <p>Entries are added when a player places a block and removed again as soon as that block is
 * broken (by anyone, for any reason) — so this only ever holds positions for blocks that are
 * <em>currently standing</em>, which keeps it naturally bounded by the amount of player
 * modification actually present in the loaded world rather than growing without limit.
 */
public final class PlacedBlockChunkData {
   public final Set<Long> positions = new HashSet<>();

   public static final Codec<PlacedBlockChunkData> CODEC = Codec.LONG
      .listOf()
      .xmap(PlacedBlockChunkData::fromList, data -> new ArrayList<>(data.positions));

   private static PlacedBlockChunkData fromList(List<Long> list) {
      PlacedBlockChunkData data = new PlacedBlockChunkData();
      data.positions.addAll(list);
      return data;
   }
}
