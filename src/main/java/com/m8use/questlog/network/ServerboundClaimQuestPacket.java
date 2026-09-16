package com.m8use.questlog.network;

import com.m8use.questlog.quest.QuestCategory;
import com.m8use.questlog.quest.QuestManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public record ServerboundClaimQuestPacket(QuestCategory category, int slotIndex) {
   public void encode(FriendlyByteBuf buf) {
      buf.writeEnum(this.category);
      buf.writeVarInt(this.slotIndex);
   }

   public static ServerboundClaimQuestPacket decode(FriendlyByteBuf buf) {
      return new ServerboundClaimQuestPacket(buf.readEnum(QuestCategory.class), buf.readVarInt());
   }

   public void handle(ServerPlayer player) {
      if (player != null) {
         QuestManager.claim(player, this.category, this.slotIndex);
      }
   }
}
