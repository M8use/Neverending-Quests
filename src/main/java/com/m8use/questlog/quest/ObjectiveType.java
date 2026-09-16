package com.m8use.questlog.quest;

/**
 * What a quest is actually tracking. {@link com.m8use.questlog.event.QuestTrackingEvents} listens
 * for the relevant vanilla/NeoForge event for each of these and reports progress through
 * {@link QuestManager#addProgress}; quest templates never need their own bespoke tracking code.
 */
public enum ObjectiveType {
   /** matchId = a block tag (e.g. "minecraft:iron_ores") or "any" for any block. */
   MINE_BLOCK,
   /** matchId = "hostile" for any {@code Enemy}, or a specific entity type id. */
   KILL_ENTITY,
   /** matchId = an item id, or "any" for any craft. */
   CRAFT_ITEM,
   /** matchId = an item id, or "any" for any smelt/cook result. */
   SMELT_ITEM,
   /** matchId unused; counts successful fishing catches. */
   CATCH_FISH,
   /** matchId unused; counts animal breeding events. */
   BREED_ANIMAL,
   /** matchId unused; amount is in blocks travelled (horizontal distance). */
   TRAVEL_DISTANCE,
   /** matchId unused; amount is the number of distinct biomes to visit this cycle. */
   VISIT_BIOME,
   /** matchId = a dimension id (e.g. "minecraft:the_nether"); counts a single visit. */
   VISIT_DIMENSION,
   /** matchId = an item tag or id; counts distinct matching items ever picked up this cycle. */
   COLLECT_DISTINCT_ITEMS,
   /**
    * matchId = "any" for any block, "underground" for the Master Miner-style underground
    * ore+stone check, or a block tag/id. Only counts blocks actually placed by a player during
    * this action; see {@link PlacedBlockTracker}.
    */
   PLACE_BLOCK,
   /** matchId unused; counts one unit of progress per completed enchanting-table action, not per enchantment applied. */
   ENCHANT_ITEM,
   /** matchId unused; counts one unit of progress each time the player picks up a brewed potion from a brewing stand. */
   BREW_POTION,
   /** matchId unused; counts one unit of progress per completed trade with any villager or wandering trader. */
   TRADE_VILLAGER
}
