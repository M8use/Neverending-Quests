package com.m8use.questlog.quest;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * Prevents the "place a block, then break it" exploit for mining/breaking quests: a block only
 * counts toward a MINE_BLOCK-style objective if it wasn't placed by a player in the first place.
 *
 * <p>Backed by a per-chunk data attachment ({@link QuestAttachments#PLACED_BLOCKS}) so it survives
 * chunk unload/reload, server restarts, and works independently per player (the position itself is
 * marked, not tied to a single player's data, so it can't be bypassed by having another player
 * place the block instead).
 */
public final class PlacedBlockTracker {
   public static void markPlaced(Level level, BlockPos pos) {
      if (!level.isClientSide()) {
         LevelChunk chunk = level.getChunkAt(pos);
         chunk.getData(QuestAttachments.PLACED_BLOCKS).positions.add(pos.asLong());
         chunk.setUnsaved(true);
      }
   }

   public static boolean isPlayerPlaced(Level level, BlockPos pos) {
      if (level.isClientSide()) {
         return false;
      } else {
         LevelChunk chunk = level.getChunkAt(pos);
         return chunk.getExistingData(QuestAttachments.PLACED_BLOCKS).map(data -> data.positions.contains(pos.asLong())).orElse(false);
      }
   }

   /** Called whenever a block is broken (regardless of who broke it) so the tracked set never outlives the block it describes. */
   public static void clearPlaced(Level level, BlockPos pos) {
      if (!level.isClientSide()) {
         LevelChunk chunk = level.getChunkAt(pos);
         chunk.getExistingData(QuestAttachments.PLACED_BLOCKS).ifPresent(data -> {
            if (data.positions.remove(pos.asLong())) {
               chunk.setUnsaved(true);
            }
         });
      }
   }

   private PlacedBlockTracker() {
   }
}
