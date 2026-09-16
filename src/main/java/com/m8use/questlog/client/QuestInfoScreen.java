package com.m8use.questlog.client;

import java.util.List;
import com.m8use.questlog.quest.QuestInfoText;
import com.m8use.questlog.quest.QuestTemplate;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;

/**
 * "What exactly does this quest want?" — a small popup over the QuestLog showing an explanation
 * generated straight from the quest's own definition (see {@link QuestInfoText}), for Daily/Weekly/
 * Monthly quests only. Mystery quests don't get this button; their UI stays focused on the box/
 * reward systems.
 */
public final class QuestInfoScreen extends Screen {
   private static final int WIDTH = 240;
   private static final int PAD = 10;
   private static final int LINE_HEIGHT = 11;

   private final Screen parent;
   private final QuestTemplate template;
   private final List<String> lines;

   public QuestInfoScreen(Screen parent, QuestTemplate template) {
      super(Component.translatable(template.nameKey()));
      this.parent = parent;
      this.template = template;
      this.lines = QuestInfoText.describe(template);
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   @Override
   public void onClose() {
      if (this.minecraft != null) {
         this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 0.8F));
         this.minecraft.setScreen(this.parent);
      }
   }

   private int accentColor() {
      return this.template.rarity() != null ? this.template.rarity().color() : categoryAccent(this.template.category());
   }

   private static int categoryAccent(com.m8use.questlog.quest.QuestCategory category) {
      return switch (category) {
         case DAILY -> 0xFF3F9BFF;
         case WEEKLY -> 0xFFB24EFF;
         case MONTHLY -> 0xFFFFB020;
         case MYSTERY -> 0xFFFF4FA3;
      };
   }

   private List<FormattedCharSequence> wrappedLines() {
      int textWidth = WIDTH - PAD * 2;
      return this.font.split(Component.literal(String.join("\n\n", this.lines)), textWidth);
   }

   private int panelHeight(int wrappedLineCount) {
      return PAD + 16 + 6 + wrappedLineCount * LINE_HEIGHT + PAD;
   }

   @Override
   public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      WheelRenderer.dimBackground(graphics, this.width, this.height, 0x90000000);
      List<FormattedCharSequence> wrapped = this.wrappedLines();
      int height = this.panelHeight(wrapped.size());
      int x1 = (this.width - WIDTH) / 2;
      int y1 = (this.height - height) / 2;
      int x2 = x1 + WIDTH;
      int y2 = y1 + height;
      int accent = this.accentColor();
      WheelRenderer.card(graphics, x1, y1, x2, y2, 6, accent, WheelRenderer.lerpColor(accent, 0xFF000000, 0.55F), 0xFF14141C);
      int centerX = (x1 + x2) / 2;
      graphics.drawCenteredString(this.font, this.getTitle(), centerX, y1 + PAD, -1);
      if (this.template.rarity() != null) {
         graphics.drawCenteredString(this.font, Component.translatable(this.template.rarity().translationKey()), centerX, y1 + PAD + 11, accent);
      }

      int textY = y1 + PAD + 22;

      for (FormattedCharSequence line : wrapped) {
         graphics.drawString(this.font, line, x1 + PAD, textY, 0xFFDCDCDC, false);
         textY += LINE_HEIGHT;
      }

      int closeSize = 14;
      int closeX1 = x2 - closeSize - 4;
      int closeY1 = y1 + 4;
      boolean hoveredClose = mouseX >= closeX1 && mouseX < closeX1 + closeSize && mouseY >= closeY1 && mouseY < closeY1 + closeSize;
      WheelRenderer.card(
         graphics, closeX1, closeY1, closeX1 + closeSize, closeY1 + closeSize, 3, 0xFF7A140C, hoveredClose ? 0xFFF15A4E : 0xFFD8332A, 0xFFB82A20
      );
      graphics.drawCenteredString(this.font, "X", closeX1 + closeSize / 2, closeY1 + 3, -1);
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      List<FormattedCharSequence> wrapped = this.wrappedLines();
      int height = this.panelHeight(wrapped.size());
      int x1 = (this.width - WIDTH) / 2;
      int y1 = (this.height - height) / 2;
      int x2 = x1 + WIDTH;
      int y2 = y1 + height;
      int closeSize = 14;
      int closeX1 = x2 - closeSize - 4;
      int closeY1 = y1 + 4;
      if (mouseX >= closeX1 && mouseX < closeX1 + closeSize && mouseY >= closeY1 && mouseY < closeY1 + closeSize) {
         this.onClose();
         return true;
      } else if (mouseX < (double)x1 || mouseX >= (double)x2 || mouseY < (double)y1 || mouseY >= (double)y2) {
         this.onClose();
         return true;
      } else {
         return true;
      }
   }
}
