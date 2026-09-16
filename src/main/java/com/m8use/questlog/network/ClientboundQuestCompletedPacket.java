package com.m8use.questlog.network;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Sent the instant a quest's progress crosses its completion threshold — independent of claiming,
 * and fired exactly once per completion regardless of how many individual actions caused it (e.g.
 * a Veinminer-style action breaking 12 blocks in one swing that happens to finish a quest still
 * only crosses the threshold once, so only one of this packet is ever sent for it).
 */
public record ClientboundQuestCompletedPacket(String templateId) {
   public void encode(FriendlyByteBuf buf) {
      buf.writeUtf(this.templateId, 64);
   }

   public static ClientboundQuestCompletedPacket decode(FriendlyByteBuf buf) {
      return new ClientboundQuestCompletedPacket(buf.readUtf(64));
   }
}
