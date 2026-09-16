package com.m8use.questlog.network;

import net.minecraft.network.FriendlyByteBuf;

/** Sent only when an upgrade (10 boxes -> 1 higher rarity) actually succeeds server-side, so the client's celebration effect can never fire for a rejected/no-op request. */
public record ClientboundBoxUpgradedPacket(String toRarityId) {
   public void encode(FriendlyByteBuf buf) {
      buf.writeUtf(this.toRarityId, 32);
   }

   public static ClientboundBoxUpgradedPacket decode(FriendlyByteBuf buf) {
      return new ClientboundBoxUpgradedPacket(buf.readUtf(32));
   }
}
