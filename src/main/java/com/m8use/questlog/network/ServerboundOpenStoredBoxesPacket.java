package com.m8use.questlog.network;

import com.m8use.questlog.quest.QuestManager;
import com.m8use.questlog.quest.QuestRarity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/**
 * "Open {@code count} boxes of {@code rarityId} from my QuestLog collection." Used both by the
 * Mystery Box Inventory screen's bulk-open control and by the earned-box prompt's "Open Now"
 * button (with {@code count == 1}). The server re-validates ownership regardless of what the
 * client sends — see {@link QuestManager#openStoredBoxes}.
 */
public record ServerboundOpenStoredBoxesPacket(String rarityId, int count) {
   public void encode(FriendlyByteBuf buf) {
      buf.writeUtf(this.rarityId, 32);
      buf.writeVarInt(this.count);
   }

   public static ServerboundOpenStoredBoxesPacket decode(FriendlyByteBuf buf) {
      return new ServerboundOpenStoredBoxesPacket(buf.readUtf(32), buf.readVarInt());
   }

   public void handle(ServerPlayer player) {
      if (player != null) {
         QuestRarity rarity = parseRarity(this.rarityId);
         if (rarity != null) {
            QuestManager.openStoredBoxes(player, rarity, this.count);
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
