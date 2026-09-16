package com.m8use.questlog.registry;

import com.m8use.questlog.QuestLogMod;
import com.m8use.questlog.item.MysteryBoxItem;
import com.m8use.questlog.quest.QuestRarity;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
   private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(QuestLogMod.MODID);

   public static final DeferredItem<MysteryBoxItem> MYSTERY_BOX_COMMON = box(QuestRarity.COMMON);
   public static final DeferredItem<MysteryBoxItem> MYSTERY_BOX_UNCOMMON = box(QuestRarity.UNCOMMON);
   public static final DeferredItem<MysteryBoxItem> MYSTERY_BOX_RARE = box(QuestRarity.RARE);
   public static final DeferredItem<MysteryBoxItem> MYSTERY_BOX_EPIC = box(QuestRarity.EPIC);
   public static final DeferredItem<MysteryBoxItem> MYSTERY_BOX_LEGENDARY = box(QuestRarity.LEGENDARY);
   public static final DeferredItem<MysteryBoxItem> MYSTERY_BOX_MYTHIC = box(QuestRarity.MYTHIC);

   private static DeferredItem<MysteryBoxItem> box(QuestRarity rarity) {
      return ITEMS.register(
         "mystery_box_" + rarity.name().toLowerCase(java.util.Locale.ROOT), () -> new MysteryBoxItem(rarity, new Item.Properties().stacksTo(16))
      );
   }

   public static DeferredItem<MysteryBoxItem> boxFor(QuestRarity rarity) {
      return switch (rarity) {
         case COMMON -> MYSTERY_BOX_COMMON;
         case UNCOMMON -> MYSTERY_BOX_UNCOMMON;
         case RARE -> MYSTERY_BOX_RARE;
         case EPIC -> MYSTERY_BOX_EPIC;
         case LEGENDARY -> MYSTERY_BOX_LEGENDARY;
         case MYTHIC -> MYSTERY_BOX_MYTHIC;
      };
   }

   public static void register(IEventBus modBus) {
      ITEMS.register(modBus);
   }

   private ModItems() {
   }
}
