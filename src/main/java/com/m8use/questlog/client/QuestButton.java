package com.m8use.questlog.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.fml.ModList;

/**
 * Stacks under the Upgrader and Daily Rewards buttons, in that fixed order — but only reserves
 * space for whichever of those two mods is actually installed, detected via {@link ModList} at
 * class-load time (mod presence never changes mid-session, so this is computed once rather than
 * every frame). With neither installed this lands at the very top instead of floating with an
 * empty gap above it; with one installed it takes the next open slot; with both, it's 3rd exactly
 * as before. Each mod only ever checks for the ones "above" it in this same fixed order — Upgrader
 * checks nothing, Daily Rewards checks only for Upgrader, this checks for both — so there's no
 * circular dependency between them.
 */
public final class QuestButton extends Button {
   public static final int SIZE = 26;
   private static final String UPGRADER_MOD_ID = "upgrader";
   private static final String DAILY_REWARDS_MOD_ID = "dailyrewards";
   private static final int OTHER_BUTTONS_RESERVED = countLoadedCompanions() * (SIZE + 6);
   private static final int BEVEL_OUTLINE = 0xFF160B1F;
   private static final int BEVEL_HIGHLIGHT = 0xFFC48CFF;
   private static final int BEVEL_SHADOW = 0xFF3A1560;
   private static final int FILL_TOP = 0xFFA855F7;
   private static final int FILL_BOTTOM = 0xFF6B21A8;
   private static final int HOVER_WASH = 0x33FFFFFF;
   private static final int CAP_OUTLINE = 0xFF2A1A0E;
   private static final int CAP_TOP = 0xFF8B5A2B;
   private static final int CAP_BOTTOM = 0xFF5C3A1B;
   private static final int PAGE_OUTLINE = 0xFF2A1A0E;
   private static final int PAGE_TOP = 0xFFF3E4C0;
   private static final int PAGE_BOTTOM = 0xFFD8BE88;
   private static final int PAGE_LINE = 0xFFB49A66;

   private final InventoryScreen inventoryScreen;

   private QuestButton(InventoryScreen inventoryScreen) {
      super(0, 0, SIZE, SIZE, Component.translatable("gui.questlog.button"), QuestButton::press, DEFAULT_NARRATION);
      this.inventoryScreen = inventoryScreen;
      this.setTooltip(Tooltip.create(Component.translatable("gui.questlog.button")));
   }

   public static QuestButton create(InventoryScreen inventoryScreen) {
      return new QuestButton(inventoryScreen);
   }

   /** Counts how many of the two "earlier in the stack" companion mods are actually installed, in the {@code upgrader} -> {@code dailyrewards} -> (this mod) order the three mods agree on. */
   private static int countLoadedCompanions() {
      int count = 0;
      if (ModList.get().isLoaded(UPGRADER_MOD_ID)) {
         count++;
      }

      if (ModList.get().isLoaded(DAILY_REWARDS_MOD_ID)) {
         count++;
      }

      return count;
   }

   private static void press(Button button) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.player != null) {
         minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 0.4F));
         minecraft.setScreen(new QuestScreen());
      }
   }

   private void followInventoryLayout() {
      int x = Math.min(this.inventoryScreen.getGuiLeft() + this.inventoryScreen.getXSize() + 6, this.inventoryScreen.width - SIZE - 4);
      int y = this.inventoryScreen.getGuiTop() + 6 + OTHER_BUTTONS_RESERVED;
      this.setX(x);
      this.setY(y);
   }

   @Override
   protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      this.followInventoryLayout();
      int x1 = this.getX();
      int y1 = this.getY();
      int x2 = x1 + this.width;
      int y2 = y1 + this.height;
      float cx = (float)(x1 + x2) / 2.0F;
      float cy = (float)(y1 + y2) / 2.0F;
      if (QuestClientState.hasAnyClaimable()) {
         float rotation = (float)(System.currentTimeMillis() / 30L % 360L);
         float pulse = 0.5F + 0.5F * (float)Math.sin((double)System.currentTimeMillis() / 400.0);
         int alpha = (int)(90.0F + 60.0F * pulse);
         WheelRenderer.sunRays(graphics, cx, cy, (float)SIZE * 0.9F, rotation, 10, alpha << 24 | 0xFFE9A8);
      }

      WheelRenderer.pixelBevelBorder(graphics, x1, y1, x2, y2, BEVEL_OUTLINE, BEVEL_HIGHLIGHT, BEVEL_SHADOW);
      WheelRenderer.roundedRect(graphics, x1 + 2, y1 + 2, x2 - 2, y2 - 2, 0, FILL_TOP, FILL_BOTTOM);
      if (this.isHoveredOrFocused()) {
         graphics.fill(x1 + 2, y1 + 2, x2 - 2, y2 - 2, HOVER_WASH);
      }

      this.drawScrollIcon(graphics, Math.round(cx), Math.round(cy));
   }

   private void drawScrollIcon(GuiGraphics graphics, int cx, int cy) {
      int left = cx - 8;
      int right = cx + 8;
      int top = cy - 6;
      int bottom = cy + 6;
      WheelRenderer.card(graphics, left + 3, top, right - 3, bottom, 1, PAGE_OUTLINE, PAGE_TOP, PAGE_BOTTOM);
      WheelRenderer.card(graphics, left, top - 1, left + 5, bottom + 1, 2, CAP_OUTLINE, CAP_TOP, CAP_BOTTOM);
      WheelRenderer.card(graphics, right - 5, top - 1, right, bottom + 1, 2, CAP_OUTLINE, CAP_TOP, CAP_BOTTOM);
      graphics.fill(left + 5, cy - 2, right - 5, cy - 1, PAGE_LINE);
      graphics.fill(left + 5, cy + 1, right - 5, cy + 2, PAGE_LINE);
   }
}
