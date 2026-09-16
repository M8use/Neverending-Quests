package com.m8use.questlog.quest;

import java.util.List;

/**
 * The quest pool, grouped by category. {@link QuestManager} randomly fills Daily/Weekly/Monthly
 * slots from these on reset, and always keeps exactly one Mystery quest active per rarity.
 *
 * <p>This is plain Java data today so it's easy to review and verify by hand; the shape is
 * deliberately simple (one {@link QuestTemplate} record per line) so swapping this for a
 * JSON-loaded pool later only touches this one file.
 *
 * <p>Pool sizes are intentionally lopsided — Common has the most entries, Mythic the fewest — so
 * that rolling a common rarity (which happens most often) still feels varied, while the rarest
 * rolls stay special without needing dozens of near-duplicate entries. Reward tiers are a
 * reasonable first pass, calibrated to stay below what the equivalent Mystery Box rarity can roll
 * (see {@link MysteryBoxLoot}) — expect to retune the exact numbers once this is in real play.
 */
public final class QuestTemplates {
   public static final List<QuestTemplate> DAILY = List.of(
      // --- COMMON (55) — things that happen naturally during normal play -------------------------------------
      new QuestTemplate("daily_common_coal", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_coal", ObjectiveType.MINE_BLOCK, "#minecraft:coal_ores", 64, "minecraft:furnace", 20, 20, 50),
      new QuestTemplate("daily_common_logs", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_logs", ObjectiveType.MINE_BLOCK, "#minecraft:logs", 32, "minecraft:apple", 3, 3, 20),
      new QuestTemplate("daily_common_hunter", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_hunter", ObjectiveType.KILL_ENTITY, "hostile", 8, "minecraft:arrow", 32, 32, 25),
      new QuestTemplate("daily_common_builder", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_builder", ObjectiveType.PLACE_BLOCK, "any", 24, QuestManager.RANDOM_LOG_REWARD_ID, 32, 32, 20),
      new QuestTemplate("daily_common_smelter", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_smelter", ObjectiveType.SMELT_ITEM, "any", 12, "minecraft:coal", 8, 8, 20),
      new QuestTemplate("daily_common_stroll", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_stroll", ObjectiveType.TRAVEL_DISTANCE, "any", 400, "minecraft:cooked_beef", 8, 8, 20),
      new QuestTemplate("daily_common_copper", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_copper", ObjectiveType.MINE_BLOCK, "#minecraft:copper_ores", 16, "minecraft:iron_ingot", 4, 4, 22),
      new QuestTemplate("daily_common_sand", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_sand", ObjectiveType.MINE_BLOCK, "minecraft:sand", 32, "minecraft:gravel", 32, 16, 18),
      new QuestTemplate("daily_common_gravel", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_gravel", ObjectiveType.MINE_BLOCK, "minecraft:gravel", 32, "minecraft:flint", 8, 8, 18),
      new QuestTemplate("daily_common_clay", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_clay", ObjectiveType.MINE_BLOCK, "minecraft:clay", 16, "minecraft:brick", 8, 8, 20),
      new QuestTemplate("daily_common_dirt", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_dirt", ObjectiveType.MINE_BLOCK, "minecraft:dirt", 64, "minecraft:wheat_seeds", 32, 32, 15),
      new QuestTemplate("daily_common_stone", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_stone", ObjectiveType.MINE_BLOCK, "minecraft:stone", 48, "minecraft:torch", 16, 16, 18),
      new QuestTemplate("daily_common_zombie", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_zombie", ObjectiveType.KILL_ENTITY, "minecraft:zombie", 8, "minecraft:carrot", 16, 16, 22),
      new QuestTemplate("daily_common_skeleton", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_skeleton", ObjectiveType.KILL_ENTITY, "minecraft:skeleton", 8, "minecraft:bone", 16, 16, 22),
      new QuestTemplate("daily_common_spider", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_spider", ObjectiveType.KILL_ENTITY, "minecraft:spider", 8, "minecraft:string", 16, 16, 22),
      new QuestTemplate("daily_common_creeper", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_creeper", ObjectiveType.KILL_ENTITY, "minecraft:creeper", 5, "minecraft:gunpowder", 10, 10, 25),
      new QuestTemplate("daily_common_drowned", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_drowned", ObjectiveType.KILL_ENTITY, "minecraft:drowned", 5, "minecraft:sea_lantern", 2, 2, 25),
      new QuestTemplate("daily_common_crafter", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_crafter", ObjectiveType.CRAFT_ITEM, "any", 30, "minecraft:experience_bottle", 4, 4, 20),
      new QuestTemplate("daily_common_chest_maker", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_chest_maker", ObjectiveType.CRAFT_ITEM, "minecraft:chest", 4, "minecraft:iron_ingot", 4, 4, 25),
      new QuestTemplate("daily_common_torch_maker", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_torch_maker", ObjectiveType.CRAFT_ITEM, "minecraft:torch", 32, "minecraft:coal", 8, 8, 18),
      new QuestTemplate("daily_common_smelt_food", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_smelt_food", ObjectiveType.SMELT_ITEM, "any", 8, "minecraft:coal", 2, 2, 18),
      new QuestTemplate("daily_common_angler", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_angler", ObjectiveType.CATCH_FISH, "any", 3, "minecraft:string", 16, 16, 20),
      new QuestTemplate("daily_common_angler2", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_angler2", ObjectiveType.CATCH_FISH, "any", 5, "minecraft:emerald", 4, 4, 25),
      new QuestTemplate("daily_common_wanderer2", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_wanderer2", ObjectiveType.TRAVEL_DISTANCE, "any", 800, "minecraft:leather", 8, 8, 25),
      new QuestTemplate("daily_common_explorer1", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_explorer1", ObjectiveType.VISIT_BIOME, "any", 2, "minecraft:apple", 8, 8, 18),
      new QuestTemplate("daily_common_explorer2", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_explorer2", ObjectiveType.VISIT_BIOME, "any", 3, "minecraft:bread", 12, 12, 22),
      new QuestTemplate("daily_common_builder2", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_builder2", ObjectiveType.PLACE_BLOCK, "any", 50, "minecraft:cobblestone", 64, 64, 22),
      new QuestTemplate("daily_common_builder3", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_builder3", ObjectiveType.PLACE_BLOCK, "#minecraft:planks", 32, "minecraft:stick", 32, 32, 20),
      new QuestTemplate("daily_common_rancher1", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_rancher1", ObjectiveType.BREED_ANIMAL, "any", 1, "minecraft:wheat", 12, 12, 18),
      new QuestTemplate("daily_common_rancher2", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_rancher2", ObjectiveType.BREED_ANIMAL, "any", 2, "minecraft:egg", 8, 8, 20),
      new QuestTemplate("daily_common_rancher3", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_rancher3", ObjectiveType.BREED_ANIMAL, "any", 3, "minecraft:leather", 6, 6, 24),
      new QuestTemplate("daily_common_collector1", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_collector1", ObjectiveType.COLLECT_DISTINCT_ITEMS, "any", 5, "minecraft:emerald", 2, 2, 25),
      new QuestTemplate("daily_common_collector2", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_collector2", ObjectiveType.COLLECT_DISTINCT_ITEMS, "any", 6, "minecraft:emerald", 3, 3, 28),
      new QuestTemplate("daily_common_trader1", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_trader1", ObjectiveType.TRADE_VILLAGER, "any", 1, "minecraft:emerald", 2, 2, 20),
      new QuestTemplate("daily_common_trader2", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_trader2", ObjectiveType.TRADE_VILLAGER, "any", 2, "minecraft:emerald", 4, 4, 25),
      new QuestTemplate("daily_common_enchant1", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_enchant1", ObjectiveType.ENCHANT_ITEM, "any", 1, "minecraft:lapis_lazuli", 8, 8, 22),
      new QuestTemplate("daily_common_andesite", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_andesite", ObjectiveType.MINE_BLOCK, "minecraft:andesite", 32, "minecraft:iron_ore", 8, 8, 16),
      new QuestTemplate("daily_common_granite", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_granite", ObjectiveType.MINE_BLOCK, "minecraft:granite", 32, "minecraft:redstone", 8, 8, 16),
      new QuestTemplate("daily_common_diorite", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_diorite", ObjectiveType.MINE_BLOCK, "minecraft:diorite", 32, "minecraft:iron_ore", 8, 8, 16),
      new QuestTemplate("daily_common_redstone", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_redstone", ObjectiveType.MINE_BLOCK, "minecraft:redstone_ore", 8, "minecraft:redstone", 16, 16, 25),
      new QuestTemplate("daily_common_kelp", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_kelp", ObjectiveType.MINE_BLOCK, "minecraft:kelp", 24, "minecraft:dried_kelp_block", 16, 16, 16),
      new QuestTemplate("daily_common_cactus", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_cactus", ObjectiveType.MINE_BLOCK, "minecraft:cactus", 16, "minecraft:green_dye", 8, 8, 16),
      new QuestTemplate("daily_common_sugarcane", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_sugarcane", ObjectiveType.MINE_BLOCK, "minecraft:sugar_cane", 24, "minecraft:paper", 16, 16, 16),
      new QuestTemplate("daily_common_pumpkin", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_pumpkin", ObjectiveType.MINE_BLOCK, "minecraft:pumpkin", 8, "minecraft:pumpkin", 8, 8, 18),
      new QuestTemplate("daily_common_melon", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_melon", ObjectiveType.MINE_BLOCK, "minecraft:melon", 8, "minecraft:melon", 16, 16, 18),
      new QuestTemplate("daily_common_farmer", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_farmer", ObjectiveType.MINE_BLOCK, "crops", 20, "minecraft:bread", 16, 16, 22),
      new QuestTemplate("daily_common_potato_farmer", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_potato_farmer", ObjectiveType.MINE_BLOCK, "minecraft:potatoes", 16, "minecraft:baked_potato", 16, 16, 18),
      new QuestTemplate("daily_common_beetroot_farmer", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_beetroot_farmer", ObjectiveType.MINE_BLOCK, "minecraft:beetroots", 16, "minecraft:diamond", 1, 1, 18),
      new QuestTemplate("daily_common_napper", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_napper", ObjectiveType.TRAVEL_DISTANCE, "any", 1000, "minecraft:emerald", 2, 2, 28),
      new QuestTemplate("daily_common_table_crafter", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_table_crafter", ObjectiveType.CRAFT_ITEM, "minecraft:crafting_table", 2, "minecraft:oak_planks", 16, 16, 16),
      new QuestTemplate("daily_common_bed_maker", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_bed_maker", ObjectiveType.CRAFT_ITEM, "#minecraft:beds", 1, "minecraft:white_wool", 8, 8, 18),
      new QuestTemplate("daily_common_husk", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_husk", ObjectiveType.KILL_ENTITY, "minecraft:husk", 5, "minecraft:tnt", 16, 16, 22),
      new QuestTemplate("daily_common_ice_breaker", QuestCategory.DAILY, QuestRarity.COMMON, "quest.questlog.daily_common_ice_breaker", ObjectiveType.MINE_BLOCK, "minecraft:ice", 16, "minecraft:blue_ice", 16, 16, 18),

      // --- UNCOMMON (38) — still casual, a little more effort ------------------------------------------------
      new QuestTemplate("daily_uncommon_iron", QuestCategory.DAILY, QuestRarity.UNCOMMON, "quest.questlog.daily_uncommon_iron", ObjectiveType.MINE_BLOCK, "#minecraft:iron_ores", 20, "minecraft:iron_ingot", 10, 10, 60),
      new QuestTemplate("daily_uncommon_hunter", QuestCategory.DAILY, QuestRarity.UNCOMMON, "quest.questlog.daily_uncommon_hunter", ObjectiveType.KILL_ENTITY, "hostile", 18, "minecraft:golden_carrot", 6, 6, 65),
      new QuestTemplate("daily_uncommon_angler", QuestCategory.DAILY, QuestRarity.UNCOMMON, "quest.questlog.daily_uncommon_angler", ObjectiveType.CATCH_FISH, "any", 5, "minecraft:cooked_cod", 16, 16, 55),
      new QuestTemplate("daily_uncommon_crafter", QuestCategory.DAILY, QuestRarity.UNCOMMON, "quest.questlog.daily_uncommon_crafter", ObjectiveType.CRAFT_ITEM, "any", 30, "minecraft:experience_bottle", 6, 6, 40),
      new QuestTemplate("daily_uncommon_rancher", QuestCategory.DAILY, QuestRarity.UNCOMMON, "quest.questlog.daily_uncommon_rancher", ObjectiveType.BREED_ANIMAL, "any", 5, "minecraft:cooked_beef", 10, 10, 55),
      new QuestTemplate("daily_uncommon_builder", QuestCategory.DAILY, QuestRarity.UNCOMMON, "quest.questlog.daily_uncommon_builder", ObjectiveType.PLACE_BLOCK, "any", 60, QuestManager.RANDOM_BLOCK_REWARD_ID, 64, 64, 55),
      new QuestTemplate("daily_uncommon_gold", QuestCategory.DAILY, QuestRarity.UNCOMMON, "quest.questlog.daily_uncommon_gold", ObjectiveType.MINE_BLOCK, "#minecraft:gold_ores", 16, "minecraft:gold_ingot", 8, 8, 60),
      new QuestTemplate("daily_uncommon_quartz", QuestCategory.DAILY, QuestRarity.UNCOMMON, "quest.questlog.daily_uncommon_quartz", ObjectiveType.MINE_BLOCK, "minecraft:netherrack", 10, "minecraft:quartz", 18, 18, 60),
      new QuestTemplate("daily_uncommon_spelunker", QuestCategory.DAILY, QuestRarity.UNCOMMON, "quest.questlog.daily_uncommon_spelunker", ObjectiveType.MINE_BLOCK, "#minecraft:base_stone_overworld", 96, "minecraft:torch", 67, 69, 420),
      new QuestTemplate("daily_uncommon_enderman", QuestCategory.DAILY, QuestRarity.UNCOMMON, "quest.questlog.daily_uncommon_enderman", ObjectiveType.KILL_ENTITY, "minecraft:enderman", 3, "minecraft:ender_pearl", 5, 5, 65),
      new QuestTemplate("daily_uncommon_witch", QuestCategory.DAILY, QuestRarity.UNCOMMON, "quest.questlog.daily_uncommon_witch", ObjectiveType.KILL_ENTITY, "minecraft:witch", 1, "minecraft:redstone", 16, 16, 65),
      new QuestTemplate("daily_uncommon_slime", QuestCategory.DAILY, QuestRarity.UNCOMMON, "quest.questlog.daily_uncommon_slime", ObjectiveType.KILL_ENTITY, "minecraft:slime", 10, "minecraft:slime_ball", 16, 16, 55),
      new QuestTemplate("daily_uncommon_pillager", QuestCategory.DAILY, QuestRarity.UNCOMMON, "quest.questlog.daily_uncommon_pillager", ObjectiveType.KILL_ENTITY, "minecraft:villager", 1, "minecraft:emerald", 20, 20, 65),
      new QuestTemplate("daily_uncommon_smelter", QuestCategory.DAILY, QuestRarity.UNCOMMON, "quest.questlog.daily_uncommon_smelter", ObjectiveType.SMELT_ITEM, "any", 40, "minecraft:iron_ingot", 6, 6, 55),
      new QuestTemplate("daily_uncommon_wanderer", QuestCategory.DAILY, QuestRarity.UNCOMMON, "quest.questlog.daily_uncommon_wanderer", ObjectiveType.TRAVEL_DISTANCE, "any", 1600, "minecraft:golden_carrot", 10, 10, 60),
      new QuestTemplate("daily_uncommon_explorer", QuestCategory.DAILY, QuestRarity.UNCOMMON, "quest.questlog.daily_uncommon_explorer", ObjectiveType.VISIT_BIOME, "any", 3, "minecraft:emerald", 6, 6, 60),
      new QuestTemplate("daily_uncommon_collector", QuestCategory.DAILY, QuestRarity.UNCOMMON, "quest.questlog.daily_uncommon_collector", ObjectiveType.COLLECT_DISTINCT_ITEMS, "any", 8, "minecraft:emerald", 6, 6, 60),
      new QuestTemplate("daily_uncommon_brewer", QuestCategory.DAILY, QuestRarity.UNCOMMON, "quest.questlog.daily_uncommon_brewer", ObjectiveType.BREW_POTION, "any", 3, "minecraft:emerald", 25, 25, 65),
      new QuestTemplate("daily_uncommon_trader", QuestCategory.DAILY, QuestRarity.UNCOMMON, "quest.questlog.daily_uncommon_trader", ObjectiveType.TRADE_VILLAGER, "any", 3, "minecraft:emerald", 8, 8, 55),
      new QuestTemplate("daily_uncommon_enchanter", QuestCategory.DAILY, QuestRarity.UNCOMMON, "quest.questlog.daily_uncommon_enchanter", ObjectiveType.ENCHANT_ITEM, "any", 1, "minecraft:lapis_lazuli", 16, 16, 55),
      new QuestTemplate("daily_uncommon_woodcutter", QuestCategory.DAILY, QuestRarity.UNCOMMON, "quest.questlog.daily_uncommon_woodcutter", ObjectiveType.MINE_BLOCK, "#minecraft:logs", 64, "minecraft:iron_axe", 1, 1, 55),
      new QuestTemplate("daily_uncommon_deep_slate", QuestCategory.DAILY, QuestRarity.UNCOMMON, "quest.questlog.daily_uncommon_deep_slate", ObjectiveType.MINE_BLOCK, "minecraft:deepslate", 64, "minecraft:iron_ingot", 8, 8, 55),
      new QuestTemplate("daily_uncommon_obsidian", QuestCategory.DAILY, QuestRarity.UNCOMMON, "quest.questlog.daily_uncommon_obsidian", ObjectiveType.MINE_BLOCK, "minecraft:obsidian", 4, "minecraft:emerald", 18, 18, 60),
      new QuestTemplate("daily_uncommon_builder2", QuestCategory.DAILY, QuestRarity.UNCOMMON, "quest.questlog.daily_uncommon_builder2", ObjectiveType.PLACE_BLOCK, "#minecraft:wool", 32, "minecraft:string", 64, 64, 55),
      new QuestTemplate("daily_uncommon_rancher2", QuestCategory.DAILY, QuestRarity.UNCOMMON, "quest.questlog.daily_uncommon_rancher2", ObjectiveType.BREED_ANIMAL, "any", 6, "minecraft:golden_apple", 1, 1, 60),
      new QuestTemplate("daily_uncommon_collector2", QuestCategory.DAILY, QuestRarity.UNCOMMON, "quest.questlog.daily_uncommon_collector2", ObjectiveType.COLLECT_DISTINCT_ITEMS, "any", 10, "minecraft:emerald", 8, 8, 65),
      new QuestTemplate("daily_uncommon_crafter2", QuestCategory.DAILY, QuestRarity.UNCOMMON, "quest.questlog.daily_uncommon_crafter2", ObjectiveType.CRAFT_ITEM, "minecraft:wooden_pickaxe", 3, "minecraft:iron_ingot", 6, 6, 50),
      new QuestTemplate("daily_uncommon_shield_crafter", QuestCategory.DAILY, QuestRarity.UNCOMMON, "quest.questlog.daily_uncommon_shield_crafter", ObjectiveType.CRAFT_ITEM, "minecraft:shield", 1, "minecraft:iron_ingot", 8, 8, 55),
      new QuestTemplate("daily_uncommon_dolphin_swim", QuestCategory.DAILY, QuestRarity.UNCOMMON, "quest.questlog.daily_uncommon_dolphin_swim", ObjectiveType.TRAVEL_DISTANCE, "any", 3000, "minecraft:heart_of_the_sea", 1, 1, 70),
      new QuestTemplate("daily_uncommon_stray", QuestCategory.DAILY, QuestRarity.UNCOMMON, "quest.questlog.daily_uncommon_stray", ObjectiveType.KILL_ENTITY, "minecraft:stray", 5, "minecraft:bone", 32, 32, 55),
      new QuestTemplate("daily_uncommon_vindicator", QuestCategory.DAILY, QuestRarity.UNCOMMON, "quest.questlog.daily_uncommon_vindicator", ObjectiveType.KILL_ENTITY, "minecraft:pig", 5, "minecraft:cooked_porkchop", 16, 16, 65),
      new QuestTemplate("daily_uncommon_nether_visit", QuestCategory.DAILY, QuestRarity.UNCOMMON, "quest.questlog.daily_uncommon_nether_visit", ObjectiveType.VISIT_DIMENSION, "minecraft:the_nether", 1, "minecraft:magma_cream", 16, 16, 60),
      new QuestTemplate("daily_uncommon_smelt_ore", QuestCategory.DAILY, QuestRarity.UNCOMMON, "quest.questlog.daily_uncommon_smelt_ore", ObjectiveType.SMELT_ITEM, "#minecraft:iron_ores", 12, "minecraft:iron_ingot", 8, 8, 60),
      new QuestTemplate("daily_uncommon_map_maker", QuestCategory.DAILY, QuestRarity.UNCOMMON, "quest.questlog.daily_uncommon_map_maker", ObjectiveType.CRAFT_ITEM, "minecraft:map", 1, "minecraft:redstone", 16, 16, 55),
      new QuestTemplate("daily_uncommon_sniffer_feed", QuestCategory.DAILY, QuestRarity.UNCOMMON, "quest.questlog.daily_uncommon_sniffer_feed", ObjectiveType.BREED_ANIMAL, "any", 4, "minecraft:torchflower_seeds", 4, 4, 55),
      new QuestTemplate("daily_uncommon_trident_hunt", QuestCategory.DAILY, QuestRarity.UNCOMMON, "quest.questlog.daily_uncommon_trident_hunt", ObjectiveType.KILL_ENTITY, "minecraft:drowned", 12, "minecraft:prismarine_shard", 32, 32, 60),
      new QuestTemplate("daily_uncommon_camper", QuestCategory.DAILY, QuestRarity.UNCOMMON, "quest.questlog.daily_uncommon_camper", ObjectiveType.CRAFT_ITEM, "minecraft:campfire", 2, "minecraft:sugar_cane", 10, 10, 50),

      // --- RARE (28) — noticeable investment -------------------------------------------------------------------
      new QuestTemplate("daily_rare_gold", QuestCategory.DAILY, QuestRarity.RARE, "quest.questlog.daily_rare_gold", ObjectiveType.MINE_BLOCK, "#minecraft:gold_ores", 20, "minecraft:gold_ingot", 14, 14, 100),
      new QuestTemplate("daily_rare_hunter", QuestCategory.DAILY, QuestRarity.RARE, "quest.questlog.daily_rare_hunter", ObjectiveType.KILL_ENTITY, "hostile", 30, "minecraft:diamond", 1, 2, 110),
      new QuestTemplate("daily_rare_enchanter", QuestCategory.DAILY, QuestRarity.RARE, "quest.questlog.daily_rare_enchanter", ObjectiveType.ENCHANT_ITEM, "any", 2, "minecraft:lapis_lazuli", 32, 32, 90),
      new QuestTemplate("daily_rare_collector", QuestCategory.DAILY, QuestRarity.RARE, "quest.questlog.daily_rare_collector", ObjectiveType.COLLECT_DISTINCT_ITEMS, "any", 12, "minecraft:emerald", 10, 10, 95),
      new QuestTemplate("daily_rare_explorer", QuestCategory.DAILY, QuestRarity.RARE, "quest.questlog.daily_rare_explorer", ObjectiveType.VISIT_BIOME, "any", 3, "minecraft:diamond", 1, 1, 100),
      new QuestTemplate("daily_rare_nether", QuestCategory.DAILY, QuestRarity.RARE, "quest.questlog.daily_rare_nether", ObjectiveType.VISIT_DIMENSION, "minecraft:the_nether", 1, "minecraft:glowstone", 64, 64, 90),
      new QuestTemplate("daily_rare_diamond_taste", QuestCategory.DAILY, QuestRarity.RARE, "quest.questlog.daily_rare_diamond_taste", ObjectiveType.MINE_BLOCK, "#minecraft:diamond_ores", 3, "minecraft:diamond", 2, 2, 110),
      new QuestTemplate("daily_rare_blaze_hunter", QuestCategory.DAILY, QuestRarity.RARE, "quest.questlog.daily_rare_blaze_hunter", ObjectiveType.KILL_ENTITY, "minecraft:blaze", 10, "minecraft:blaze_rod", 3, 3, 110),
      new QuestTemplate("daily_rare_wither_skeleton", QuestCategory.DAILY, QuestRarity.RARE, "quest.questlog.daily_rare_wither_skeleton", ObjectiveType.KILL_ENTITY, "minecraft:wither_skeleton", 5, "minecraft:coal", 32, 32, 110),
      new QuestTemplate("daily_rare_smelter", QuestCategory.DAILY, QuestRarity.RARE, "quest.questlog.daily_rare_smelter", ObjectiveType.SMELT_ITEM, "any", 100, "minecraft:diamond", 1, 1, 100),
      new QuestTemplate("daily_rare_trader", QuestCategory.DAILY, QuestRarity.RARE, "quest.questlog.daily_rare_trader", ObjectiveType.TRADE_VILLAGER, "any", 5, "minecraft:emerald", 16, 16, 95),
      new QuestTemplate("daily_rare_brewer", QuestCategory.DAILY, QuestRarity.RARE, "quest.questlog.daily_rare_brewer", ObjectiveType.BREW_POTION, "any", 5, "minecraft:diamond", 2, 2, 115),
      new QuestTemplate("daily_rare_rancher", QuestCategory.DAILY, QuestRarity.RARE, "quest.questlog.daily_rare_rancher", ObjectiveType.BREED_ANIMAL, "any", 10, "minecraft:golden_apple", 2, 2, 100),
      new QuestTemplate("daily_rare_builder", QuestCategory.DAILY, QuestRarity.RARE, "quest.questlog.daily_rare_builder", ObjectiveType.PLACE_BLOCK, "any", 150, "minecraft:diamond", 1, 1, 95),
      new QuestTemplate("daily_rare_wanderer", QuestCategory.DAILY, QuestRarity.RARE, "quest.questlog.daily_rare_wanderer", ObjectiveType.TRAVEL_DISTANCE, "any", 3000, "minecraft:ender_pearl", 4, 4, 100),
      new QuestTemplate("daily_rare_ancient_city", QuestCategory.DAILY, QuestRarity.RARE, "quest.questlog.daily_rare_ancient_city", ObjectiveType.VISIT_BIOME, "minecraft:deep_dark", 1, "minecraft:echo_shard", 2, 2, 120),
      new QuestTemplate("daily_rare_end_visit", QuestCategory.DAILY, QuestRarity.RARE, "quest.questlog.daily_rare_end_visit", ObjectiveType.VISIT_DIMENSION, "minecraft:the_end", 1, "minecraft:ender_pearl", 32, 32, 110),
      new QuestTemplate("daily_rare_piglin_trade", QuestCategory.DAILY, QuestRarity.RARE, "quest.questlog.daily_rare_piglin_trade", ObjectiveType.KILL_ENTITY, "minecraft:hoglin", 6, "minecraft:leather", 16, 16, 100),
      new QuestTemplate("daily_rare_crafter", QuestCategory.DAILY, QuestRarity.RARE, "quest.questlog.daily_rare_crafter", ObjectiveType.CRAFT_ITEM, "minecraft:diamond_pickaxe", 1, "minecraft:diamond", 2, 2, 105),
      new QuestTemplate("daily_rare_diamond_armor", QuestCategory.DAILY, QuestRarity.RARE, "quest.questlog.daily_rare_diamond_armor", ObjectiveType.CRAFT_ITEM, "minecraft:diamond_chestplate", 1, "minecraft:diamond", 2, 2, 105),
      new QuestTemplate("daily_rare_angler", QuestCategory.DAILY, QuestRarity.RARE, "quest.questlog.daily_rare_angler", ObjectiveType.CATCH_FISH, "any", 10, "minecraft:golden_carrot", 32, 32, 95),
      new QuestTemplate("daily_rare_ghast_hunt", QuestCategory.DAILY, QuestRarity.RARE, "quest.questlog.daily_rare_ghast_hunt", ObjectiveType.KILL_ENTITY, "minecraft:ghast", 3, "minecraft:ghast_tear", 5, 5, 115),
      new QuestTemplate("daily_rare_shulker_hunt", QuestCategory.DAILY, QuestRarity.RARE, "quest.questlog.daily_rare_shulker_hunt", ObjectiveType.KILL_ENTITY, "minecraft:shulker", 1, "minecraft:shulker_shell", 2, 2, 120),
      new QuestTemplate("daily_rare_deepslate_diamonds", QuestCategory.DAILY, QuestRarity.RARE, "quest.questlog.daily_rare_deepslate_diamonds", ObjectiveType.MINE_BLOCK, "minecraft:deepslate_diamond_ore", 2, "minecraft:diamond", 2, 2, 115),
      new QuestTemplate("daily_rare_collector2", QuestCategory.DAILY, QuestRarity.RARE, "quest.questlog.daily_rare_collector2", ObjectiveType.COLLECT_DISTINCT_ITEMS, "any", 20, "minecraft:diamond", 1, 1, 105),
      new QuestTemplate("daily_rare_evoker_hunt", QuestCategory.DAILY, QuestRarity.RARE, "quest.questlog.daily_rare_evoker_hunt", ObjectiveType.KILL_ENTITY, "minecraft:evoker", 1, "minecraft:totem_of_undying", 1, 1, 130),
      new QuestTemplate("daily_rare_lapis_miner", QuestCategory.DAILY, QuestRarity.RARE, "quest.questlog.daily_rare_lapis_miner", ObjectiveType.MINE_BLOCK, "#minecraft:lapis_ores", 24, "minecraft:lapis_lazuli", 32, 32, 95),
      new QuestTemplate("daily_rare_emerald_miner", QuestCategory.DAILY, QuestRarity.RARE, "quest.questlog.daily_rare_emerald_miner", ObjectiveType.MINE_BLOCK, "#minecraft:emerald_ores", 1, "minecraft:emerald", 10, 10, 105),
           new QuestTemplate("daily_epic_raid_survivor", QuestCategory.DAILY, QuestRarity.EPIC, "quest.questlog.daily_epic_raid_survivor", ObjectiveType.KILL_ENTITY, "minecraft:ravager", 1, "minecraft:emerald", 5, 10, 210),


           // --- EPIC (18) — significant challenge -------------------------------------------------------------------
      new QuestTemplate("daily_epic_diamond", QuestCategory.DAILY, QuestRarity.EPIC, "quest.questlog.daily_epic_diamond", ObjectiveType.MINE_BLOCK, "#minecraft:diamond_ores", 5, "minecraft:diamond", 5, 5, 180),
      new QuestTemplate("daily_epic_hunter", QuestCategory.DAILY, QuestRarity.EPIC, "quest.questlog.daily_epic_hunter", ObjectiveType.KILL_ENTITY, "hostile", 55, "minecraft:diamond", 2, 2, 200),
      new QuestTemplate("daily_epic_architect", QuestCategory.DAILY, QuestRarity.EPIC, "quest.questlog.daily_epic_architect", ObjectiveType.PLACE_BLOCK, "any", 300, "minecraft:emerald", 18, 18, 170),
      new QuestTemplate("daily_epic_sprinter", QuestCategory.DAILY, QuestRarity.EPIC, "quest.questlog.daily_epic_sprinter", ObjectiveType.TRAVEL_DISTANCE, "any", 5000, QuestManager.SWIFTNESS_II_REWARD_ID, 1, 1, 160),
      new QuestTemplate("daily_epic_angler", QuestCategory.DAILY, QuestRarity.EPIC, "quest.questlog.daily_epic_angler", ObjectiveType.CATCH_FISH, "any", 14, "minecraft:golden_apple", 2, 2, 180),
      new QuestTemplate("daily_epic_smelter", QuestCategory.DAILY, QuestRarity.EPIC, "quest.questlog.daily_epic_smelter", ObjectiveType.SMELT_ITEM, "any", 200, "minecraft:iron_block", 4, 4, 160),
      new QuestTemplate("daily_epic_enchanter", QuestCategory.DAILY, QuestRarity.EPIC, "quest.questlog.daily_epic_enchanter", ObjectiveType.ENCHANT_ITEM, "any", 4, "minecraft:experience_bottle", 32, 32, 190),
      new QuestTemplate("daily_epic_wither_slayer", QuestCategory.DAILY, QuestRarity.EPIC, "quest.questlog.daily_epic_wither_slayer", ObjectiveType.KILL_ENTITY, "minecraft:wither_skeleton", 20, "minecraft:wither_skeleton_skull", 1, 1, 220),
      new QuestTemplate("daily_epic_end_explorer", QuestCategory.DAILY, QuestRarity.EPIC, "quest.questlog.daily_epic_end_explorer", ObjectiveType.VISIT_DIMENSION, "minecraft:the_end", 1, "minecraft:obsidian", 32, 32, 190),
      new QuestTemplate("daily_epic_trader", QuestCategory.DAILY, QuestRarity.EPIC, "quest.questlog.daily_epic_trader", ObjectiveType.TRADE_VILLAGER, "any", 10, "minecraft:emerald_block", 2, 2, 180),
      new QuestTemplate("daily_epic_rancher", QuestCategory.DAILY, QuestRarity.EPIC, "quest.questlog.daily_epic_rancher", ObjectiveType.BREED_ANIMAL, "any", 20, "minecraft:golden_apple", 3, 3, 190),
      new QuestTemplate("daily_epic_ancient_debris", QuestCategory.DAILY, QuestRarity.EPIC, "quest.questlog.daily_epic_ancient_debris", ObjectiveType.MINE_BLOCK, "minecraft:ancient_debris", 3, "minecraft:diamond", 6, 6, 210),
      new QuestTemplate("daily_epic_biome_hopper", QuestCategory.DAILY, QuestRarity.EPIC, "quest.questlog.daily_epic_biome_hopper", ObjectiveType.VISIT_BIOME, "any", 5, "minecraft:iron_ingot", 32, 32, 190),
      new QuestTemplate("daily_epic_collector", QuestCategory.DAILY, QuestRarity.EPIC, "quest.questlog.daily_epic_collector", ObjectiveType.COLLECT_DISTINCT_ITEMS, "any", 15, "minecraft:diamond", 2, 2, 180),
      new QuestTemplate("daily_epic_brewer", QuestCategory.DAILY, QuestRarity.EPIC, "quest.questlog.daily_epic_brewer", ObjectiveType.BREW_POTION, "any", 10, "minecraft:diamond", 5, 5, 210),
      new QuestTemplate("daily_epic_underground", QuestCategory.DAILY, QuestRarity.EPIC, "quest.questlog.daily_epic_underground", ObjectiveType.MINE_BLOCK, QuestManager.UNDERGROUND_ORES_AND_STONE, 200, "minecraft:diamond", 2, 3, 190),
      new QuestTemplate("daily_epic_crafter", QuestCategory.DAILY, QuestRarity.EPIC, "quest.questlog.daily_epic_crafter", ObjectiveType.CRAFT_ITEM, "any", 300, "minecraft:diamond", 4, 4, 170),

      // --- LEGENDARY (11) — large investment, unusual objectives -------------------------------------------------
      new QuestTemplate("daily_legendary_ores", QuestCategory.DAILY, QuestRarity.LEGENDARY, "quest.questlog.daily_legendary_ores", ObjectiveType.MINE_BLOCK, QuestManager.UNDERGROUND_ORES_AND_STONE, 400, "minecraft:diamond", 6, 6, 350),
      new QuestTemplate("daily_legendary_hunter", QuestCategory.DAILY, QuestRarity.LEGENDARY, "quest.questlog.daily_legendary_hunter", ObjectiveType.KILL_ENTITY, "hostile", 100, "minecraft:diamond", 7, 7, 380),
      new QuestTemplate("daily_legendary_builder", QuestCategory.DAILY, QuestRarity.LEGENDARY, "quest.questlog.daily_legendary_builder", ObjectiveType.PLACE_BLOCK, "any", 600, "minecraft:emerald_block", 4, 4, 320),
      new QuestTemplate("daily_legendary_explorer", QuestCategory.DAILY, QuestRarity.LEGENDARY, "quest.questlog.daily_legendary_explorer", ObjectiveType.VISIT_BIOME, "any", 6, "minecraft:diamond", 4, 4, 340),
      new QuestTemplate("daily_legendary_collector", QuestCategory.DAILY, QuestRarity.LEGENDARY, "quest.questlog.daily_legendary_collector", ObjectiveType.COLLECT_DISTINCT_ITEMS, "any", 30, "minecraft:diamond", 5, 5, 330),
      new QuestTemplate("daily_legendary_angler", QuestCategory.DAILY, QuestRarity.LEGENDARY, "quest.questlog.daily_legendary_angler", ObjectiveType.CATCH_FISH, "any", 25, QuestManager.LURE_UNBREAKING_BOOK_REWARD_ID, 1, 1, 350),
      new QuestTemplate("daily_legendary_ender_dragon_prep", QuestCategory.DAILY, QuestRarity.LEGENDARY, "quest.questlog.daily_legendary_ender_dragon_prep", ObjectiveType.KILL_ENTITY, "minecraft:enderman", 30, "minecraft:netherite_scrap", 1, 1, 370),
      new QuestTemplate("daily_legendary_enchanter", QuestCategory.DAILY, QuestRarity.LEGENDARY, "quest.questlog.daily_legendary_enchanter", ObjectiveType.ENCHANT_ITEM, "any", 8, "minecraft:lapis_block", 16, 16, 360),
      new QuestTemplate("daily_legendary_trader", QuestCategory.DAILY, QuestRarity.LEGENDARY, "quest.questlog.daily_legendary_trader", ObjectiveType.TRADE_VILLAGER, "any", 20, "minecraft:diamond", 6, 6, 340),
      new QuestTemplate("daily_legendary_wither_slayer", QuestCategory.DAILY, QuestRarity.LEGENDARY, "quest.questlog.daily_legendary_wither_slayer", ObjectiveType.KILL_ENTITY, "minecraft:ender_dragon", 1, "minecraft:end_crystal", 16, 32, 400),
      new QuestTemplate("daily_legendary_traveler", QuestCategory.DAILY, QuestRarity.LEGENDARY, "quest.questlog.daily_legendary_traveler", ObjectiveType.TRAVEL_DISTANCE, "any", 12000, "minecraft:diamond", 6, 6, 350),

      // --- MYTHIC (7) — very difficult and special --------------------------------------------------------------
      new QuestTemplate("daily_mythic_ores", QuestCategory.DAILY, QuestRarity.MYTHIC, "quest.questlog.daily_mythic_ores", ObjectiveType.MINE_BLOCK, QuestManager.UNDERGROUND_ORES_AND_STONE, 800, "minecraft:netherite_scrap", 3, 3, 700),
      new QuestTemplate("daily_mythic_hunter", QuestCategory.DAILY, QuestRarity.MYTHIC, "quest.questlog.daily_mythic_hunter", ObjectiveType.KILL_ENTITY, "hostile", 150, "minecraft:diamond_block", 2, 2, 700),
      new QuestTemplate("daily_mythic_builder", QuestCategory.DAILY, QuestRarity.MYTHIC, "quest.questlog.daily_mythic_builder", ObjectiveType.PLACE_BLOCK, "any", 1000, "minecraft:diamond", 10, 10, 650),
      new QuestTemplate("daily_mythic_explorer", QuestCategory.DAILY, QuestRarity.MYTHIC, "quest.questlog.daily_mythic_explorer", ObjectiveType.VISIT_BIOME, "any", 15, "minecraft:netherite_ingot", 1, 1, 700),
      new QuestTemplate("daily_mythic_traveler", QuestCategory.DAILY, QuestRarity.MYTHIC, "quest.questlog.daily_mythic_traveler", ObjectiveType.TRAVEL_DISTANCE, "any", 20000, "minecraft:diamond", 12, 12, 700),
      new QuestTemplate("daily_mythic_angler", QuestCategory.DAILY, QuestRarity.MYTHIC, "quest.questlog.daily_mythic_angler", ObjectiveType.CATCH_FISH, "any", 40, "minecraft:trident", 1, 1, 700),
           new QuestTemplate("daily_mythic_end_dive", QuestCategory.DAILY, QuestRarity.MYTHIC, "quest.questlog.daily_mythic_end_dive", ObjectiveType.VISIT_DIMENSION, "minecraft:the_end", 1, "minecraft:elytra", 1, 1, 110),

           new QuestTemplate("daily_mythic_wither_slayer", QuestCategory.DAILY, QuestRarity.MYTHIC, "quest.questlog.daily_mythic_wither_slayer", ObjectiveType.KILL_ENTITY, "minecraft:wither", 2, "minecraft:netherite_ingot", 1, 1, 750),
      new QuestTemplate("daily_mythic_bacon", QuestCategory.DAILY, QuestRarity.MYTHIC, "quest.questlog.daily_mythic_bacon", ObjectiveType.KILL_ENTITY, "minecraft:pig", 1000, "minecraft:netherite_block", 1, 1, 750)
   );

   public static final List<QuestTemplate> WEEKLY = List.of(
      new QuestTemplate("weekly_master_miner", QuestCategory.WEEKLY, null, "quest.questlog.weekly_master_miner", ObjectiveType.MINE_BLOCK, QuestManager.UNDERGROUND_ORES_AND_STONE, 2000, "minecraft:diamond", 24, 24, 500),
      new QuestTemplate("weekly_enchanter", QuestCategory.WEEKLY, null, "quest.questlog.weekly_enchanter", ObjectiveType.ENCHANT_ITEM, "any", 30, "minecraft:lapis_lazuli", 128, 128, 700),
      new QuestTemplate("weekly_deep_diamonds", QuestCategory.WEEKLY, null, "quest.questlog.weekly_deep_diamonds", ObjectiveType.MINE_BLOCK, "#minecraft:diamond_ores", 64, "minecraft:netherite_scrap", 4, 4, 600),
      new QuestTemplate("weekly_monster_slayer", QuestCategory.WEEKLY, null, "quest.questlog.weekly_monster_slayer", ObjectiveType.KILL_ENTITY, "hostile", 300, "minecraft:diamond", 15, 15, 500),
      new QuestTemplate("weekly_ghast_hunter", QuestCategory.WEEKLY, null, "quest.questlog.weekly_ghast_hunter", ObjectiveType.KILL_ENTITY, "minecraft:ghast", 20, "minecraft:ghast_tear", 20, 20, 500),
      new QuestTemplate("weekly_world_traveler", QuestCategory.WEEKLY, null, "quest.questlog.weekly_world_traveler", ObjectiveType.VISIT_BIOME, "any", 15, "minecraft:trident", 1, 1, 400),
      new QuestTemplate("weekly_globetrotter", QuestCategory.WEEKLY, null, "quest.questlog.weekly_globetrotter", ObjectiveType.TRAVEL_DISTANCE, "any", 50000, "minecraft:golden_carrot", 32, 32, 400),
      new QuestTemplate("weekly_master_chef", QuestCategory.WEEKLY, null, "quest.questlog.weekly_master_chef", ObjectiveType.SMELT_ITEM, "any", 450, "minecraft:golden_apple", 5, 5, 500),
      new QuestTemplate("weekly_builder", QuestCategory.WEEKLY, null, "quest.questlog.weekly_builder", ObjectiveType.PLACE_BLOCK, "any", 1500, "minecraft:emerald_block", 6, 6, 700),
      new QuestTemplate("weekly_woodcutter", QuestCategory.WEEKLY, null, "quest.questlog.weekly_woodcutter", ObjectiveType.MINE_BLOCK, "#minecraft:logs", 500, "minecraft:diamond", 8, 8, 600),
      new QuestTemplate("weekly_rancher", QuestCategory.WEEKLY, null, "quest.questlog.weekly_rancher", ObjectiveType.BREED_ANIMAL, "any", 30, "minecraft:golden_apple", 6, 6, 600),
      new QuestTemplate("weekly_collector", QuestCategory.WEEKLY, null, "quest.questlog.weekly_collector", ObjectiveType.COLLECT_DISTINCT_ITEMS, "any", 25, "minecraft:emerald", 40, 40, 650),
      new QuestTemplate("weekly_angler", QuestCategory.WEEKLY, null, "quest.questlog.weekly_angler", ObjectiveType.CATCH_FISH, "any", 60, "minecraft:diamond", 6, 6, 600),
      new QuestTemplate("weekly_crafting_spree", QuestCategory.WEEKLY, null, "quest.questlog.weekly_crafting_spree", ObjectiveType.CRAFT_ITEM, "any", 800, "minecraft:experience_bottle", 32, 32, 500),
      new QuestTemplate("weekly_end_explorer", QuestCategory.WEEKLY, null, "quest.questlog.weekly_end_explorer", ObjectiveType.VISIT_DIMENSION, "minecraft:the_end", 1, "minecraft:shulker_shell", 4, 4, 700),
      new QuestTemplate("weekly_brewer", QuestCategory.WEEKLY, null, "quest.questlog.weekly_brewer", ObjectiveType.BREW_POTION, "any", 20, "minecraft:diamond", 10, 10, 600),
      new QuestTemplate("weekly_merchant", QuestCategory.WEEKLY, null, "quest.questlog.weekly_merchant", ObjectiveType.TRADE_VILLAGER, "any", 30, "minecraft:emerald_block", 4, 4, 550),
      new QuestTemplate("weekly_wither_hunter", QuestCategory.WEEKLY, null, "quest.questlog.weekly_wither_hunter", ObjectiveType.KILL_ENTITY, "minecraft:wither_skeleton", 40, "minecraft:netherite_scrap", 3, 3, 650),
      new QuestTemplate("weekly_ancient_debris_hunt", QuestCategory.WEEKLY, null, "quest.questlog.weekly_ancient_debris_hunt", ObjectiveType.MINE_BLOCK, "minecraft:ancient_debris", 12, "minecraft:netherite_ingot", 1, 1, 800),
      new QuestTemplate("weekly_beacon_builder", QuestCategory.WEEKLY, null, "quest.questlog.weekly_beacon_builder", ObjectiveType.MINE_BLOCK, "#minecraft:diamond_ores", 100, "minecraft:diamond", 20, 20, 700),
      new QuestTemplate("weekly_biome_collector", QuestCategory.WEEKLY, null, "quest.questlog.weekly_biome_collector", ObjectiveType.VISIT_BIOME, "any", 25, "minecraft:diamond", 12, 12, 600),
      new QuestTemplate("weekly_shulker_hunter", QuestCategory.WEEKLY, null, "quest.questlog.weekly_shulker_hunter", ObjectiveType.KILL_ENTITY, "minecraft:shulker", 6, "minecraft:shulker_shell", 4, 4, 700),
      new QuestTemplate("weekly_evoker_hunter", QuestCategory.WEEKLY, null, "quest.questlog.weekly_evoker_hunter", ObjectiveType.KILL_ENTITY, "minecraft:evoker", 8, "minecraft:totem_of_undying", 1, 1, 750),
      new QuestTemplate("weekly_dedicated_farmer", QuestCategory.WEEKLY, null, "quest.questlog.weekly_dedicated_farmer", ObjectiveType.MINE_BLOCK, "crops", 400, "minecraft:golden_apple", 4, 4, 500),
      new QuestTemplate("weekly_enchant_master", QuestCategory.WEEKLY, null, "quest.questlog.weekly_enchant_master", ObjectiveType.ENCHANT_ITEM, "any", 15, "minecraft:diamond", 16, 16, 650),
      new QuestTemplate("weekly_stone_age", QuestCategory.WEEKLY, null, "quest.questlog.weekly_stone_age", ObjectiveType.MINE_BLOCK, "#minecraft:base_stone_overworld", 3000, "minecraft:diamond", 10, 10, 500),
      new QuestTemplate("weekly_hoglin_hunter", QuestCategory.WEEKLY, null, "quest.questlog.weekly_hoglin_hunter", ObjectiveType.KILL_ENTITY, "minecraft:hoglin", 20, "minecraft:netherite_scrap", 2, 2, 650),
      new QuestTemplate("weekly_pillager_purge", QuestCategory.WEEKLY, null, "quest.questlog.weekly_pillager_purge", ObjectiveType.KILL_ENTITY, "minecraft:pillager", 40, "minecraft:emerald", 60, 60, 550),
      new QuestTemplate("weekly_underwater_explorer", QuestCategory.WEEKLY, null, "quest.questlog.weekly_underwater_explorer", ObjectiveType.KILL_ENTITY, "minecraft:guardian", 15, "minecraft:prismarine_crystals", 64, 64, 600),
      new QuestTemplate("weekly_placement_spree", QuestCategory.WEEKLY, null, "quest.questlog.weekly_placement_spree", ObjectiveType.PLACE_BLOCK, "#minecraft:planks", 2000, "minecraft:diamond", 8, 8, 550),
      new QuestTemplate("weekly_item_hoarder", QuestCategory.WEEKLY, null, "quest.questlog.weekly_item_hoarder", ObjectiveType.COLLECT_DISTINCT_ITEMS, "any", 40, "minecraft:diamond", 10, 10, 600),
      new QuestTemplate("weekly_smelter_supreme", QuestCategory.WEEKLY, null, "quest.questlog.weekly_smelter_supreme", ObjectiveType.SMELT_ITEM, "#minecraft:iron_ores", 128, "minecraft:iron_block", 8, 8, 550),
      new QuestTemplate("weekly_amethyst_hunter", QuestCategory.WEEKLY, null, "quest.questlog.weekly_amethyst_hunter", ObjectiveType.MINE_BLOCK, "minecraft:amethyst_cluster", 32, "minecraft:amethyst_shard", 40, 40, 500),
      new QuestTemplate("weekly_rare_fisher", QuestCategory.WEEKLY, null, "quest.questlog.weekly_rare_fisher", ObjectiveType.CATCH_FISH, "any", 120, "minecraft:diamond", 10, 10, 600),
      new QuestTemplate("weekly_ravager_hunter", QuestCategory.WEEKLY, null, "quest.questlog.weekly_ravager_hunter", ObjectiveType.KILL_ENTITY, "minecraft:ravager", 5, "minecraft:diamond", 14, 14, 650)
   );

   public static final List<QuestTemplate> MONTHLY = List.of(
      new QuestTemplate("monthly_master_explorer", QuestCategory.MONTHLY, null, "quest.questlog.monthly_master_explorer", ObjectiveType.VISIT_BIOME, "any", 50, "minecraft:netherite_ingot", 2, 2, 2000),
      new QuestTemplate("monthly_treasure_hoarder", QuestCategory.MONTHLY, null, "quest.questlog.monthly_treasure_hoarder", ObjectiveType.MINE_BLOCK, "#minecraft:diamond_ores", 1000, "minecraft:beacon", 10, 10, 7500),
      new QuestTemplate("monthly_diamond_enjoyer", QuestCategory.MONTHLY, null, "quest.questlog.monthly_diamond_enjoyer", ObjectiveType.MINE_BLOCK, "#minecraft:diamond_ores", 500, "minecraft:ancient_debris", 64, 64, 3000),
      new QuestTemplate("monthly_fishing_enjoyer", QuestCategory.MONTHLY, null, "quest.questlog.monthly_fishing_enjoyer", ObjectiveType.CATCH_FISH, "any", 500, "minecraft:enchanted_golden_apple", 3, 3, 2500),
      new QuestTemplate("monthly_fish_master", QuestCategory.MONTHLY, null, "quest.questlog.monthly_fish_master", ObjectiveType.CATCH_FISH, "any", 1000, QuestManager.DENSITY_MACE_REWARD_ID, 1, 1, 5000),
      new QuestTemplate("monthly_enchanter_grandmaster", QuestCategory.MONTHLY, null, "quest.questlog.monthly_enchanter_grandmaster", ObjectiveType.ENCHANT_ITEM, "any", 30, "minecraft:diamond", 20, 20, 3000),
      new QuestTemplate("monthly_builder_grandmaster", QuestCategory.MONTHLY, null, "quest.questlog.monthly_builder_grandmaster", ObjectiveType.PLACE_BLOCK, "any", 5000, "minecraft:netherite_ingot", 2, 2, 3000),
      new QuestTemplate("monthly_master_slayer", QuestCategory.MONTHLY, null, "quest.questlog.monthly_master_slayer", ObjectiveType.KILL_ENTITY, "hostile", 800, "minecraft:netherite_ingot", 2, 2, 3200),
      new QuestTemplate("monthly_master_trader", QuestCategory.MONTHLY, null, "quest.questlog.monthly_master_trader", ObjectiveType.TRADE_VILLAGER, "any", 100, "minecraft:diamond_block", 4, 4, 2800),
      new QuestTemplate("monthly_master_brewer", QuestCategory.MONTHLY, null, "quest.questlog.monthly_master_brewer", ObjectiveType.BREW_POTION, "any", 50, "minecraft:diamond_block", 4, 4, 2800),
      new QuestTemplate("monthly_master_rancher", QuestCategory.MONTHLY, null, "quest.questlog.monthly_master_rancher", ObjectiveType.BREED_ANIMAL, "any", 100, "minecraft:golden_apple", 10, 10, 2400),
      new QuestTemplate("monthly_deep_dark_explorer", QuestCategory.MONTHLY, null, "quest.questlog.monthly_deep_dark_explorer", ObjectiveType.VISIT_BIOME, "minecraft:deep_dark", 1, "minecraft:echo_shard", 8, 8, 3200),
      new QuestTemplate("monthly_wither_grandmaster", QuestCategory.MONTHLY, null, "quest.questlog.monthly_wither_grandmaster", ObjectiveType.KILL_ENTITY, "minecraft:wither", 3, "minecraft:nether_star", 2, 2, 6000),
      new QuestTemplate("monthly_ancient_debris_grandmaster", QuestCategory.MONTHLY, null, "quest.questlog.monthly_ancient_debris_grandmaster", ObjectiveType.MINE_BLOCK, "minecraft:ancient_debris", 64, "minecraft:netherite_ingot", 6, 6, 5000),
      new QuestTemplate("monthly_underground_grandmaster", QuestCategory.MONTHLY, null, "quest.questlog.monthly_underground_grandmaster", ObjectiveType.MINE_BLOCK, QuestManager.UNDERGROUND_ORES_AND_STONE, 6000, "minecraft:netherite_ingot", 4, 4, 4000),
      new QuestTemplate("monthly_globe_grandmaster", QuestCategory.MONTHLY, null, "quest.questlog.monthly_globe_grandmaster", ObjectiveType.TRAVEL_DISTANCE, "any", 100000, "minecraft:diamond_block", 5, 5, 3000),
      new QuestTemplate("monthly_collector_grandmaster", QuestCategory.MONTHLY, null, "quest.questlog.monthly_collector_grandmaster", ObjectiveType.COLLECT_DISTINCT_ITEMS, "any", 80, "minecraft:diamond_block", 4, 4, 2800),
      new QuestTemplate("monthly_placement_grandmaster", QuestCategory.MONTHLY, null, "quest.questlog.monthly_placement_grandmaster", ObjectiveType.PLACE_BLOCK, "any", 15000, "minecraft:diamond_block", 6, 6, 3000),
      new QuestTemplate("monthly_crafting_grandmaster", QuestCategory.MONTHLY, null, "quest.questlog.monthly_crafting_grandmaster", ObjectiveType.CRAFT_ITEM, "any", 2000, "minecraft:diamond_block", 5, 5, 3000),
      new QuestTemplate("monthly_smelting_grandmaster", QuestCategory.MONTHLY, null, "quest.questlog.monthly_smelting_grandmaster", ObjectiveType.SMELT_ITEM, "any", 1000, "minecraft:diamond_block", 4, 4, 2800),
      new QuestTemplate("monthly_enderman_purge", QuestCategory.MONTHLY, null, "quest.questlog.monthly_enderman_purge", ObjectiveType.KILL_ENTITY, "minecraft:enderman", 200, "minecraft:diamond_block", 4, 4, 3000)
   );

   /** One entry per rarity — QuestManager keeps exactly one Mystery slot active per rarity at all times. */
   public static final List<QuestTemplate> MYSTERY = List.of(
      new QuestTemplate("mystery_common", QuestCategory.MYSTERY, QuestRarity.COMMON, "quest.questlog.mystery_common", ObjectiveType.MINE_BLOCK, "#minecraft:base_stone_overworld", 64, null, 0, 0, 0),
      new QuestTemplate("mystery_uncommon", QuestCategory.MYSTERY, QuestRarity.UNCOMMON, "quest.questlog.mystery_uncommon", ObjectiveType.KILL_ENTITY, "hostile", 15, null, 0, 0, 0),
      new QuestTemplate("mystery_rare", QuestCategory.MYSTERY, QuestRarity.RARE, "quest.questlog.mystery_rare", ObjectiveType.MINE_BLOCK, "#minecraft:diamond_ores", 6, null, 0, 0, 0),
      new QuestTemplate("mystery_epic", QuestCategory.MYSTERY, QuestRarity.EPIC, "quest.questlog.mystery_epic", ObjectiveType.PLACE_BLOCK, "any", 500, null, 0, 0, 0),
      new QuestTemplate("mystery_legendary", QuestCategory.MYSTERY, QuestRarity.LEGENDARY, "quest.questlog.mystery_legendary", ObjectiveType.VISIT_BIOME, "any", 30, null, 0, 0, 0),
      new QuestTemplate("mystery_mythic", QuestCategory.MYSTERY, QuestRarity.MYTHIC, "quest.questlog.mystery_mythic", ObjectiveType.MINE_BLOCK, "minecraft:ancient_debris", 8, null, 0, 0, 0)
   );

   public static List<QuestTemplate> pool(QuestCategory category) {
      return switch (category) {
         case DAILY -> DAILY;
         case WEEKLY -> WEEKLY;
         case MONTHLY -> MONTHLY;
         case MYSTERY -> MYSTERY;
      };
   }

   public static QuestTemplate byId(String id) {
      for (QuestCategory category : QuestCategory.values()) {
         for (QuestTemplate template : pool(category)) {
            if (template.id().equals(id)) {
               return template;
            }
         }
      }

      throw new IllegalArgumentException("Unknown quest template: " + id);
   }

   /** As {@link #byId}, but returns {@code null} instead of throwing when the id isn't in the current pool. */
   public static QuestTemplate tryById(String id) {
      for (QuestCategory category : QuestCategory.values()) {
         for (QuestTemplate template : pool(category)) {
            if (template.id().equals(id)) {
               return template;
            }
         }
      }

      return null;
   }

   private QuestTemplates() {
   }
}
