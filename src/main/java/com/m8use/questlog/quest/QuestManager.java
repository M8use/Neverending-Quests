package com.m8use.questlog.quest;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import com.m8use.questlog.Ids;
import com.m8use.questlog.network.ClientboundBoxUpgradedPacket;
import com.m8use.questlog.network.ClientboundMysteryBoxBulkRevealPacket;
import com.m8use.questlog.network.ClientboundMysteryBoxEarnedPacket;
import com.m8use.questlog.network.ClientboundMysteryBoxRevealPacket;
import com.m8use.questlog.network.ClientboundQuestClaimResultPacket;
import com.m8use.questlog.network.ClientboundQuestCompletedPacket;
import com.m8use.questlog.network.ClientboundQuestSyncPacket;
import com.m8use.questlog.network.QuestNetwork;
import com.m8use.questlog.network.QuestSlotDto;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The server-authoritative brain of the mod. Everything that decides "is this quest assigned,
 * is it done, did it get claimed" lives here — the client only ever sees the result via
 * {@link com.m8use.questlog.network.ClientboundQuestSyncPacket} and never tells the server
 * "I finished this", only "please check if I finished this".
 */
public final class QuestManager {
   private static final ZoneId ZONE = ZoneId.systemDefault();
   /** Blocks at or above this Y level don't count as "underground" for objectives like Master Miner. */
   private static final int UNDERGROUND_MAX_Y = 60;
   /** Special {@link ObjectiveType#MINE_BLOCK} matchId meaning "an ore or stone-type block, and it must be underground". */
   public static final String UNDERGROUND_ORES_AND_STONE = "underground_ores_and_stone";
   /** How many boxes can be opened from the Mystery Box Inventory in a single request, regardless of what the client asks for. */
   private static final int MAX_BULK_OPEN = 64;
   /**
    * Special {@code rewardItemId} markers recognized by {@link #buildRewardStack}: a potion or an
    * enchanted tool can't be expressed as a plain item id, since the effect/enchantment lives in a
    * data component rather than being a distinct registry entry.
    */
   public static final String SWIFTNESS_II_REWARD_ID = "questlog:swiftness_ii_potion";
   public static final String DENSITY_MACE_REWARD_ID = "questlog:density_mace";
   /** A random log type each time it's granted — see {@link #RANDOM_LOG_TYPES} and the Common Builder quest. */
   public static final String RANDOM_LOG_REWARD_ID = "questlog:random_log";

   /** The log types the Common Builder quest's reward can roll — matches every vanilla wood type as of 1.21.1. */
   public static final List<Item> RANDOM_LOG_TYPES = List.of(
      Items.OAK_LOG,
      Items.SPRUCE_LOG,
      Items.BIRCH_LOG,
      Items.JUNGLE_LOG,
      Items.ACACIA_LOG,
      Items.DARK_OAK_LOG,
      Items.MANGROVE_LOG,
      Items.CHERRY_LOG
   );

   /** A random ordinary block each time it's granted — see {@link #randomBlockPool()} and the Uncommon Builder quest. */
   public static final String RANDOM_BLOCK_REWARD_ID = "questlog:random_block";
   /** A fixed Lure III + Unbreaking III enchanted book — see the Legendary Angler quest. */
   public static final String LURE_UNBREAKING_BOOK_REWARD_ID = "questlog:lure_unbreaking_book";

   /**
    * Blocks that exist as real items but aren't something a survival player would ever legitimately
    * end up holding — creative/technical placement tools, not obtainable through mining, crafting,
    * or trading. Excluded from {@link #randomBlockPool()} on top of the explicit reward-tier blocks
    * (dragon egg, and the six metal/diamond/emerald storage blocks) that quest itself excludes.
    */
   private static final Set<Block> TECHNICAL_BLOCK_DENYLIST = Set.of(
      Blocks.BARRIER,
      Blocks.STRUCTURE_VOID,
      Blocks.STRUCTURE_BLOCK,
      Blocks.JIGSAW,
      Blocks.COMMAND_BLOCK,
      Blocks.CHAIN_COMMAND_BLOCK,
      Blocks.REPEATING_COMMAND_BLOCK,
      Blocks.LIGHT,
      Blocks.SPAWNER,
      Blocks.TRIAL_SPAWNER,
      Blocks.VAULT,
      Blocks.PETRIFIED_OAK_SLAB
   );

   private static List<Item> randomBlockPool;

   /**
    * Every block in the game that's realistically obtainable in survival, computed once and cached.
    * A block is included unless: it has no item form at all (catches fluids, fire, portals, piston
    * heads, and other purely-technical placements automatically), it's on the small hand-picked
    * {@link #TECHNICAL_BLOCK_DENYLIST} of creative-only blocks that *do* have an item form, or it's
    * one of the Uncommon Builder quest's own explicit exclusions (dragon egg, and the six metal/
    * gem storage blocks, which are reward-tier items in their own right elsewhere in this mod).
    */
   public static List<Item> randomBlockPool() {
      if (randomBlockPool == null) {
         List<Item> pool = new ArrayList<>();

         for (Block block : BuiltInRegistries.BLOCK) {
            Item item = block.asItem();
            if (item != Items.AIR
               && !TECHNICAL_BLOCK_DENYLIST.contains(block)
               && block != Blocks.DRAGON_EGG
               && block != Blocks.DIAMOND_BLOCK
               && block != Blocks.EMERALD_BLOCK
               && block != Blocks.GOLD_BLOCK
               && block != Blocks.IRON_BLOCK
               && block != Blocks.NETHERITE_BLOCK) {
               pool.add(item);
            }
         }

         randomBlockPool = List.copyOf(pool);
      }

      return randomBlockPool;
   }

