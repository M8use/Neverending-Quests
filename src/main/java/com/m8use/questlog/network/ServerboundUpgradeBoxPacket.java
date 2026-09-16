package com.m8use.questlog.network;

import com.m8use.questlog.quest.QuestManager;
import com.m8use.questlog.quest.QuestRarity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public record ServerboundUpgradeBoxPacket(String rarityId) {
   public void encode(FriendlyByteBuf buf) {
      buf.writeUtf(this.rarityId, 32);
   }

   public static ServerboundUpgradeBoxPacket decode(FriendlyByteBuf buf) {
      return new ServerboundUpgradeBoxPacket(buf.readUtf(32));
   }

   public void handle(ServerPlayer player) {
      if (player != null) {
         QuestRarity rarity = parseRarity(this.rarityId);
         if (rarity != null) {
            QuestManager.upgradeBox(player, rarity);
         }
      }
   }

   private static QuestRarity parseRarity(String name) {
      try {
         return QuestRarity.valueOf(name);
      } catch (IllegalArgumentException exception) {
         return null;
      }
   }
}
