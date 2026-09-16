package com.m8use.questlog.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * The QuestLog's own dedicated Mystery Box collection: how many boxes of each rarity a player
 * currently owns. This is the storage system described by the QuestLog Inventory screen — boxes
 * kept here never occupy a slot in the player's normal Minecraft inventory.
 *
 * <p>Embedded as a single field in {@link PlayerQuestData} so it persists the same way everything
 * else in this mod does (a NeoForge data attachment on the player entity, saved with player NBT).
 */
public final class MysteryBoxInventory {
   private int common;
   private int uncommon;
   private int rare;
   private int epic;
   private int legendary;
   private int mythic;

   public MysteryBoxInventory() {
   }

   public MysteryBoxInventory(int common, int uncommon, int rare, int epic, int legendary, int mythic) {
      this.common = Math.max(0, common);
      this.uncommon = Math.max(0, uncommon);
      this.rare = Math.max(0, rare);
      this.epic = Math.max(0, epic);
      this.legendary = Math.max(0, legendary);
      this.mythic = Math.max(0, mythic);
   }

   public int get(QuestRarity rarity) {
      return switch (rarity) {
         case COMMON -> this.common;
         case UNCOMMON -> this.uncommon;
         case RARE -> this.rare;
         case EPIC -> this.epic;
         case LEGENDARY -> this.legendary;
         case MYTHIC -> this.mythic;
      };
   }

   private void set(QuestRarity rarity, int value) {
      int clamped = Math.max(0, value);
      switch (rarity) {
         case COMMON -> this.common = clamped;
         case UNCOMMON -> this.uncommon = clamped;
         case RARE -> this.rare = clamped;
         case EPIC -> this.epic = clamped;
         case LEGENDARY -> this.legendary = clamped;
         case MYTHIC -> this.mythic = clamped;
      }
   }

   /** Adds (or, with a negative amount, removes) boxes of a rarity. Never goes below zero. */
   public void add(QuestRarity rarity, int amount) {
      this.set(rarity, this.get(rarity) + amount);
   }

   /**
    * Attempts to remove {@code amount} boxes of {@code rarity}. Returns {@code false} (and leaves
    * the count unchanged) if the player doesn't own enough — callers must check this before
    * granting whatever the removal was supposed to pay for, so a box can never be spent twice.
    */
   public boolean tryRemove(QuestRarity rarity, int amount) {
      if (amount <= 0 || this.get(rarity) < amount) {
         return false;
      } else {
         this.set(rarity, this.get(rarity) - amount);
         return true;
      }
   }

   public static final Codec<MysteryBoxInventory> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            Codec.INT.fieldOf("common").forGetter(d -> d.common),
            Codec.INT.fieldOf("uncommon").forGetter(d -> d.uncommon),
            Codec.INT.fieldOf("rare").forGetter(d -> d.rare),
            Codec.INT.fieldOf("epic").forGetter(d -> d.epic),
            Codec.INT.fieldOf("legendary").forGetter(d -> d.legendary),
            Codec.INT.fieldOf("mythic").forGetter(d -> d.mythic)
         )
         .apply(instance, MysteryBoxInventory::new)
   );
}