   /** Rarity distribution for newly rolled Daily Quests. Weights are plain percentages — they sum to exactly 100. */
   public static final SimpleWeightedRandomList<QuestRarity> DAILY_RARITY_WEIGHTS = SimpleWeightedRandomList.<QuestRarity>builder()
      .add(QuestRarity.COMMON, 39)
      .add(QuestRarity.UNCOMMON, 25)
      .add(QuestRarity.RARE, 20)
      .add(QuestRarity.EPIC, 10)
      .add(QuestRarity.LEGENDARY, 5)
      .add(QuestRarity.MYTHIC, 1)
      .build();

   // --- Public entry points used by login/tick/network handlers -----------------------------------------------

   public static void onPlayerLogin(ServerPlayer player) {
      sync(player);
   }

   /** Re-checks resets, backfills any empty slots, then pushes the current state to the client. */
   public static void sync(ServerPlayer player) {
      PlayerQuestData data = player.getData(QuestAttachments.DATA);
      checkResets(player, data);
      QuestNetwork.toClient(player, buildSyncPacket(data));
   }

   public static void claim(ServerPlayer player, QuestCategory category, int slotIndex) {
      PlayerQuestData data = player.getData(QuestAttachments.DATA);
      checkResets(player, data);
      List<QuestInstance> slots = data.questsFor(category);
      if (slotIndex >= 0 && slotIndex < slots.size()) {
         QuestInstance instance = slots.get(slotIndex);
         QuestTemplate template = instance.templateOrNull();
         if (!instance.claimed && template != null && instance.isComplete()) {
            instance.claimed = true;
            if (template.isMystery()) {
               QuestRarity rarity = template.rarity();
               slots.set(slotIndex, freshInstance(template));
               // The box goes straight into the permanent collection — never forced open, never a
               // physical item the player has to carry. "Open Now" (if they choose it) is just a
               // convenience that immediately spends the box that's already safely stored below.
               data.mysteryBoxes.add(rarity, 1);
               QuestNetwork.toClient(player, new ClientboundMysteryBoxEarnedPacket(rarity.name()));
            } else {
               ItemStack reward = grantPlainReward(player, template);
               if (template.rewardXp() > 0) {
                  player.giveExperiencePoints(template.rewardXp());
               }

               QuestNetwork.toClient(
                  player, new ClientboundQuestClaimResultPacket(template.id(), Ids.of(reward.getItem()), reward.getCount(), template.rewardXp())
               );
               // Keep exactly {activeSlotCount} slots active at all times: the claimed quest is
               // immediately replaced with a freshly generated one, using the same generation
               // logic (rarity weights, pool, no-repeat rules) as a normal category reset.
               slots.set(slotIndex, generateReplacement(player.getRandom(), data, category, slots, slotIndex));
            }
         }
      }

      sync(player);
   }

   /** Opens a single physically-held Mystery Box item (see {@link com.m8use.questlog.item.MysteryBoxItem}). */
   public static void openMysteryBox(ServerPlayer player, QuestRarity rarity) {
      MysteryBoxLoot.RollResult result = MysteryBoxLoot.roll(rarity, player.getRandom(), player.level().registryAccess());
      scheduleGrant(player, result.stack().copy());
      QuestNetwork.toClient(
         player,
         new ClientboundMysteryBoxRevealPacket(rarity.name(), Ids.of(result.stack().getItem()), result.stack().getCount(), result.itemRarity().name())
      );
   }

   /**
    * Opens {@code count} boxes of {@code rarity} from the player's persistent QuestLog collection.
    * Server-authoritative end to end: the requested count is clamped, ownership is checked with
    * {@link MysteryBoxInventory#tryRemove}, and the count is only ever decremented once — a player
    * can never open more boxes than they actually own, and a rejected request changes nothing.
    */
   public static void openStoredBoxes(ServerPlayer player, QuestRarity rarity, int count) {
      PlayerQuestData data = player.getData(QuestAttachments.DATA);
      int clamped = Math.min(count, MAX_BULK_OPEN);
      if (clamped > 0 && data.mysteryBoxes.tryRemove(rarity, clamped)) {
         RandomSource random = player.getRandom();
         RegistryAccess registryAccess = player.level().registryAccess();
         List<String> itemIds = new ArrayList<>(clamped);
         List<Integer> amounts = new ArrayList<>(clamped);
         List<String> itemRarityIds = new ArrayList<>(clamped);
         List<ItemStack> rolledStacks = new ArrayList<>(clamped);

         for (int i = 0; i < clamped; i++) {
            MysteryBoxLoot.RollResult result = MysteryBoxLoot.roll(rarity, random, registryAccess);
            rolledStacks.add(result.stack());
            itemIds.add(Ids.of(result.stack().getItem()));
            amounts.add(result.stack().getCount());
            itemRarityIds.add(result.itemRarity().name());
         }

         if (clamped == 1) {
            // The single-box roulette reveal takes a few seconds to "land" — delay the actual grant
            // to match, so the item can't be spoiled by simply glancing at the inventory/hotbar.
            scheduleGrant(player, rolledStacks.get(0).copy());
            QuestNetwork.toClient(player, new ClientboundMysteryBoxRevealPacket(rarity.name(), itemIds.get(0), amounts.get(0), itemRarityIds.get(0)));
         } else {
            for (ItemStack stack : rolledStacks) {
               grant(player, stack.copy());
            }

            QuestNetwork.toClient(player, new ClientboundMysteryBoxBulkRevealPacket(rarity.name(), itemIds, amounts, itemRarityIds));
         }
      }

      sync(player);
   }

