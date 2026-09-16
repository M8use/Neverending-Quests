package com.m8use.questlog.quest;

import com.m8use.questlog.QuestLogMod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class QuestAttachments {
   private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(
      NeoForgeRegistries.Keys.ATTACHMENT_TYPES, QuestLogMod.MODID
   );

   public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerQuestData>> DATA = ATTACHMENT_TYPES.register(
      "player_quest_data", () -> AttachmentType.builder(PlayerQuestData::new).serialize(PlayerQuestData.CODEC).copyOnDeath().build()
   );

   /** Per-chunk record of which positions currently hold a player-placed block; see {@link PlacedBlockTracker}. */
   public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlacedBlockChunkData>> PLACED_BLOCKS = ATTACHMENT_TYPES.register(
      "placed_blocks", () -> AttachmentType.builder(PlacedBlockChunkData::new).serialize(PlacedBlockChunkData.CODEC).build()
   );

   public static void register(IEventBus modBus) {
      ATTACHMENT_TYPES.register(modBus);
   }

   private QuestAttachments() {
   }
}
