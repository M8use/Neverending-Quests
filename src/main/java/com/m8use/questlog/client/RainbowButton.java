package com.m8use.questlog.client;

import com.m8use.questlog.quest.QuestRarity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/**
 * A button styled after a target rarity — used for the Mystery Box upgrade action, which should
 * visually represent whatever rarity the upgrade produces (Common -> Uncommon = green, ...,
 * Legendary -> Mythic = an animated rainbow, matching {@link QuestRarity}'s own color scheme).
 */
public final class RainbowButton extends Button {
   private final QuestRarity targetRarity;

   public RainbowButton(int x, int y, int width, int height, Component message, QuestRarity targetRarity, OnPress onPress) {
      super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
      this.targetRarity = targetRarity;
   }

   @Override
   protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      int x1 = this.getX();
      int y1 = this.getY();
      int x2 = x1 + this.width;
      int y2 = y1 + this.height;
      if (this.active) {
         boolean rainbow = this.targetRarity == QuestRarity.MYTHIC;
         if (rainbow) {
            float phase = (float)(System.currentTimeMillis() / 6L % 360L);
            WheelRenderer.rainbowRect(graphics, x1, y1, x2, y2, 4, phase);
         } else {
            int accent = this.targetRarity.color();
            int top = WheelRenderer.lerpColor(accent, -1, 0.2F);
            int border = WheelRenderer.lerpColor(accent, 0xFF000000, 0.3F);
            WheelRenderer.card(graphics, x1, y1, x2, y2, 4, border, top, accent);
         }

         if (this.isHoveredOrFocused()) {
            graphics.fill(x1, y1, x2, y2, 0x33FFFFFF);
         }
      } else {
         WheelRenderer.card(graphics, x1, y1, x2, y2, 4, 0xFF2A2C36, 0xFF3A3E4A, 0xFF262933);
      }

      int textColor = this.active ? -1 : 0xFFA0A0A0;
      net.minecraft.client.gui.Font font = Minecraft.getInstance().font;
      int textWidth = font.width(this.getMessage());
      graphics.drawString(font, this.getMessage(), x1 + (this.width - textWidth) / 2, y1 + (this.height - 8) / 2, textColor, true);
   }
}
