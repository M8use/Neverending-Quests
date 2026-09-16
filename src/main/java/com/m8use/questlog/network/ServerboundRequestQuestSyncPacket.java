package com.m8use.questlog.network;

import com.m8use.questlog.quest.QuestManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public record ServerboundRequestQuestSyncPacket() {
   public void encode(FriendlyByteBuf buf) {
   }

   public static ServerboundRequestQuestSyncPacket decode(FriendlyByteBuf buf) {
      return new ServerboundRequestQuestSyncPacket();
   }

   public void handle(ServerPlayer player) {
      if (player != null) {
         QuestManager.sync(player);
      }
   }
}