   /** Converts {@link QuestRarity#UPGRADE_COST} boxes of {@code rarity} into a single box of the next rarity up. */
   public static void upgradeBox(ServerPlayer player, QuestRarity rarity) {
      PlayerQuestData data = player.getData(QuestAttachments.DATA);
      QuestRarity next = rarity.next();
      if (next != null && data.mysteryBoxes.tryRemove(rarity, QuestRarity.UPGRADE_COST)) {
         data.mysteryBoxes.add(next, 1);
         QuestNetwork.toClient(player, new ClientboundBoxUpgradedPacket(next.name()));
      }

      sync(player);
   }

   // --- Delayed reward granting -------------------------------------------------------------------------------

   /**
    * How long the client's single-box roulette reveal takes before the item visually "lands" (see
    * {@code MysteryBoxRevealScreen}'s ANTICIPATION_MS + SPIN_MS + FALL_MS). The actual item isn't
    * added to the player's inventory until this much time has passed — otherwise the reward shows
    * up in their hotbar/inventory the instant they click Open, and simply glancing at it spoils the
    * multi-second animation regardless of how suspenseful it looks.
    */
   private static final long REVEAL_ANIMATION_MS = 3500L;
   /** If a player disconnects mid-animation, keep the reward waiting for them instead of losing it — but not forever. */
   private static final long PENDING_GRANT_EXPIRY_MS = 300000L;
   private static final Deque<PendingGrant> pendingGrants = new ArrayDeque<>();

   private record PendingGrant(UUID playerId, ItemStack stack, long readyAtMillis, long expireAtMillis) {
   }

   private static void scheduleGrant(ServerPlayer player, ItemStack stack) {
      long now = System.currentTimeMillis();
      pendingGrants.add(new PendingGrant(player.getUUID(), stack, now + REVEAL_ANIMATION_MS, now + REVEAL_ANIMATION_MS + PENDING_GRANT_EXPIRY_MS));
   }

   /** Called once per server tick (see {@code QuestTrackingEvents#onServerTick}) to deliver any rewards whose reveal animation has finished. */
   public static void processPendingGrants(MinecraftServer server) {
      if (!pendingGrants.isEmpty()) {
         long now = System.currentTimeMillis();
         pendingGrants.removeIf(pending -> {
            if (now < pending.readyAtMillis()) {
               return false;
            } else {
               ServerPlayer player = server.getPlayerList().getPlayer(pending.playerId());
               if (player != null) {
                  grant(player, pending.stack());
                  return true;
               } else {
                  return now >= pending.expireAtMillis();
               }
            }
         });
      }
   }

   // --- Progress tracking, called from QuestTrackingEvents -----------------------------------------------------

   /**
    * Handles one broken block. This is intentionally called once per block, not once per player
    * action — a Veinminer-style mod that breaks 12 connected blocks in a single swing is correctly
    * counted as 12 units of progress here, <em>provided</em> that mod fires a normal {@code
    * BlockEvent.BreakEvent} for each block it removes (the standard, compatible way to implement
    * multi-block mining on NeoForge, since that's the same signal drop-multiplier mods, XP mods,
    * and stats all rely on). A Veinminer implementation that bypasses the event system for its
    * secondary blocks — legal but nonstandard — wouldn't be visible here or to any other mod that
    * depends on this event, and there's no separate "block removed for any reason" event to fall
    * back on.
    */
   public static void onBlockMined(ServerPlayer player, BlockPos pos, BlockState state) {
      Level level = player.level();
      boolean wasPlayerPlaced = PlacedBlockTracker.isPlayerPlaced(level, pos);
      // Bound the tracker to blocks that are still standing, regardless of the outcome below.
      PlacedBlockTracker.clearPlaced(level, pos);
      if (!wasPlayerPlaced) {
         forEachActive(player, instance -> {
            QuestTemplate template = instance.template();
            if (template.objective() == ObjectiveType.MINE_BLOCK && matchesBlock(template.matchId(), pos, state)) {
               increment(instance, 1);
            }
         });
      }
   }

   public static void onBlockPlaced(ServerPlayer player, BlockPos pos, BlockState state) {
      PlacedBlockTracker.markPlaced(player.level(), pos);
      forEachActive(player, instance -> {
         QuestTemplate template = instance.template();
         if (template.objective() == ObjectiveType.PLACE_BLOCK && matchesPlacedBlock(template.matchId(), state)) {
            increment(instance, 1);
         }
      });
   }

