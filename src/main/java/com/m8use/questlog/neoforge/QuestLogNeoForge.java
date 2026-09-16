package com.m8use.questlog.neoforge;

import com.m8use.questlog.client.QuestButton;
import com.m8use.questlog.command.QuestModCommand;
import com.m8use.questlog.event.QuestTrackingEvents;
import com.m8use.questlog.quest.QuestAttachments;
import com.m8use.questlog.quest.QuestManager;
import com.m8use.questlog.registry.ModItems;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@Mod("questlog")
public class QuestLogNeoForge {
   public QuestLogNeoForge(IEventBus modBus, ModContainer container) {
      QuestAttachments.register(modBus);
      ModItems.register(modBus);
      modBus.addListener(NeoForgeNetwork::register);
      NeoForge.EVENT_BUS.register(new QuestTrackingEvents());
      NeoForge.EVENT_BUS.addListener(this::onInventoryScreenInit);
      NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);
      NeoForge.EVENT_BUS.addListener(QuestModCommand::register);
   }

   private void onInventoryScreenInit(ScreenEvent.Init.Post event) {
      if (event.getScreen() instanceof InventoryScreen inventory) {
         event.addListener(QuestButton.create(inventory));
      }
   }

   private void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
      if (event.getEntity() instanceof ServerPlayer serverPlayer) {
         QuestManager.onPlayerLogin(serverPlayer);
      }
   }
}
