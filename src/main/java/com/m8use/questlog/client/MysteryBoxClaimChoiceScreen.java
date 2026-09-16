package com.m8use.questlog.client;

import com.m8use.questlog.network.QuestNetwork;
import com.m8use.questlog.network.ServerboundOpenStoredBoxesPacket;
import com.m8use.questlog.quest.QuestRarity;
import com.m8use.questlog.registry.ModItems;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;

/**
 * Shown right after a Mystery Quest is claimed. By the time this appears, the box has already been
 * added to the player's permanent QuestLog collection (see {@link
 * com.m8use.questlog.network.ClientboundMysteryBoxEarnedPacket}) — this screen never risks losing
 * it. "Open Now" is a convenience that immediately spends the box that's already stored; dismissing
 * this screen any other way (Keep It, Escape) simply leaves it stored, which is the default.
 */
public final class MysteryBoxClaimChoiceScreen extends Screen {
   private static final int CARD_W = 210;
   private static final int CARD_H = 118;

   private final Screen parent;
   private final QuestRarity rarity;

   public MysteryBoxClaimChoiceScreen(Screen parent, QuestRarity rarity) {
      super(Component.translatable("gui.questlog.mystery_earned.title"));
      this.parent = parent;
      this.rarity = rarity;
   }

   @Override
   protected void init() {
      int x1 = (this.width - CARD_W) / 2;
      int y1 = (this.height - CARD_H) / 2;
      int buttonY = y1 + CARD_H - 28;
      int buttonWidth = 96;
      int centerX = x1 + CARD_W / 2;
      this.addRenderableWidget(
         StyledButton.goldWhiteText(
            centerX - buttonWidth - 4, buttonY, buttonWidth, 20, Component.translatable("gui.questlog.mystery_earned.keep_it"), b -> this.onClose()
         )
      );
      this.addRenderableWidget(
         StyledButton.blue(
            centerX + 4, buttonY, buttonWidth, 20, Component.translatable("gui.questlog.mystery_earned.open_now"), b -> this.openNow()
         )
      );
   }

   private void openNow() {
      if (this.minecraft != null) {
         this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F));
      }

      QuestNetwork.toServer(new ServerboundOpenStoredBoxesPacket(this.rarity.name(), 1));
      this.onClose();
   }

   @Override
   public void onClose() {
      if (this.minecraft != null) {
         this.minecraft.setScreen(this.parent);
      }
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   @Override
   public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      graphics.fill(0, 0, this.width, this.height, 0xAA0A0A12);
      int x1 = (this.width - CARD_W) / 2;
      int y1 = (this.height - CARD_H) / 2;
      int x2 = x1 + CARD_W;
      int y2 = y1 + CARD_H;
      int centerX = (x1 + x2) / 2;
      WheelRenderer.card(graphics, x1, y1, x2, y2, 6, this.rarity.color(), WheelRenderer.lerpColor(this.rarity.color(), -1, 0.22F), 0xFF1B1B24);
      graphics.drawCenteredString(this.font, this.getTitle(), centerX, y1 + 8, -1);
      graphics.drawCenteredString(this.font, Component.translatable(this.rarity.translationKey()), centerX, y1 + 19, this.rarity.color());
      ItemStack boxStack = new ItemStack(ModItems.boxFor(this.rarity).get());
      float pulse = 0.5F + 0.5F * (float)Math.sin((double)System.currentTimeMillis() / 260.0);
      int rayAlpha = (int)(90.0F + 60.0F * pulse);
      int rayColor = rayAlpha << 24 | this.rarity.glowColor() & 16777215;
      float rayRotation = (float)(System.currentTimeMillis() / 20L % 360L);
      WheelRenderer.sunRays(graphics, (float)centerX, (float)(y1 + 52), 26.0F, rayRotation, 8, rayColor);
      graphics.pose().pushPose();
      graphics.pose().translate((float)centerX, (float)(y1 + 44), 0.0F);
      graphics.pose().scale(1.6F, 1.6F, 1.0F);
      graphics.renderItem(boxStack, -8, -8);
      graphics.pose().popPose();
      graphics.drawCenteredString(this.font, Component.translatable("gui.questlog.mystery_earned.subtitle"), centerX, y1 + 70, 0xFFB0B0B0);
      // See MysteryBoxInventoryScreen for why we don't call super.render() here.
      for (net.minecraft.client.gui.components.Renderable renderable : this.renderables) {
         renderable.render(graphics, mouseX, mouseY, partialTick);
      }
   }
}
