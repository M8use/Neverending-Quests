package com.m8use.questlog.network;

import net.minecraft.network.FriendlyByteBuf;

public record ClientboundQuestClaimResultPacket(String templateId, String itemId, int amount, int xp) {
   public void encode(FriendlyByteBuf buf) {
      buf.writeUtf(this.templateId, 64);
      buf.writeUtf(this.itemId, 256);
      buf.writeVarInt(this.amount);
      buf.writeVarInt(this.xp);
   }

   public static ClientboundQuestClaimResultPacket decode(FriendlyByteBuf buf) {
      return new ClientboundQuestClaimResultPacket(buf.readUtf(64), buf.readUtf(256), buf.readVarInt(), buf.readVarInt());
   }
}