   public static void onEntityKilled(ServerPlayer player, Entity killed) {
      forEachActive(player, instance -> {
         QuestTemplate template = instance.template();
         if (template.objective() == ObjectiveType.KILL_ENTITY && matchesEntity(template.matchId(), killed)) {
            increment(instance, 1);
         }
      });
   }

   public static void onItemCrafted(ServerPlayer player, ItemStack crafted) {
      forEachActive(player, instance -> {
         QuestTemplate template = instance.template();
         if (template.objective() == ObjectiveType.CRAFT_ITEM && matchesItem(template.matchId(), crafted)) {
            increment(instance, 1);
         }
      });
   }

   public static void onItemSmelted(ServerPlayer player, ItemStack smelted) {
      forEachActive(player, instance -> {
         QuestTemplate template = instance.template();
         if (template.objective() == ObjectiveType.SMELT_ITEM && matchesItem(template.matchId(), smelted)) {
            increment(instance, 1);
         }
      });
   }

   public static void onFishCaught(ServerPlayer player) {
      forEachActive(player, instance -> {
         if (instance.template().objective() == ObjectiveType.CATCH_FISH) {
            increment(instance, 1);
         }
      });
   }

   public static void onAnimalBred(ServerPlayer player) {
      forEachActive(player, instance -> {
         if (instance.template().objective() == ObjectiveType.BREED_ANIMAL) {
            increment(instance, 1);
         }
      });
   }

   /** Fires once per completed enchanting-table action (not once per enchantment applied, and never for items that were already enchanted). */
   public static void onItemEnchanted(ServerPlayer player) {
      forEachActive(player, instance -> {
         if (instance.template().objective() == ObjectiveType.ENCHANT_ITEM) {
            increment(instance, 1);
         }
      });
   }

   public static void onPotionBrewed(ServerPlayer player) {
      forEachActive(player, instance -> {
         if (instance.template().objective() == ObjectiveType.BREW_POTION) {
            increment(instance, 1);
         }
      });
   }

   public static void onVillagerTraded(ServerPlayer player) {
      forEachActive(player, instance -> {
         if (instance.template().objective() == ObjectiveType.TRADE_VILLAGER) {
            increment(instance, 1);
         }
      });
   }

   public static void onItemCollected(ServerPlayer player, ItemStack stack) {
      if (!stack.isEmpty()) {
         String itemId = Ids.of(stack.getItem());
         forEachActive(player, instance -> {
            QuestTemplate template = instance.template();
            if (template.objective() == ObjectiveType.COLLECT_DISTINCT_ITEMS
               && matchesItem(template.matchId(), stack)
               && !instance.seenKeys.contains(itemId)) {
               instance.seenKeys.add(itemId);
               instance.progress = instance.seenKeys.size();
            }
         });
      }
   }

   public static void onTravelled(ServerPlayer player, int blocks) {
      forEachActive(player, instance -> {
         if (instance.template().objective() == ObjectiveType.TRAVEL_DISTANCE) {
            increment(instance, blocks);
         }
      });
   }

   /** Only counts a biome the first time it's seen for a given quest cycle — see {@link QuestInstance#seenKeys}. */
   public static void onBiomeVisited(ServerPlayer player, String biomeId) {
      forEachActive(player, instance -> {
         if (instance.template().objective() == ObjectiveType.VISIT_BIOME && !instance.seenKeys.contains(biomeId)) {
            instance.seenKeys.add(biomeId);
            instance.progress = instance.seenKeys.size();
         }
      });
   }

   public static void onDimensionVisited(ServerPlayer player, String dimensionId) {
      forEachActive(player, instance -> {
         QuestTemplate template = instance.template();
         if (template.objective() == ObjectiveType.VISIT_DIMENSION && template.matchId().equals(dimensionId)) {
            increment(instance, 1);
         }
      });
   }

   private static void increment(QuestInstance instance, int amount) {
      if (!instance.claimed) {
         instance.progress = Math.min(instance.template().amount(), instance.progress + amount);
      }
   }

   private static void forEachActive(ServerPlayer player, Consumer<QuestInstance> action) {
      PlayerQuestData data = player.getData(QuestAttachments.DATA);
      boolean changed = false;

      for (QuestCategory category : QuestCategory.values()) {
         for (QuestInstance instance : data.questsFor(category)) {
            if (!instance.claimed && instance.templateOrNull() != null) {
               int before = instance.progress;
               boolean wasComplete = instance.isComplete();
               action.accept(instance);
               if (instance.progress != before) {
                  changed = true;
                  if (!wasComplete && instance.isComplete()) {
                     QuestNetwork.toClient(player, new ClientboundQuestCompletedPacket(instance.templateId));
                  }
               }
            }
         }
      }

      if (changed) {
         sync(player);
      }
   }

   // --- Matching -------------------------------------------------------------------------------------------

