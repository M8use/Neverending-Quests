package com.m8use.questlog.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;

/**
 * Stored as a NeoForge data attachment on the player entity (saved/loaded with normal player NBT).
 * Plain mutable holder — callers mutate the lists/fields directly; entity attachments are
 * re-serialized from the live object at save time, no extra "mark dirty" step needed.
 */
public final class PlayerQuestData {
   public List<QuestInstance> dailyQuests = new ArrayList<>();
   public List<QuestInstance> weeklyQuests = new ArrayList<>();
   public List<QuestInstance> monthlyQuests = new ArrayList<>();
   public List<QuestInstance> mysteryQuests = new ArrayList<>();
   /** Epoch day (LocalDate#toEpochDay) of the last daily reset, or -1 if never. */
   public long lastDailyResetEpochDay = -1L;
   /** Epoch week number (epoch day / 7) of the last weekly reset, or -1 if never. */
   public long lastWeeklyResetEpochWeek = -1L;
   /** Epoch month number (year * 12 + monthValue) of the last monthly reset, or -1 if never. */
   public long lastMonthlyResetEpochMonth = -1L;
   /** The QuestLog's own persistent Mystery Box collection — never occupies normal inventory slots. */
   public MysteryBoxInventory mysteryBoxes = new MysteryBoxInventory();
   /**
    * Template ids of every Monthly Quest this player has already been assigned, oldest first.
    * Consulted on each monthly reset so the same Monthly Quest doesn't repeat until the whole
    * pool has been exhausted (see {@link QuestManager#assignMonthly}).
    */
   public List<String> monthlyQuestHistory = new ArrayList<>();

   public PlayerQuestData() {
   }

   public PlayerQuestData(
      List<QuestInstance> dailyQuests,
      List<QuestInstance> weeklyQuests,
      List<QuestInstance> monthlyQuests,
      List<QuestInstance> mysteryQuests,
      long lastDailyResetEpochDay,
      long lastWeeklyResetEpochWeek,
      long lastMonthlyResetEpochMonth,
      MysteryBoxInventory mysteryBoxes,
      List<String> monthlyQuestHistory
   ) {
      this.dailyQuests = new ArrayList<>(dailyQuests);
      this.weeklyQuests = new ArrayList<>(weeklyQuests);
      this.monthlyQuests = new ArrayList<>(monthlyQuests);
      this.mysteryQuests = new ArrayList<>(mysteryQuests);
      this.lastDailyResetEpochDay = lastDailyResetEpochDay;
      this.lastWeeklyResetEpochWeek = lastWeeklyResetEpochWeek;
      this.lastMonthlyResetEpochMonth = lastMonthlyResetEpochMonth;
      this.mysteryBoxes = mysteryBoxes;
      this.monthlyQuestHistory = new ArrayList<>(monthlyQuestHistory);
   }

   public List<QuestInstance> questsFor(QuestCategory category) {
      return switch (category) {
         case DAILY -> this.dailyQuests;
         case WEEKLY -> this.weeklyQuests;
         case MONTHLY -> this.monthlyQuests;
         case MYSTERY -> this.mysteryQuests;
      };
   }

   public static final Codec<PlayerQuestData> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            QuestInstance.CODEC.listOf().fieldOf("daily").forGetter(d -> d.dailyQuests),
            QuestInstance.CODEC.listOf().fieldOf("weekly").forGetter(d -> d.weeklyQuests),
            QuestInstance.CODEC.listOf().fieldOf("monthly").forGetter(d -> d.monthlyQuests),
            QuestInstance.CODEC.listOf().fieldOf("mystery").forGetter(d -> d.mysteryQuests),
            Codec.LONG.fieldOf("last_daily_reset_epoch_day").forGetter(d -> d.lastDailyResetEpochDay),
            Codec.LONG.fieldOf("last_weekly_reset_epoch_week").forGetter(d -> d.lastWeeklyResetEpochWeek),
            Codec.LONG.fieldOf("last_monthly_reset_epoch_month").forGetter(d -> d.lastMonthlyResetEpochMonth),
            MysteryBoxInventory.CODEC.optionalFieldOf("mystery_boxes", new MysteryBoxInventory()).forGetter(d -> d.mysteryBoxes),
            Codec.STRING.listOf().optionalFieldOf("monthly_quest_history", List.of()).forGetter(d -> d.monthlyQuestHistory)
         )
         .apply(instance, PlayerQuestData::new)
   );
}
