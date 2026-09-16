package com.m8use.questlog.client;

import java.util.List;
import com.m8use.questlog.network.QuestSlotDto;
import com.m8use.questlog.quest.QuestCategory;
import com.m8use.questlog.quest.QuestRarity;
import com.m8use.questlog.quest.QuestTemplate;
import com.m8use.questlog.quest.QuestTemplates;

public final class QuestClientState {
   private static List<QuestSlotDto> daily = List.of();
   private static List<QuestSlotDto> weekly = List.of();
   private static List<QuestSlotDto> monthly = List.of();
   private static List<QuestSlotDto> mystery = List.of();
   private static long dailyResetEpochMillis;
   private static long weeklyResetEpochMillis;
   private static long monthlyResetEpochMillis;
   /** Indexed by {@link QuestRarity#ordinal()}. */
   private static List<Integer> mysteryBoxCounts = List.of(0, 0, 0, 0, 0, 0);

   public static void apply(
      List<QuestSlotDto> daily,
      List<QuestSlotDto> weekly,
      List<QuestSlotDto> monthly,
      List<QuestSlotDto> mystery,
      long dailyResetEpochMillis,
      long weeklyResetEpochMillis,
      long monthlyResetEpochMillis,
      List<Integer> mysteryBoxCounts
   ) {
      QuestClientState.daily = daily;
      QuestClientState.weekly = weekly;
      QuestClientState.monthly = monthly;
      QuestClientState.mystery = mystery;
      QuestClientState.dailyResetEpochMillis = dailyResetEpochMillis;
      QuestClientState.weeklyResetEpochMillis = weeklyResetEpochMillis;
      QuestClientState.monthlyResetEpochMillis = monthlyResetEpochMillis;
      QuestClientState.mysteryBoxCounts = mysteryBoxCounts;
   }

   public static List<QuestSlotDto> daily() {
      return daily;
   }

   public static List<QuestSlotDto> weekly() {
      return weekly;
   }

   public static List<QuestSlotDto> monthly() {
      return monthly;
   }

   public static List<QuestSlotDto> mystery() {
      return mystery;
   }

   public static List<QuestSlotDto> forCategory(QuestCategory category) {
      return switch (category) {
         case DAILY -> daily;
         case WEEKLY -> weekly;
         case MONTHLY -> monthly;
         case MYSTERY -> mystery;
      };
   }

   public static long dailyResetEpochMillis() {
      return dailyResetEpochMillis;
   }

   public static long weeklyResetEpochMillis() {
      return weeklyResetEpochMillis;
   }

   public static long monthlyResetEpochMillis() {
      return monthlyResetEpochMillis;
   }

   public static int mysteryBoxCount(QuestRarity rarity) {
      int index = rarity.ordinal();
      return index < mysteryBoxCounts.size() ? mysteryBoxCounts.get(index) : 0;
   }

   /** True the moment any Daily/Weekly/Monthly/Mystery slot is complete and not yet claimed — drives the Inventory button's red badge. */
   public static boolean hasAnyClaimable() {
      for (QuestCategory category : QuestCategory.values()) {
         for (QuestSlotDto slot : forCategory(category)) {
            if (!slot.claimed() && isComplete(slot)) {
               return true;
            }
         }
      }

      return false;
   }

   private static boolean isComplete(QuestSlotDto slot) {
      try {
         QuestTemplate template = QuestTemplates.byId(slot.templateId());
         return slot.progress() >= template.amount();
      } catch (IllegalArgumentException exception) {
         return false;
      }
   }

   private QuestClientState() {
   }
}
