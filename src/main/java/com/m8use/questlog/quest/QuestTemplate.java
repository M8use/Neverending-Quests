package com.m8use.questlog.quest;

/**
 * One entry in the quest pool. {@code rarity} means two different things depending on category:
 *
 * <ul>
 *   <li>For {@link QuestCategory#MYSTERY} quests, it's fixed and determines which tier of Mystery
 *       Box is granted (the reward fields are unused for those).
 *   <li>For {@link QuestCategory#DAILY} quests, it's the quest's own difficulty/reward tier (see
 *       {@link QuestManager#DAILY_RARITY_WEIGHTS}) and drives both its visual background and how
 *       demanding {@code amount} and how generous the reward fields are.
 * </ul>
 *
 * For Weekly and Monthly quests, {@code rarity} is null — those categories don't have a per-quest
 * rarity system, just an overall difficulty tier implied by the category itself.
 */
public record QuestTemplate(
   String id,
   QuestCategory category,
   QuestRarity rarity,
   String nameKey,
   ObjectiveType objective,
   String matchId,
   int amount,
   String rewardItemId,
   int rewardMin,
   int rewardMax,
   int rewardXp
) {
   public boolean isMystery() {
      return this.category == QuestCategory.MYSTERY;
   }
}
