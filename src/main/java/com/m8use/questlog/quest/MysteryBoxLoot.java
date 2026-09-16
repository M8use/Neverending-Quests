package com.m8use.questlog.quest;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * What's actually inside each rarity of Mystery Box. Centralized here so balancing the mod means
 * editing this one file, not touching the open/reveal logic in {@link QuestManager}.
 *
 * <p>Each pool is a {@link SimpleWeightedRandomList} — the number passed to {@code add(entry,
 * weight)} is that entry's relative weight. Most entries within a pool share the same weight (equal
 * odds among themselves); a handful of intentional low-weight "jackpot" entries are called out
 * below wherever they appear, so a lower box can occasionally surprise the player with a
 * higher-rarity item — see {@link Entry#itemRarity}.
 *
 * <p><b>Box rarity and item rarity are different things.</b> Every entry carries its own {@link
 * QuestRarity} independent of which box pool it lives in — a Common box is mostly Common-rarity
 * items, but its jackpot entry is tagged Rare; that tag, not the box's own rarity, is what the
 * opening animation uses to decide the reveal's color and intensity.
 */
public final class MysteryBoxLoot {
   private static Entry of(Item item, int min, int max, QuestRarity itemRarity) {
      return new Entry(item, min, max, itemRarity, List.of(), false);
   }

   private static Entry enchantedBook(QuestRarity itemRarity, EnchantSpec... enchants) {
      return new Entry(Items.ENCHANTED_BOOK, 1, 1, itemRarity, List.of(enchants), false);
   }

   /** An enchanted book that rolls a genuine random (non-curse) enchantment at open time, rather than a fixed one — see {@link #roll}. */
   private static Entry randomEnchantedBook(QuestRarity itemRarity) {
      return new Entry(Items.ENCHANTED_BOOK, 1, 1, itemRarity, List.of(), true);
   }

   public static final SimpleWeightedRandomList<Entry> COMMON = SimpleWeightedRandomList.<Entry>builder()
      .add(of(Items.IRON_INGOT, 4, 8, QuestRarity.UNCOMMON), 3)
      .add(of(Items.COAL, 8, 16, QuestRarity.COMMON), 3)
      .add(of(Items.BREAD, 8, 16, QuestRarity.COMMON), 3)
      .add(of(Items.ARROW, 16, 24, QuestRarity.COMMON), 3)
      .add(of(Items.TORCH, 16, 32, QuestRarity.COMMON), 3)
      .add(of(Items.CARROT, 5, 15, QuestRarity.COMMON), 3)
      .add(of(Items.BONE, 1, 5, QuestRarity.COMMON), 3)
      .add(of(Items.COBBLESTONE, 16, 64, QuestRarity.COMMON), 3)
      .add(of(Items.COAL, 16, 16, QuestRarity.COMMON), 3)
      .add(of(Items.TORCH, 16, 16, QuestRarity.COMMON), 3)
      .add(of(Items.BREAD, 16, 16, QuestRarity.COMMON), 3)
      .add(of(Items.LEATHER, 4, 8, QuestRarity.COMMON), 3)
      .add(of(Items.WHEAT, 12, 20, QuestRarity.COMMON), 3)
      .add(of(Items.STRING, 8, 16, QuestRarity.COMMON), 3)
      .add(of(Items.DIAMOND, 1, 1, QuestRarity.RARE), 1) // jackpot: a Common box can still surprise you
      .build();

   public static final SimpleWeightedRandomList<Entry> UNCOMMON = SimpleWeightedRandomList.<Entry>builder()
      .add(of(Items.GOLD_INGOT, 4, 10, QuestRarity.UNCOMMON), 4)
      .add(of(Items.EMERALD, 3, 15, QuestRarity.UNCOMMON), 4)
      .add(of(Items.COOKED_BEEF, 16, 24, QuestRarity.COMMON), 4)
      .add(of(Items.ICE, 4, 16, QuestRarity.COMMON), 4)
      .add(of(Items.IRON_PICKAXE, 1, 1, QuestRarity.UNCOMMON), 4)
      .add(of(Items.DIAMOND, 2, 2, QuestRarity.EPIC), 1) // jackpot
      .build();

   public static final SimpleWeightedRandomList<Entry> RARE = SimpleWeightedRandomList.<Entry>builder()
      .add(of(Items.DIAMOND, 2, 4, QuestRarity.RARE), 4)
      .add(of(Items.GOLDEN_CARROT, 8, 12, QuestRarity.UNCOMMON), 4)
      .add(of(Items.EMERALD, 8, 12, QuestRarity.UNCOMMON), 4)
      .add(randomEnchantedBook(QuestRarity.RARE), 4)
      .add(of(Items.GOLD_INGOT, 8, 16, QuestRarity.UNCOMMON), 4)
      .add(of(Items.IRON_INGOT, 8, 16, QuestRarity.UNCOMMON), 4)
      .add(of(Items.NETHERITE_SCRAP, 1, 1, QuestRarity.LEGENDARY), 1) // jackpot
      .build();

   public static final SimpleWeightedRandomList<Entry> EPIC = SimpleWeightedRandomList.<Entry>builder()
      .add(of(Items.DIAMOND, 5, 8, QuestRarity.EPIC), 4)
      .add(of(Items.DIAMOND_PICKAXE, 1, 1, QuestRarity.RARE), 4)
      .add(of(Items.DIAMOND_SWORD, 1, 1, QuestRarity.RARE), 4)
      .add(of(Items.GOLDEN_APPLE, 3, 5, QuestRarity.RARE), 4)
      .add(of(Items.EMERALD, 16, 32, QuestRarity.RARE), 4)
      .add(of(Items.NETHERITE_INGOT, 1, 1, QuestRarity.LEGENDARY), 1) // jackpot
      .build();

   public static final SimpleWeightedRandomList<Entry> LEGENDARY = SimpleWeightedRandomList.<Entry>builder()
      .add(of(Items.NETHERITE_INGOT, 1, 2, QuestRarity.MYTHIC), 1)
      .add(of(Items.DIAMOND, 12, 18, QuestRarity.EPIC), 1)
      .add(of(Items.ENCHANTED_GOLDEN_APPLE, 1, 1, QuestRarity.LEGENDARY), 1)
      .add(of(Items.TOTEM_OF_UNDYING, 1, 1, QuestRarity.LEGENDARY), 1)
      .add(of(Items.NETHERITE_SCRAP, 3, 5, QuestRarity.EPIC), 1)
      .add(of(Items.TRIDENT, 1, 1, QuestRarity.LEGENDARY), 1)
      .add(of(Items.OBSIDIAN, 32, 64, QuestRarity.UNCOMMON), 1)
      .add(enchantedBook(QuestRarity.LEGENDARY, new EnchantSpec(Enchantments.MENDING, 1)), 1)
      .build();

   /**
    * The Mythic-tier "kitchen sink" enchanted book: Mending, Sharpness V, Looting III, Fortune
    * III, Protection IV and Unbreaking III all on one book. Vanilla's normal enchanting/anvil
    * rules would never let several of these coexist (Protection is armor-only, Sharpness/Looting/
    * Fortune are tool-or-weapon-only, and anvils enforce exclusivity groups) — {@link
    * ItemStack#enchant} bypasses all of that by writing the enchantments straight into the item's
    * {@code ItemEnchantments} data component, which is the only reliable way to produce a book
    * like this on the current version. The result is a real, functional item: every one of those
    * six enchantments is genuinely present and active, not a cosmetic label.
    */
   private static final EnchantSpec[] MYTHIC_BOOK = {
      new EnchantSpec(Enchantments.MENDING, 1),
      new EnchantSpec(Enchantments.SHARPNESS, 5),
      new EnchantSpec(Enchantments.LOOTING, 3),
      new EnchantSpec(Enchantments.FORTUNE, 3),
      new EnchantSpec(Enchantments.PROTECTION, 4),
      new EnchantSpec(Enchantments.UNBREAKING, 3),
           new EnchantSpec(Enchantments.EFFICIENCY, 5),
           new EnchantSpec(Enchantments.LURE, 3)
   }
;
    private static final EnchantSpec[] MYTHIC_BOOK_2 = {
            new EnchantSpec(Enchantments.MENDING, 1),
            new EnchantSpec(Enchantments.SHARPNESS, 5),
            new EnchantSpec(Enchantments.LOOTING, 3),
            new EnchantSpec(Enchantments.SILK_TOUCH, 1),
            new EnchantSpec(Enchantments.PROTECTION, 4),
            new EnchantSpec(Enchantments.UNBREAKING, 3),
            new EnchantSpec(Enchantments.EFFICIENCY, 5),
            new EnchantSpec(Enchantments.WIND_BURST, 3),
            new EnchantSpec(Enchantments.LUCK_OF_THE_SEA, 3),
            new EnchantSpec(Enchantments.RIPTIDE, 3)

   };

   public static final SimpleWeightedRandomList<Entry> MYTHIC = SimpleWeightedRandomList.<Entry>builder()
      .add(of(Items.ELYTRA, 1, 1, QuestRarity.MYTHIC), 1)
      .add(of(Items.NETHERITE_INGOT, 4, 6, QuestRarity.LEGENDARY), 1)
      .add(of(Items.TOTEM_OF_UNDYING, 2, 5, QuestRarity.LEGENDARY), 1)
      .add(of(Items.NETHER_STAR, 1, 1, QuestRarity.MYTHIC), 1)
      .add(of(Items.DRAGON_EGG, 1, 1, QuestRarity.MYTHIC), 1)
      .add(of(Items.MACE, 1, 1, QuestRarity.MYTHIC), 1)
      .add(of(Items.DIAMOND_BLOCK, 8, 8, QuestRarity.EPIC), 1)
      .add(of(Items.END_CRYSTAL, 32, 64, QuestRarity.EPIC), 1)
      .add(of(Items.ENCHANTED_GOLDEN_APPLE, 3, 5, QuestRarity.LEGENDARY), 1)
      .add(enchantedBook(QuestRarity.MYTHIC, MYTHIC_BOOK), 1)
           .add(enchantedBook(QuestRarity.MYTHIC, MYTHIC_BOOK_2), 1)

           .build();

   public static SimpleWeightedRandomList<Entry> pool(QuestRarity rarity) {
      return switch (rarity) {
         case COMMON -> COMMON;
         case UNCOMMON -> UNCOMMON;
         case RARE -> RARE;
         case EPIC -> EPIC;
         case LEGENDARY -> LEGENDARY;
         case MYTHIC -> MYTHIC;
      };
   }

   public static RollResult roll(QuestRarity boxRarity, RandomSource random, RegistryAccess registryAccess) {
      Entry entry = pool(boxRarity).getRandomValue(random).orElse(null);
      if (entry == null) {
         return new RollResult(ItemStack.EMPTY, QuestRarity.COMMON);
      } else {
         int amount = entry.min >= entry.max ? entry.min : entry.min + random.nextInt(entry.max - entry.min + 1);
         if (entry.randomEnchant) {
            ItemStack randomBook = QuestManager.randomEnchantedBook(registryAccess, amount, random);
            return new RollResult(randomBook, entry.itemRarity);
         }

         ItemStack stack = new ItemStack(entry.item, amount);
         if (!entry.enchants.isEmpty()) {
            var enchantmentRegistry = registryAccess.registryOrThrow(Registries.ENCHANTMENT);

            for (EnchantSpec spec : entry.enchants) {
               Holder<Enchantment> holder = enchantmentRegistry.getHolderOrThrow(spec.key);
               stack.enchant(holder, spec.level);
            }
         }

         return new RollResult(stack, entry.itemRarity);
      }
   }

   /** A handful of representative (item, rarity) pairs for the roulette strip, independent of an actual roll. */
   public static List<PreviewEntry> previewEntries(QuestRarity boxRarity) {
      return pool(boxRarity).unwrap().stream().map(wrapper -> new PreviewEntry(wrapper.data().item, wrapper.data().min, wrapper.data().max, wrapper.data().itemRarity)).toList();
   }

   /** The result of opening one box: the actual granted stack, and the rarity that stack itself should be presented with. */
   public record RollResult(ItemStack stack, QuestRarity itemRarity) {
   }

   /** A (item, quantity range, rarity) triple for populating the roulette strip's non-winning slots — the actual amount awarded (if this ever became the winner) is rolled fresh, since these slots are never actually granted. */
   public record PreviewEntry(Item item, int min, int max, QuestRarity itemRarity) {
   }

   record EnchantSpec(ResourceKey<Enchantment> key, int level) {
   }

   static final class Entry {
      final Item item;
      final int min;
      final int max;
      final QuestRarity itemRarity;
      final List<EnchantSpec> enchants;
      final boolean randomEnchant;

      Entry(Item item, int min, int max, QuestRarity itemRarity, List<EnchantSpec> enchants, boolean randomEnchant) {
         this.item = item;
         this.min = min;
         this.max = max;
         this.itemRarity = itemRarity;
         this.enchants = enchants;
         this.randomEnchant = randomEnchant;
      }
   }

   private MysteryBoxLoot() {
   }
}
