package com.m8use.questlog.network;

import java.util.List;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Results of opening two or more stored Mystery Boxes from the QuestLog Inventory in one request.
 * {@code itemIds}/{@code amounts}/{@code itemRarityIds} are parallel lists, one entry per box
 * opened — each item's own rarity (independent of {@code boxRarityId}) drives that item's card
 * color in the results grid. Opening exactly one stored box instead reuses the single-box
 * {@link ClientboundMysteryBoxRevealPacket} and its full roulette reveal screen.
 */
public record ClientboundMysteryBoxBulkRevealPacket(String boxRarityId, List<String> itemIds, List<Integer> amounts, List<String> itemRarityIds) {
   public void encode(FriendlyByteBuf buf) {
      buf.writeUtf(this.boxRarityId, 32);
      buf.writeCollection(this.itemIds, (b, id) -> b.writeUtf(id, 256));
      buf.writeCollection(this.amounts, (b, amount) -> b.writeVarInt(amount));
      buf.writeCollection(this.itemRarityIds, (b, id) -> b.writeUtf(id, 32));
   }

   public static ClientboundMysteryBoxBulkRevealPacket decode(FriendlyByteBuf buf) {
      return new ClientboundMysteryBoxBulkRevealPacket(
         buf.readUtf(32), buf.readList(b -> b.readUtf(256)), buf.readList(FriendlyByteBuf::readVarInt), buf.readList(b -> b.readUtf(32))
      );
   }
}
