package com.m8use.questlog.network;

import net.minecraft.network.FriendlyByteBuf;

/**
 * The box's own rarity ({@code boxRarityId}) is only used for the "opening a Rarity box" label —
 * the actual reveal presentation (card color, glow, confetti intensity) is driven entirely by
 * {@code itemRarityId}, the rarity of the specific item that landed. The two are independent: a
 * Common box can land a Rare (or rarer) item and the reveal will correctly show as Rare.
 */
public record ClientboundMysteryBoxRevealPacket(String boxRarityId, String itemId, int amount, String itemRarityId) {
   public void encode(FriendlyByteBuf buf) {
      buf.writeUtf(this.boxRarityId, 32);
      buf.writeUtf(this.itemId, 256);
      buf.writeVarInt(this.amount);
      buf.writeUtf(this.itemRarityId, 32);
   }

   public static ClientboundMysteryBoxRevealPacket decode(FriendlyByteBuf buf) {
      return new ClientboundMysteryBoxRevealPacket(buf.readUtf(32), buf.readUtf(256), buf.readVarInt(), buf.readUtf(32));
   }
}
