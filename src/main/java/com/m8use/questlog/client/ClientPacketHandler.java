package com.m8use.questlog.client;

import com.m8use.questlog.network.ClientboundBoxUpgradedPacket;
import com.m8use.questlog.network.ClientboundMysteryBoxBulkRevealPacket;
import com.m8use.questlog.network.ClientboundMysteryBoxEarnedPacket;
import com.m8use.questlog.network.ClientboundMysteryBoxRevealPacket;
import com.m8use.questlog.network.ClientboundQuestClaimResultPacket;
import com.m8use.questlog.network.ClientboundQuestCompletedPacket;
import com.m8use.questlog.network.ClientboundQuestSyncPacket;
import com.m8use.questlog.quest.QuestRarity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

public final class ClientPacketHandler {
   public static void handleSync(ClientboundQuestSyncPacket packet) {
      QuestClientState.apply(
         packet.daily(),
         packet.weekly(),
         packet.monthly(),
         packet.mystery(),
         packet.dailyResetEpochMillis(),
         packet.weeklyResetEpochMillis(),
         packet.monthlyResetEpochMillis(),
         packet.mysteryBoxCounts()
      );
   }

   public static void handleClaimResult(ClientboundQuestClaimResultPacket packet) {
      if (Minecraft.getInstance().screen instanceof QuestScreen screen) {
         screen.onClaimResult(packet);
      }
   }

   public static void handleMysteryReveal(ClientboundMysteryBoxRevealPacket packet) {
      Minecraft minecraft = Minecraft.getInstance();
      if (!(minecraft.screen instanceof MysteryBoxRevealScreen) && !(minecraft.screen instanceof MysteryBoxBulkRevealScreen)) {
         minecraft.setScreen(new MysteryBoxRevealScreen(packet));
      }
   }

   public static void handleMysteryBulkReveal(ClientboundMysteryBoxBulkRevealPacket packet) {
      Minecraft minecraft = Minecraft.getInstance();
      if (!(minecraft.screen instanceof MysteryBoxRevealScreen) && !(minecraft.screen instanceof MysteryBoxBulkRevealScreen)) {
         minecraft.setScreen(new MysteryBoxBulkRevealScreen(packet));
      }
   }

   /** Offers the Save/Open-Now choice on top of whatever screen is currently open (normally the QuestScreen). The box is already stored either way. */
   public static void handleMysteryEarned(ClientboundMysteryBoxEarnedPacket packet) {
      Minecraft minecraft = Minecraft.getInstance();
      Screen current = minecraft.screen;
      QuestRarity rarity = parseRarity(packet.rarityId());
      if (current != null && rarity != null && !(current instanceof MysteryBoxClaimChoiceScreen)) {
         minecraft.setScreen(new MysteryBoxClaimChoiceScreen(current, rarity));
      }
   }

   private static QuestRarity parseRarity(String name) {
      try {
         return QuestRarity.valueOf(name);
      } catch (IllegalArgumentException exception) {
         return null;
      }
   }

   /**
    * Fired the instant a quest crosses its completion threshold — independent of, and usually
    * before, the player actually clicking Claim. Plays regardless of what screen (if any) is open,
    * like a vanilla advancement toast, since the moment of completion can happen during ordinary
    * play (mining, fishing, etc.) with the QuestLog closed.
    */
   public static void handleQuestCompleted(ClientboundQuestCompletedPacket packet) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.player != null) {
         minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F));
      }
   }

   /** Fired only when an upgrade actually succeeded server-side — triggers the Inventory screen's sound/confetti feedback if it's open. */
   public static void handleBoxUpgraded(ClientboundBoxUpgradedPacket packet) {
      QuestRarity toRarity = parseRarity(packet.toRarityId());
      if (toRarity != null && Minecraft.getInstance().screen instanceof MysteryBoxInventoryScreen screen) {
         screen.onUpgraded(toRarity);
      }
   }

   private ClientPacketHandler() {
   }
}
