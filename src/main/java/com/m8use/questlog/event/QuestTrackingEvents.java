package com.m8use.questlog.event;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.m8use.questlog.quest.QuestManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEnchantItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.TradeWithVillagerEvent;
import net.neoforged.neoforge.event.brewing.PlayerBrewedPotionEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Progress tracking, kept separate from {@link QuestManager} so the "what event means what
 * progress" wiring is easy to scan in one place. Nothing here decides quest rules — it just
 * reports raw game events to the manager, which does the actual matching.
 */
public final class QuestTrackingEvents {
   private static final int TRAVEL_CHECK_INTERVAL_TICKS = 20;
   private final Map<UUID, BlockPos> lastCheckedPosition = new HashMap<>();

   @SubscribeEvent
   public void onBlockBreak(BlockEvent.BreakEvent event) {
      if (event.getPlayer() instanceof ServerPlayer serverPlayer) {
         QuestManager.onBlockMined(serverPlayer, event.getPos(), event.getState());
      }
   }

   /**
    * Covers both single-block placements and multi-block ones (beds, doors, etc. — {@link
    * BlockEvent.EntityMultiPlaceEvent} extends this event, so it arrives here too); each counted
    * once toward PLACE_BLOCK objectives and marked once in the anti-exploit tracker.
    */
   @SubscribeEvent
   public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
      if (event.getEntity() instanceof ServerPlayer serverPlayer) {
         QuestManager.onBlockPlaced(serverPlayer, event.getPos(), event.getPlacedBlock());
      }
   }

   @SubscribeEvent
   public void onLivingDeath(LivingDeathEvent event) {
      DamageSource source = event.getSource();
      if (source.getEntity() instanceof ServerPlayer serverPlayer) {
         QuestManager.onEntityKilled(serverPlayer, event.getEntity());
      }
   }

   @SubscribeEvent
   public void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
      if (event.getEntity() instanceof ServerPlayer serverPlayer && !event.getCrafting().isEmpty()) {
         QuestManager.onItemCrafted(serverPlayer, event.getCrafting());
      }
   }

   @SubscribeEvent
   public void onItemSmelted(PlayerEvent.ItemSmeltedEvent event) {
      if (event.getEntity() instanceof ServerPlayer serverPlayer && !event.getSmelting().isEmpty()) {
         QuestManager.onItemSmelted(serverPlayer, event.getSmelting());
      }
   }

   @SubscribeEvent
   public void onItemFished(ItemFishedEvent event) {
      if (event.getEntity() instanceof ServerPlayer serverPlayer && !event.getDrops().isEmpty()) {
         QuestManager.onFishCaught(serverPlayer);
      }
   }

   @SubscribeEvent
   public void onAnimalBred(BabyEntitySpawnEvent event) {
      if (event.getCausedByPlayer() instanceof ServerPlayer serverPlayer) {
         QuestManager.onAnimalBred(serverPlayer);
      }
   }

   /** Only real enchanting-table actions reach here — an item that was already enchanted before the player picked it up never fires this. */
   @SubscribeEvent
   public void onItemEnchanted(PlayerEnchantItemEvent event) {
      if (event.getEntity() instanceof ServerPlayer serverPlayer) {
         QuestManager.onItemEnchanted(serverPlayer);
      }
   }

   @SubscribeEvent
   public void onItemPickup(ItemEntityPickupEvent.Post event) {
      if (event.getPlayer() instanceof ServerPlayer serverPlayer) {
         QuestManager.onItemCollected(serverPlayer, event.getOriginalStack());
      }
   }

   @SubscribeEvent
   public void onPotionBrewed(PlayerBrewedPotionEvent event) {
      if (event.getEntity() instanceof ServerPlayer serverPlayer) {
         QuestManager.onPotionBrewed(serverPlayer);
      }
   }

   @SubscribeEvent
   public void onVillagerTraded(TradeWithVillagerEvent event) {
      if (event.getEntity() instanceof ServerPlayer serverPlayer) {
         QuestManager.onVillagerTraded(serverPlayer);
      }
   }

   @SubscribeEvent
   public void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
      if (event.getEntity() instanceof ServerPlayer serverPlayer) {
         QuestManager.onDimensionVisited(serverPlayer, event.getTo().location().toString());
      }
   }

   /** Throttled travel-distance and biome-visit tracking; both need to sample position, not react to a single event. */
   @SubscribeEvent
   public void onPlayerTick(PlayerTickEvent.Post event) {
      Player player = event.getEntity();
      if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer && serverPlayer.tickCount % TRAVEL_CHECK_INTERVAL_TICKS == 0) {
         BlockPos current = serverPlayer.blockPosition();
         BlockPos last = this.lastCheckedPosition.get(serverPlayer.getUUID());
         if (last != null) {
            double dx = current.getX() - last.getX();
            double dz = current.getZ() - last.getZ();
            int distance = (int)Math.sqrt(dx * dx + dz * dz);
            if (distance > 0) {
               QuestManager.onTravelled(serverPlayer, distance);
            }
         }

         this.lastCheckedPosition.put(serverPlayer.getUUID(), current);
         Holder<Biome> biome = serverPlayer.level().getBiome(current);
         biome.unwrapKey().ifPresent(key -> QuestManager.onBiomeVisited(serverPlayer, key.location().toString()));
      }
   }

   /** Delivers any Mystery Box rewards whose roulette reveal animation has finished — see {@link QuestManager#processPendingGrants}. */
   @SubscribeEvent
   public void onServerTick(ServerTickEvent.Post event) {
      QuestManager.processPendingGrants(event.getServer());
   }

   @SubscribeEvent
   public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
      this.lastCheckedPosition.remove(event.getEntity().getUUID());
   }
}
