package com.m8use.questlog.quest;

public enum QuestCategory {
   DAILY(6, "gui.questlog.category.daily"),
   WEEKLY(3, "gui.questlog.category.weekly"),
   MONTHLY(4, "gui.questlog.category.monthly"),
   MYSTERY(6, "gui.questlog.category.mystery");

   private final int activeSlotCount;
   private final String translationKey;

   QuestCategory(int activeSlotCount, String translationKey) {
      this.activeSlotCount = activeSlotCount;
      this.translationKey = translationKey;
   }

   /** How many quests from this category are active for a player at once. */
   public int activeSlotCount() {
      return this.activeSlotCount;
   }

   public String translationKey() {
      return this.translationKey;
   }
}