   private static boolean matchesBlock(String matchId, BlockPos pos, BlockState state) {
      if (matchId.equals(UNDERGROUND_ORES_AND_STONE)) {
         return pos.getY() < UNDERGROUND_MAX_Y && (state.is(oresTag()) || state.is(baseStoneTag()));
      } else if (matchId.equals("crops")) {
         return state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state);
      } else if (matchId.startsWith("#")) {
         TagKey<Block> tag = TagKey.create(Registries.BLOCK, ResourceLocation.parse(matchId.substring(1)));
         return state.is(tag);
      } else {
         Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(matchId));
         return state.is(block);
      }
   }

   private static boolean matchesPlacedBlock(String matchId, BlockState state) {
      if (matchId.equals("any")) {
         return true;
      } else if (matchId.startsWith("#")) {
         TagKey<Block> tag = TagKey.create(Registries.BLOCK, ResourceLocation.parse(matchId.substring(1)));
         return state.is(tag);
      } else {
         Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(matchId));
         return state.is(block);
      }
   }

   private static TagKey<Block> oresTag() {
      return TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("ores"));
   }

   private static TagKey<Block> baseStoneTag() {
      return TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("base_stone_overworld"));
   }

   private static boolean matchesEntity(String matchId, Entity entity) {
      if (matchId.equals("hostile")) {
         return entity instanceof Enemy;
      } else if (matchId.startsWith("#")) {
         TagKey<EntityType<?>> tag = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.parse(matchId.substring(1)));
         return entity.getType().is(tag);
      } else {
         EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(matchId));
         return entity.getType() == type;
      }
   }

   private static boolean matchesItem(String matchId, ItemStack stack) {
      if (matchId.equals("any")) {
         return !stack.isEmpty();
      } else if (matchId.startsWith("#")) {
         TagKey<Item> tag = TagKey.create(Registries.ITEM, ResourceLocation.parse(matchId.substring(1)));
         return stack.is(tag);
      } else {
         Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(matchId));
         return stack.is(item);
      }
   }

   // --- Resets and assignment --------------------------------------------------------------------------------

   private static void checkResets(ServerPlayer player, PlayerQuestData data) {
      LocalDate today = LocalDate.now(ZONE);
      long epochDay = today.toEpochDay();
      long epochWeek = Math.floorDiv(epochDay, 7L);
      long epochMonth = (long)today.getYear() * 12L + today.getMonthValue();
      RandomSource random = player.getRandom();
      if (data.lastDailyResetEpochDay != epochDay) {
         data.dailyQuests = assignDaily(random, QuestCategory.DAILY.activeSlotCount());
         data.lastDailyResetEpochDay = epochDay;
      }

      if (data.lastWeeklyResetEpochWeek != epochWeek) {
         data.weeklyQuests = assignRandom(random, QuestTemplates.WEEKLY, QuestCategory.WEEKLY.activeSlotCount());
         data.lastWeeklyResetEpochWeek = epochWeek;
      }

      if (data.lastMonthlyResetEpochMonth != epochMonth) {
         data.monthlyQuests = assignMonthly(random, data, QuestCategory.MONTHLY.activeSlotCount());
         data.lastMonthlyResetEpochMonth = epochMonth;
      }

      if (data.mysteryQuests.size() != QuestTemplates.MYSTERY.size()) {
         List<QuestInstance> fixed = new ArrayList<>();

         for (QuestTemplate template : QuestTemplates.MYSTERY) {
            QuestInstance existing = findByTemplateId(data.mysteryQuests, template.id());
            fixed.add(existing != null ? existing : freshInstance(template));
         }

         data.mysteryQuests = fixed;
      }
   }

   private static QuestInstance findByTemplateId(List<QuestInstance> instances, String templateId) {
      for (QuestInstance instance : instances) {
         if (instance.templateId.equals(templateId)) {
            return instance;
         }
      }

      return null;
   }

   private static QuestInstance freshInstance(QuestTemplate template) {
      return new QuestInstance(template.id(), 0, false, List.of());
   }

   /**
    * Generates a single replacement quest for one slot, immediately after that slot's quest was
    * claimed — this is what keeps Daily at exactly 6 active slots (etc.) instead of leaving a
    * claimed quest sitting there forever. Reuses the exact same selection rules as a full category
    * reset (rarity weights for Daily, no-repeat history for Monthly), and additionally avoids
    * duplicating whatever templates are still active in this category's *other* slots — not a
    * strict guarantee forever, just a practical best-effort against obvious immediate repeats.
    */
   private static QuestInstance generateReplacement(RandomSource random, PlayerQuestData data, QuestCategory category, List<QuestInstance> slots, int slotIndex) {
      Set<String> usedIds = new HashSet<>();
      Set<ObjectiveType> usedTypes = new HashSet<>();

      for (int i = 0; i < slots.size(); i++) {
         if (i != slotIndex) {
            QuestTemplate other = slots.get(i).templateOrNull();
            if (other != null) {
               usedIds.add(other.id());
               usedTypes.add(other.objective());
            }
         }
      }

      QuestTemplate pick = switch (category) {
         case DAILY -> pickDailyReplacement(random, usedIds, usedTypes);
         case WEEKLY -> pickUnusedFromPool(random, QuestTemplates.WEEKLY, usedIds);
         case MONTHLY -> pickMonthlyReplacement(random, data, usedIds);
         case MYSTERY -> throw new IllegalStateException("Mystery quests are replaced by claim() directly, not through generateReplacement");
      };
      return freshInstance(pick);
   }

   private static QuestTemplate pickDailyReplacement(RandomSource random, Set<String> usedIds, Set<ObjectiveType> usedTypes) {
      QuestRarity rarity = DAILY_RARITY_WEIGHTS.getRandomValue(random).orElse(QuestRarity.COMMON);
      QuestTemplate pick = pickUnusedDailyTemplate(random, rarity, usedIds, usedTypes);
      if (pick == null) {
         pick = pickUnusedDailyTemplate(random, null, usedIds, usedTypes);
      }

      return pick != null ? pick : QuestTemplates.DAILY.get(random.nextInt(QuestTemplates.DAILY.size()));
   }

   private static QuestTemplate pickMonthlyReplacement(RandomSource random, PlayerQuestData data, Set<String> usedIds) {
      Set<String> history = new HashSet<>(data.monthlyQuestHistory);
      QuestTemplate pick = pickUnusedFromPoolOrNull(random, QuestTemplates.MONTHLY, id -> !usedIds.contains(id) && !history.contains(id));
      if (pick == null) {
         // Pool exhausted against history + current slots — relax to just avoiding current duplicates.
         pick = pickUnusedFromPool(random, QuestTemplates.MONTHLY, usedIds);
      }

      data.monthlyQuestHistory.add(pick.id());
      return pick;
   }

   /** Picks a random template not in {@code usedIds}, falling back to the whole pool if every entry is currently in use. */
   private static QuestTemplate pickUnusedFromPool(RandomSource random, List<QuestTemplate> pool, Set<String> usedIds) {
      QuestTemplate pick = pickUnusedFromPoolOrNull(random, pool, id -> !usedIds.contains(id));
      return pick != null ? pick : pool.get(random.nextInt(pool.size()));
   }

   private static QuestTemplate pickUnusedFromPoolOrNull(RandomSource random, List<QuestTemplate> pool, Predicate<String> allowed) {
      List<QuestTemplate> candidates = new ArrayList<>();

      for (QuestTemplate template : pool) {
         if (allowed.test(template.id())) {
            candidates.add(template);
         }
      }

      return candidates.isEmpty() ? null : candidates.get(random.nextInt(candidates.size()));
   }

   /**
    * Rolls {@code count} Daily Quests: each slot independently rolls a rarity from
    * {@link #DAILY_RARITY_WEIGHTS}, then picks a not-yet-chosen template of that rarity (preferring
    * one with an objective type not already represented in this reset, so three slots don't all
    * feel the same). Falls back to any remaining Daily template if a rolled rarity's pool is
    * exhausted, so a small pool can never cause a slot to come up empty.
    */
   private static List<QuestInstance> assignDaily(RandomSource random, int count) {
      List<QuestTemplate> chosen = new ArrayList<>();
      Set<String> usedIds = new HashSet<>();
      Set<ObjectiveType> usedTypes = new HashSet<>();

      for (int i = 0; i < count; i++) {
         QuestRarity rarity = DAILY_RARITY_WEIGHTS.getRandomValue(random).orElse(QuestRarity.COMMON);
         QuestTemplate pick = pickUnusedDailyTemplate(random, rarity, usedIds, usedTypes);
         if (pick == null) {
            pick = pickUnusedDailyTemplate(random, null, usedIds, usedTypes);
         }

         if (pick != null) {
            chosen.add(pick);
            usedIds.add(pick.id());
            usedTypes.add(pick.objective());
         }
      }

      List<QuestInstance> result = new ArrayList<>();

      for (QuestTemplate template : chosen) {
         result.add(freshInstance(template));
      }

      return result;
   }

   private static QuestTemplate pickUnusedDailyTemplate(RandomSource random, QuestRarity rarity, Set<String> usedIds, Set<ObjectiveType> usedTypes) {
      List<QuestTemplate> candidates = new ArrayList<>();
      List<QuestTemplate> freshObjective = new ArrayList<>();

      for (QuestTemplate template : QuestTemplates.DAILY) {
         if (!usedIds.contains(template.id()) && (rarity == null || template.rarity() == rarity)) {
            candidates.add(template);
            if (!usedTypes.contains(template.objective())) {
               freshObjective.add(template);
            }
         }
      }

      List<QuestTemplate> pool = freshObjective.isEmpty() ? candidates : freshObjective;
      return pool.isEmpty() ? null : pool.get(random.nextInt(pool.size()));
   }

   /**
    * Rolls {@code count} Monthly Quests, avoiding anything in {@link PlayerQuestData#monthlyQuestHistory}
    * as long as enough unused quests remain. Once the pool is exhausted, the history is cleared and
    * the rotation starts a fresh cycle rather than being forced to repeat immediately.
    */
   private static List<QuestInstance> assignMonthly(RandomSource random, PlayerQuestData data, int count) {
      List<QuestTemplate> pool = QuestTemplates.MONTHLY;
      Set<String> history = new HashSet<>(data.monthlyQuestHistory);
      List<QuestTemplate> available = new ArrayList<>();

      for (QuestTemplate template : pool) {
         if (!history.contains(template.id())) {
            available.add(template);
         }
      }

      if (available.size() < count) {
         data.monthlyQuestHistory.clear();
         available = new ArrayList<>(pool);
      }

      List<QuestTemplate> shuffledAvailable = shuffled(available, random);
      List<QuestTemplate> chosen = shuffledAvailable.subList(0, Math.min(count, shuffledAvailable.size()));

      for (QuestTemplate template : chosen) {
         data.monthlyQuestHistory.add(template.id());
      }

      List<QuestInstance> result = new ArrayList<>();

      for (QuestTemplate template : chosen) {
         result.add(freshInstance(template));
      }

      return result;
   }

   /** Picks {@code count} templates, preferring distinct objective types so the same slots don't all feel identical. */
   private static List<QuestInstance> assignRandom(RandomSource random, List<QuestTemplate> pool, int count) {
      List<QuestTemplate> shuffled = shuffled(pool, random);
      List<QuestTemplate> chosen = new ArrayList<>();
      Set<ObjectiveType> usedTypes = new HashSet<>();

      for (QuestTemplate template : shuffled) {
         if (chosen.size() >= count) {
            break;
         }

         if (usedTypes.add(template.objective())) {
            chosen.add(template);
         }
      }

      for (QuestTemplate template : shuffled) {
         if (chosen.size() >= count) {
            break;
         }

         if (!chosen.contains(template)) {
            chosen.add(template);
         }
      }

      List<QuestInstance> result = new ArrayList<>();

      for (QuestTemplate template : chosen) {
         result.add(freshInstance(template));
      }

      return result;
   }

   private static <T> List<T> shuffled(List<T> list, RandomSource random) {
      List<T> copy = new ArrayList<>(list);

      for (int i = copy.size() - 1; i > 0; i--) {
         int j = random.nextInt(i + 1);
         T tmp = copy.get(i);
         copy.set(i, copy.get(j));
         copy.set(j, tmp);
      }

      return copy;
   }

   // --- Rewards ----------------------------------------------------------------------------------------------

   private static ItemStack grantPlainReward(ServerPlayer player, QuestTemplate template) {
      int amount = rollAmount(template, player.getRandom());
      ItemStack stack = buildRewardStack(template.rewardItemId(), amount, player.level().registryAccess());
      grant(player, stack.copy());
      return stack;
   }

   private static int rollAmount(QuestTemplate template, RandomSource random) {
      return template.rewardMin() >= template.rewardMax()
         ? template.rewardMin()
         : template.rewardMin() + random.nextInt(template.rewardMax() - template.rewardMin() + 1);
   }

   /**
    * Resolves a quest template's {@code rewardItemId} into an actual {@link ItemStack}. Shared
    * between the server (actually granting the reward) and the client (previewing the quest card
    * icon) so the two can never disagree about what a reward looks like. Most ids are plain item
    * ids; {@link #SWIFTNESS_II_REWARD_ID} and {@link #DENSITY_MACE_REWARD_ID} are special markers
    * for rewards that need a data component (a potion effect, an enchantment) rather than being a
    * distinct registry entry.
    */
   public static ItemStack buildRewardStack(String rewardItemId, int amount, RegistryAccess registryAccess) {
      if (SWIFTNESS_II_REWARD_ID.equals(rewardItemId)) {
         ItemStack stack = new ItemStack(Items.POTION, Math.max(1, amount));
         stack.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.STRONG_SWIFTNESS));
         return stack;
      } else if (DENSITY_MACE_REWARD_ID.equals(rewardItemId)) {
         ItemStack stack = new ItemStack(Items.MACE, 1);
         Holder<Enchantment> density = registryAccess.registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.DENSITY);
         stack.enchant(density, 5);
         return stack;
      } else if (RANDOM_LOG_REWARD_ID.equals(rewardItemId)) {
         Item log = RANDOM_LOG_TYPES.get(RandomSource.create().nextInt(RANDOM_LOG_TYPES.size()));
         return new ItemStack(log, Math.max(1, amount));
      } else if (RANDOM_BLOCK_REWARD_ID.equals(rewardItemId)) {
         List<Item> pool = randomBlockPool();
         Item block = pool.get(RandomSource.create().nextInt(pool.size()));
         return new ItemStack(block, Math.max(1, amount));
      } else if (LURE_UNBREAKING_BOOK_REWARD_ID.equals(rewardItemId)) {
         ItemStack stack = new ItemStack(Items.ENCHANTED_BOOK, 1);
         var enchantmentRegistry = registryAccess.registryOrThrow(Registries.ENCHANTMENT);
         stack.enchant(enchantmentRegistry.getHolderOrThrow(Enchantments.LURE), 3);
         stack.enchant(enchantmentRegistry.getHolderOrThrow(Enchantments.UNBREAKING), 3);
         return stack;
      } else {
         Item item = Ids.item(rewardItemId);
         if (item == Items.ENCHANTED_BOOK) {
            return randomEnchantedBook(registryAccess, Math.max(1, amount));
         } else {
            return item == null ? ItemStack.EMPTY : new ItemStack(item, Math.max(1, amount));
         }
      }
   }

   static ItemStack randomEnchantedBook(RegistryAccess registryAccess, int copies) {
      return randomEnchantedBook(registryAccess, copies, RandomSource.create());
   }

   /**
    * A plain {@code "minecraft:enchanted_book"} reward id would otherwise produce a book with no
    * enchantment at all — {@code new ItemStack(Items.ENCHANTED_BOOK, ...)} never sets the {@code
    * ItemEnchantments} data component on its own, so the book would look right but do nothing.
    * Instead, roll a genuine random enchantment at a random valid level for it, so every enchanted
    * book reward is actually usable. Curse enchantments are excluded — a "reward" that curses the
    * player defeats the point of it being a reward.
    */
   static ItemStack randomEnchantedBook(RegistryAccess registryAccess, int copies, RandomSource random) {
      Registry<Enchantment> registry = registryAccess.registryOrThrow(Registries.ENCHANTMENT);
      List<Holder.Reference<Enchantment>> candidates = registry.holders()
         .filter(holder -> !holder.is(Enchantments.BINDING_CURSE) && !holder.is(Enchantments.VANISHING_CURSE))
         .toList();
      ItemStack stack = new ItemStack(Items.ENCHANTED_BOOK, copies);
      if (!candidates.isEmpty()) {
         Holder.Reference<Enchantment> chosen = candidates.get(random.nextInt(candidates.size()));
         int level = 1 + random.nextInt(chosen.value().getMaxLevel());
         stack.enchant(chosen, level);
      }

      return stack;
   }

   private static void grant(ServerPlayer player, ItemStack stack) {
      if (!stack.isEmpty()) {
         player.getInventory().add(stack);
         if (!stack.isEmpty()) {
            player.drop(stack, false);
         }
      }
   }

   // --- Packet building ----------------------------------------------------------------------------------------

   private static ClientboundQuestSyncPacket buildSyncPacket(PlayerQuestData data) {
      LocalDate today = LocalDate.now(ZONE);
      long dailyReset = today.plusDays(1L).atStartOfDay(ZONE).toInstant().toEpochMilli();
      long currentEpochWeek = Math.floorDiv(today.toEpochDay(), 7L);
      long weeklyReset = LocalDate.ofEpochDay((currentEpochWeek + 1L) * 7L).atStartOfDay(ZONE).toInstant().toEpochMilli();
      long monthlyReset = today.withDayOfMonth(1).plusMonths(1L).atStartOfDay(ZONE).toInstant().toEpochMilli();
      List<Integer> boxCounts = new ArrayList<>();

      for (QuestRarity rarity : QuestRarity.values()) {
         boxCounts.add(data.mysteryBoxes.get(rarity));
      }

      return new ClientboundQuestSyncPacket(
         toDto(data.dailyQuests),
         toDto(data.weeklyQuests),
         toDto(data.monthlyQuests),
         toDto(data.mysteryQuests),
         dailyReset,
         weeklyReset,
         monthlyReset,
         boxCounts
      );
   }

   private static List<QuestSlotDto> toDto(List<QuestInstance> instances) {
      List<QuestSlotDto> result = new ArrayList<>();

      for (QuestInstance instance : instances) {
         result.add(new QuestSlotDto(instance.templateId, instance.progress, instance.claimed));
      }

      return result;
   }

   // --- Cheat/testing commands (see com.m8use.questlog.command.QuestModCommand; gated to permission level 2) ------

   /** Instantly completes (but does not claim) the quest at a slot, for testing. Returns false if the slot doesn't exist. */
   public static boolean cheatCompleteSlot(ServerPlayer player, QuestCategory category, int slotIndex) {
      PlayerQuestData data = player.getData(QuestAttachments.DATA);
      checkResets(player, data);
      List<QuestInstance> slots = data.questsFor(category);
      if (slotIndex >= 0 && slotIndex < slots.size()) {
         QuestInstance instance = slots.get(slotIndex);
         QuestTemplate template = instance.templateOrNull();
         if (template != null && !instance.claimed) {
            instance.progress = template.amount();
            sync(player);
            return true;
         }
      }

      return false;
   }

   /** Instantly completes AND claims the quest at a slot, for testing the full reward flow in one step. */
   public static boolean cheatClaimSlot(ServerPlayer player, QuestCategory category, int slotIndex) {
      PlayerQuestData data = player.getData(QuestAttachments.DATA);
      checkResets(player, data);
      List<QuestInstance> slots = data.questsFor(category);
      if (slotIndex >= 0 && slotIndex < slots.size() && slots.get(slotIndex).templateOrNull() != null) {
         slots.get(slotIndex).progress = slots.get(slotIndex).template().amount();
         claim(player, category, slotIndex);
         return true;
      }

      return false;
   }

   /** Finds the Mystery slot for a rarity and instantly completes it (not claimed), for testing. */
   public static boolean cheatCompleteMystery(ServerPlayer player, QuestRarity rarity) {
      PlayerQuestData data = player.getData(QuestAttachments.DATA);
      checkResets(player, data);

      for (int i = 0; i < data.mysteryQuests.size(); i++) {
         QuestInstance instance = data.mysteryQuests.get(i);
         QuestTemplate template = instance.templateOrNull();
         if (template != null && template.rarity() == rarity) {
            instance.progress = template.amount();
            sync(player);
            return true;
         }
      }

      return false;
   }

   /** Directly adds boxes to the QuestLog collection — never physical items, exactly like every other way of gaining boxes. */
   public static void cheatGiveBoxes(ServerPlayer player, QuestRarity rarity, int count) {
      if (count > 0) {
         PlayerQuestData data = player.getData(QuestAttachments.DATA);
         data.mysteryBoxes.add(rarity, count);
         sync(player);
      }
   }

   private QuestManager() {
   }
}
