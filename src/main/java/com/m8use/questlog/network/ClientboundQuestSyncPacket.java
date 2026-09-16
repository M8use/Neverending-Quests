package com.m8use.questlog.network;

import java.util.List;
import net.minecraft.network.FriendlyByteBuf;

public record ClientboundQuestSyncPacket(
   List<QuestSlotDto> daily,
   List<QuestSlotDto> weekly,
   List<QuestSlotDto> monthly,
   List<QuestSlotDto> mystery,
   long dailyResetEpochMillis,
   long weeklyResetEpochMillis,
   long monthlyResetEpochMillis,
   List<Integer> mysteryBoxCounts
) {
   public void encode(FriendlyByteBuf buf) {
      buf.writeCollection(this.daily, (b, slot) -> slot.encode(b));
      buf.writeCollection(this.weekly, (b, slot) -> slot.encode(b));
      buf.writeCollection(this.monthly, (b, slot) -> slot.encode(b));
      buf.writeCollection(this.mystery, (b, slot) -> slot.encode(b));
      buf.writeVarLong(this.dailyResetEpochMillis);
      buf.writeVarLong(this.weeklyResetEpochMillis);
      buf.writeVarLong(this.monthlyResetEpochMillis);
      buf.writeCollection(this.mysteryBoxCounts, (b, count) -> b.writeVarInt(count));
   }

   public static ClientboundQuestSyncPacket decode(FriendlyByteBuf buf) {
      return new ClientboundQuestSyncPacket(
         buf.readList(QuestSlotDto::decode),
         buf.readList(QuestSlotDto::decode),
         buf.readList(QuestSlotDto::decode),
         buf.readList(QuestSlotDto::decode),
         buf.readVarLong(),
         buf.readVarLong(),
         buf.readVarLong(),
         buf.readList(FriendlyByteBuf::readVarInt)
      );
   }
}
