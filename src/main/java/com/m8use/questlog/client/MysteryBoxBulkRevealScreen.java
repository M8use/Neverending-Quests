package com.m8use.questlog.client;

import java.util.ArrayList;
import java.util.List;
import com.m8use.questlog.Ids;
import com.m8use.questlog.network.ClientboundMysteryBoxBulkRevealPacket;
import com.m8use.questlog.quest.QuestRarity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Shown after opening two or more stored Mystery Boxes at once from the QuestLog Inventory. Rather
 * than replaying the single-box roulette {@code count} times, this lays every result out in a grid
 * at once, each cell colored by that specific item's own rarity — clearer for a bulk open and far
 * cheaper to render.
 */
public final class MysteryBoxBulkRevealScreen extends Screen {
   private static final int CELL = 30;
   private static final int COLUMNS = 8;

   private final QuestRarity boxRarity;
   private final List<ResultSlot> results;
   private final ConfettiBurst confetti = new ConfettiBurst();

   public MysteryBoxBulkRevealScreen(ClientboundMysteryBoxBulkRevealPacket packet) {
      super(Component.translatable("gui.questlog.mystery_bulk_reveal.title"));
      this.boxRarity = parseRarity(packet.boxRarityId());
      this.results = buildResults(packet);
   }

   private static QuestRarity parseRarity(String name) {
      try {
         return QuestRarity.valueOf(name);
      } catch (IllegalArgumentException exception) {
         return QuestRarity.COMMON;
      }
   }

   private record ResultSlot(ItemStack stack, QuestRarity rarity) {
   }

   private static List<ResultSlot> buildResults(ClientboundMysteryBoxBulkRevealPacket packet) {
      List<ResultSlot> results = new ArrayList<>();
      List<String> itemIds = packet.itemIds();
      List<Integer> amounts = packet.amounts();
      List<String> itemRarityIds = packet.itemRarityIds();

      for (int i = 0; i < itemIds.size(); i++) {
         Item item = Ids.item(itemIds.get(i));
         int amount = i < amounts.size() ? amounts.get(i) : 1;
         QuestRarity rarity = i < itemRarityIds.size() ? parseRarity(itemRarityIds.get(i)) : QuestRarity.COMMON;
         if (item != null) {
            results.add(new ResultSlot(new ItemStack(item, Math.max(1, amount)), rarity));
         }
      }

      return results;
   }

   @Override
   protected void init() {
      if (this.minecraft != null) {
         this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.9F, 0.6F));
      }

      this.confetti.spawnConfetti((float)(this.width / 2), (float)(this.height / 2 - 40), Math.min(80, 20 + this.results.size() * 2));
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      this.onClose();
      return true;
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      this.onClose();
      return true;
   }

   @Override
   public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      WheelRenderer.dimBackground(graphics, this.width, this.height, 0xD2080810);
      int columns = Math.max(1, Math.min(COLUMNS, this.results.size()));
      int rows = (this.results.size() + columns - 1) / columns;
      int gridW = columns * CELL;
      int gridH = rows * CELL;
      int gridX1 = (this.width - gridW) / 2;
      int gridY1 = (this.height - gridH) / 2;
      int centerX = this.width / 2;
      graphics.drawCenteredString(this.font, Component.translatable("gui.questlog.mystery_bulk_reveal.title"), centerX, gridY1 - 34, -1);
      graphics.drawCenteredString(
         this.font,
         Component.translatable("gui.questlog.mystery_bulk_reveal.count", this.results.size(), Component.translatable(this.boxRarity.translationKey())),
         centerX,
         gridY1 - 20,
         this.boxRarity.color()
      );
      WheelRenderer.roundedRect(
         graphics, gridX1 - 6, gridY1 - 6, gridX1 + gridW + 6, gridY1 + gridH + 6, 5, 0x40000000 | this.boxRarity.glowColor() & 16777215
      );

      for (int i = 0; i < this.results.size(); i++) {
         int col = i % columns;
         int row = i / columns;
         int cellX = gridX1 + col * CELL;
         int cellY = gridY1 + row * CELL;
         ResultSlot slot = this.results.get(i);
         int accent = slot.rarity().color();
         if (slot.rarity() == QuestRarity.MYTHIC) {
            float phase = (float)(System.currentTimeMillis() / 5L % 360L) + (float)i * 20.0F;
            WheelRenderer.rainbowRect(graphics, cellX + 1, cellY + 1, cellX + CELL - 1, cellY + CELL - 1, 3, phase);
         } else {
            int top = WheelRenderer.lerpColor(accent, -1, 0.28F);
            int bottom = WheelRenderer.lerpColor(accent, 0xFF000000, 0.3F);
            int border = WheelRenderer.lerpColor(accent, 0xFF000000, 0.5F);
            WheelRenderer.card(graphics, cellX + 1, cellY + 1, cellX + CELL - 1, cellY + CELL - 1, 3, border, top, bottom);
         }

         boolean hovered = mouseX >= cellX && mouseX < cellX + CELL && mouseY >= cellY && mouseY < cellY + CELL;
         if (hovered) {
            WheelRenderer.roundedRect(graphics, cellX + 1, cellY + 1, cellX + CELL - 1, cellY + CELL - 1, 3, 0x33FFFFFF);
         }

         graphics.renderItem(slot.stack(), cellX + 7, cellY + 7);
         graphics.renderItemDecorations(this.font, slot.stack(), cellX + 7, cellY + 7);
      }

      graphics.drawCenteredString(this.font, Component.translatable("gui.questlog.mystery_reveal.dismiss"), centerX, gridY1 + gridH + 20, 0xFFB0B0B0);
      this.confetti.render(graphics);
   }
}
