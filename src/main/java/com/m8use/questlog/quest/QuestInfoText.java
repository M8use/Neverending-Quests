package com.m8use.questlog.quest;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Turns a {@link QuestTemplate} into a plain-language explanation of exactly what it requires —
 * generated from the template's own {@code objective}/{@code matchId}/{@code amount}/reward
 * fields, not written by hand per quest. If a quest's mechanics ever change here, its info panel
 * text automatically changes with it; there's nothing to keep in sync manually.
 */
public final class QuestInfoText {
   public static List<String> describe(QuestTemplate template) {
      List<String> lines = new ArrayList<>();
      lines.add(objectiveLine(template));
      String caveat = caveatLine(template);
      if (caveat != null) {
         lines.add(caveat);
      }

      lines.add(rewardLine(template));
      return lines;
   }

   private static String objectiveLine(QuestTemplate template) {
      String target = describeMatch(template.objective(), template.matchId());
      int amount = template.amount();
      return switch (template.objective()) {
         case MINE_BLOCK -> "Mine or break " + amount + " " + target + ".";
         case PLACE_BLOCK -> "Place " + amount + " " + target + ".";
         case KILL_ENTITY -> "Defeat " + amount + " " + target + ".";
         case CRAFT_ITEM -> "Craft " + amount + " " + target + ".";
         case SMELT_ITEM -> "Smelt " + amount + " " + target + ".";
         case CATCH_FISH -> "Catch " + amount + " fish while fishing.";
         case TRAVEL_DISTANCE -> "Travel a total of " + amount + " blocks.";
         case VISIT_BIOME -> "Visit " + amount + " unique biome" + (amount == 1 ? "" : "s") + ".";
         case VISIT_DIMENSION -> "Travel to " + target + ".";
         case COLLECT_DISTINCT_ITEMS -> "Pick up " + amount + " different kinds of items.";
         case BREED_ANIMAL -> "Breed " + amount + " pair" + (amount == 1 ? "" : "s") + " of animals.";
         case ENCHANT_ITEM -> "Enchant " + amount + " item" + (amount == 1 ? "" : "s") + " at an enchanting table.";
         case BREW_POTION -> "Brew and collect " + amount + " potion" + (amount == 1 ? "" : "s") + " from a brewing stand.";
         case TRADE_VILLAGER -> "Complete " + amount + " trade" + (amount == 1 ? "" : "s") + " with a villager or wandering trader.";
      };
   }

   /** Extra rules or exceptions worth calling out, or {@code null} if the objective is self-explanatory. */
   private static String caveatLine(QuestTemplate template) {
      return switch (template.objective()) {
         case MINE_BLOCK -> template.matchId().equals(QuestManager.UNDERGROUND_ORES_AND_STONE)
            ? "Only counts underground, below Y=60. Blocks you placed yourself do not count."
            : "Only legitimately broken or naturally generated blocks count. Blocks you placed yourself do not count.";
         case PLACE_BLOCK -> "You must actually place the block yourself — it doesn't count if it's already there.";
         case VISIT_BIOME -> "Each biome only counts the first time you visit it. Returning to a biome you've already counted does not increase progress.";
         case COLLECT_DISTINCT_ITEMS -> "Each kind of item only counts the first time you pick it up. Picking up more of a kind you've already counted does not increase progress.";
         case TRAVEL_DISTANCE -> "Counts cumulative distance moved, in any direction, across the whole quest.";
         case ENCHANT_ITEM -> "Counts the enchanting action itself, once per use of the table — not once per enchantment applied.";
         default -> null;
      };
   }

   private static String rewardLine(QuestTemplate template) {
      if (template.isMystery()) {
         return "Reward: a " + capitalize(template.rarity().name()) + " Mystery Box, added to your collection.";
      } else if (QuestManager.SWIFTNESS_II_REWARD_ID.equals(template.rewardItemId())) {
         return "Reward: " + template.rewardMin() + " Potion" + (template.rewardMin() == 1 ? "" : "s") + " of Swiftness II, plus " + template.rewardXp() + " XP.";
      } else if (QuestManager.DENSITY_MACE_REWARD_ID.equals(template.rewardItemId())) {
         return "Reward: a Mace enchanted with Density V, plus " + template.rewardXp() + " XP.";
      } else if (QuestManager.RANDOM_LOG_REWARD_ID.equals(template.rewardItemId())) {
         return "Reward: " + template.rewardMin() + " logs of a randomly chosen wood type, plus " + template.rewardXp() + " XP.";
      } else if (QuestManager.RANDOM_BLOCK_REWARD_ID.equals(template.rewardItemId())) {
         return "Reward: " + template.rewardMin() + " of a randomly chosen block (any survival-obtainable block except dragon egg and the metal/gem storage blocks), plus " + template.rewardXp() + " XP.";
      } else if (QuestManager.LURE_UNBREAKING_BOOK_REWARD_ID.equals(template.rewardItemId())) {
         return "Reward: an enchanted book with Lure III and Unbreaking III, plus " + template.rewardXp() + " XP.";
      } else if ("minecraft:enchanted_book".equals(template.rewardItemId())) {
         return "Reward: an enchanted book with a random enchantment, plus " + template.rewardXp() + " XP.";
      } else {
         String amount = template.rewardMin() >= template.rewardMax() ? String.valueOf(template.rewardMin()) : template.rewardMin() + "-" + template.rewardMax();
         String itemName = readableId(template.rewardItemId());
         return "Reward: " + amount + " " + itemName + ", plus " + template.rewardXp() + " XP.";
      }
   }

   private static String describeMatch(ObjectiveType objective, String matchId) {
      if (matchId.equals("any")) {
         return objective == ObjectiveType.PLACE_BLOCK ? "any blocks" : "anything";
      } else if (matchId.equals("hostile")) {
         return "hostile mobs";
      } else if (matchId.equals("crops")) {
         return "fully-grown crops";
      } else if (matchId.equals(QuestManager.UNDERGROUND_ORES_AND_STONE)) {
         return "underground ores and stone";
      } else {
         return readableId(matchId);
      }
   }

   /** "#minecraft:coal_ores" / "minecraft:zombie" -> "Coal Ores" / "Zombie" — a generic, good-enough fallback for any id or tag this mod doesn't special-case above. */
   private static String readableId(String id) {
      String path = id.startsWith("#") ? id.substring(1) : id;
      int colon = path.indexOf(':');
      if (colon >= 0) {
         path = path.substring(colon + 1);
      }

      String[] words = path.split("_");
      StringBuilder builder = new StringBuilder();

      for (String word : words) {
         if (!word.isEmpty()) {
            if (builder.length() > 0) {
               builder.append(' ');
            }

            builder.append(capitalize(word));
         }
      }

      return builder.toString();
   }

   private static String capitalize(String word) {
      return word.isEmpty() ? word : Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase(Locale.ROOT);
   }

   private QuestInfoText() {
   }
}
