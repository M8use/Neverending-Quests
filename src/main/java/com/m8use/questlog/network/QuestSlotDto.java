package com.m8use.questlog.network;

import net.minecraft.network.FriendlyByteBuf;

public record QuestSlotDto(String templateId, int progress, boolean claimed) {
   public void encode(FriendlyByteBuf buf) {
      buf.writeUtf(this.templateId, 64);
      buf.writeVarInt(this.progress);
      buf.writeBoolean(this.claimed);
   }

   public static QuestSlotDto decode(FriendlyByteBuf buf) {
      return new QuestSlotDto(buf.readUtf(64), buf.readVarInt(), buf.readBoolean());
   }
}
