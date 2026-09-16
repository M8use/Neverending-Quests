package com.m8use.questlog.client;

import java.util.List;
import com.m8use.questlog.network.QuestNetwork;
import com.m8use.questlog.network.ServerboundOpenStoredBoxesPacket;
import com.m8use.questlog.network.ServerboundUpgradeBoxPacket;
import com.m8use.questlog.quest.QuestRarity;
import com.m8use.questlog.registry.ModItems;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

/**
 * The QuestLog's dedicated permanent storage for Mystery Boxes — separate from, and never
 * occupying slots in, the player's normal Minecraft inventory. Opened from the "Inventory" button
 * in the top-right of the main QuestLog menu.
 */
public final class MysteryBoxInventoryScreen extends Screen {
   private static final int WIDTH = 300;
   private static final int HEIGHT = 250;
   private static final int PAD = 6;
   private static final int HEADER_H = 22;
   private static final int LIST_WIDTH = 140;
   private static final int ROW_H = 34;
   private static final int TEXT_WHITE = -1;
   private static final int TEXT_DIM = 0xFFA0A0A0;

   private final Screen parent;
   private int leftPos;
   private int topPos;
   private QuestRarity selectedRarity = QuestRarity.COMMON;
   private int openCount = 1;

   private Button decrementButton;
   private Button incrementButton;
   private Button openButton;
   private RainbowButton upgradeButton;
   private final ConfettiBurst confetti = new ConfettiBurst();

   public MysteryBoxInventoryScreen(Screen parent) {
      super(Component.translatable("gui.questlog.inventory.title"));
      this.parent = parent;
   }

   @Override
   protected void init() {
      this.leftPos = (this.width - WIDTH) / 2;
      this.topPos = (this.height - HEIGHT) / 2;
      this.addDetailWidgets();
   }

   private int detailX1() {
      return this.leftPos + PAD + LIST_WIDTH + 10;
   }

   private int detailX2() {
      return this.leftPos + WIDTH - PAD;
   }

   private int stepperY() {
      return this.topPos + HEADER_H + 100;
   }

   private void addDetailWidgets() {
      int x1 = this.detailX1();
      int x2 = this.detailX2();
      int width = x2 - x1;
      int stepperY = this.stepperY();
      this.decrementButton = this.addRenderableWidget(StyledButton.blue(x1, stepperY, 20, 20, Component.literal("-"), b -> this.changeCount(-1)));
      this.incrementButton = this.addRenderableWidget(StyledButton.blue(x2 - 20, stepperY, 20, 20, Component.literal("+"), b -> this.changeCount(1)));
      this.openButton = this.addRenderableWidget(
         StyledButton.gold(x1, stepperY + 24, width, 20, Component.translatable("gui.questlog.inventory.open"), b -> this.openBoxes())
      );
      QuestRarity next = this.selectedRarity.next();
      Component upgradeLabel = next == null
         ? Component.translatable("gui.questlog.inventory.max_rarity")
         : Component.translatable("gui.questlog.inventory.upgrade", QuestRarity.UPGRADE_COST, Component.translatable(next.translationKey()));
      QuestRarity buttonTarget = next == null ? this.selectedRarity : next;
      this.upgradeButton = this.addRenderableWidget(new RainbowButton(x1, stepperY + 50, width, 20, upgradeLabel, buttonTarget, b -> this.upgrade()));
      this.refreshWidgetState();
   }

   @Override
   public void tick() {
      super.tick();
      this.refreshWidgetState();
   }

   private void refreshWidgetState() {
      int owned = QuestClientState.mysteryBoxCount(this.selectedRarity);
      this.openCount = Math.max(1, Math.min(this.openCount, Math.max(1, owned)));
      boolean hasAny = owned > 0;
      this.decrementButton.active = hasAny && this.openCount > 1;
      this.incrementButton.active = hasAny && this.openCount < owned;
      this.openButton.active = hasAny;
      QuestRarity next = this.selectedRarity.next();
      this.upgradeButton.visible = next != null;
      this.upgradeButton.active = next != null && owned >= QuestRarity.UPGRADE_COST;
   }

   private void changeCount(int delta) {
      int owned = QuestClientState.mysteryBoxCount(this.selectedRarity);
      this.openCount = Math.max(1, Math.min(this.openCount + delta, Math.max(1, owned)));
      this.playClick();
   }

   private void openBoxes() {
      if (QuestClientState.mysteryBoxCount(this.selectedRarity) > 0) {
         QuestNetwork.toServer(new ServerboundOpenStoredBoxesPacket(this.selectedRarity.name(), this.openCount));
         this.playClick();
         this.openCount = 1;
      }
   }

   private void upgrade() {
      QuestRarity next = this.selectedRarity.next();
      if (next != null && QuestClientState.mysteryBoxCount(this.selectedRarity) >= QuestRarity.UPGRADE_COST) {
         QuestNetwork.toServer(new ServerboundUpgradeBoxPacket(this.selectedRarity.name()));
         this.playClick();
      }
   }

   /**
    * Called only once the server has confirmed the upgrade actually succeeded (see {@link
    * com.m8use.questlog.network.ClientboundBoxUpgradedPacket}) — the underlying collection data is
    * already correct by this point; this is purely the celebratory feedback layer on top of it.
    */
   public void onUpgraded(QuestRarity toRarity) {
      if (this.minecraft != null) {
         this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 0.7F));
      }

      int centerX = (this.detailX1() + this.detailX2()) / 2;
      int iconCy = this.topPos + HEADER_H + 6 + 34;
      this.confetti.spawnConfetti((float)centerX, (float)iconCy, 40);
   }

   private void selectRarity(QuestRarity rarity) {
      if (rarity != this.selectedRarity) {
         this.selectedRarity = rarity;
         this.openCount = 1;
         this.playClick();
         this.clearWidgets();
         this.addDetailWidgets();
      }
   }

   private void playClick() {
      if (this.minecraft != null) {
         this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F));
      }
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   @Override
   public void onClose() {
      if (this.minecraft != null) {
         this.minecraft.setScreen(this.parent);
      }
   }

   @Override
   public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      WheelRenderer.dimBackground(graphics, this.width, this.height, 0x60000000);
      int x1 = this.leftPos;
      int y1 = this.topPos;
      int x2 = this.leftPos + WIDTH;
      int y2 = this.topPos + HEIGHT;
      WheelRenderer.roundedRect(graphics, x1, y1, x2, y2, 6, -13024934);
      boolean rainbow = this.selectedRarity == QuestRarity.MYTHIC;
      int darkBase = WheelRenderer.lerpColor(this.selectedRarity.color(), 0xFF000000, 0.86F);
      float backgroundPhase = (float)(System.currentTimeMillis() % 9000L) / 9000.0F;
      WheelRenderer.movingSheenBackground(graphics, x1 + 1, y1 + 1, x2 - 1, y2 - 1, 5, darkBase, this.selectedRarity.color(), backgroundPhase, rainbow);
      graphics.drawString(this.font, this.getTitle(), x1 + PAD + 2, y1 + 7, TEXT_WHITE, false);
      int closeX2 = x2 - PAD;
      int closeX1 = closeX2 - 16;
      int closeY1 = y1 + 4;
      WheelRenderer.card(graphics, closeX1, closeY1, closeX2, closeY1 + 14, 3, 0xFF7A140C, 0xFFF15A4E, 0xFFD8332A);
      graphics.drawCenteredString(this.font, "X", (closeX1 + closeX2) / 2, closeY1 + 3, TEXT_WHITE);
      this.renderRarityList(graphics, mouseX, mouseY);
      this.renderDetailPanel(graphics);
      this.confetti.render(graphics);
      // Deliberately not calling super.render(...) here: Screen's default render() calls
      // renderBackground() (the vanilla blur effect) AFTER rendering its own content, and doing
      // that here would blur everything we just drew above. Render the widgets directly instead.
      for (net.minecraft.client.gui.components.Renderable renderable : this.renderables) {
         renderable.render(graphics, mouseX, mouseY, partialTick);
      }
   }

   private void renderRarityList(GuiGraphics graphics, int mouseX, int mouseY) {
      int listX1 = this.leftPos + PAD;
      int listX2 = listX1 + LIST_WIDTH;
      int listY = this.topPos + HEADER_H + 4;

      for (int i = 0; i < QuestRarity.values().length; i++) {
         QuestRarity rarity = QuestRarity.values()[i];
         int rowY1 = listY + i * ROW_H;
         int rowY2 = rowY1 + ROW_H - 2;
         boolean selected = rarity == this.selectedRarity;
         int owned = QuestClientState.mysteryBoxCount(rarity);
         boolean hovered = mouseX >= listX1 && mouseX < listX2 && mouseY >= rowY1 && mouseY < rowY2;
         int border = selected ? rarity.color() : 0xFF14141C;
         int top = selected ? WheelRenderer.lerpColor(rarity.color(), 0xFF000000, 0.45F) : 0xFF23252F;
         int bottom = selected ? WheelRenderer.lerpColor(rarity.color(), 0xFF000000, 0.65F) : 0xFF1B1C24;
         WheelRenderer.card(graphics, listX1, rowY1, listX2, rowY2, 4, border, top, bottom);
         if (hovered && !selected) {
            WheelRenderer.roundedRect(graphics, listX1 + 1, rowY1 + 1, listX2 - 1, rowY2 - 1, 3, 0x22FFFFFF);
         }

         if (owned > 0 && rarity != this.selectedRarity) {
            float phase = (float)(System.currentTimeMillis() % 3000L) / 3000.0F * 360.0F;
            WheelRenderer.movingOutline(graphics, listX1, rowY1, listX2, rowY2, 1, rarity.color(), phase, rarity == QuestRarity.MYTHIC, 90);
         }

         ItemStack boxStack = new ItemStack(ModItems.boxFor(rarity).get());
         int iconCy = (rowY1 + rowY2) / 2;
         graphics.renderItem(boxStack, listX1 + 6, iconCy - 8);
         String nameText = Component.translatable(rarity.translationKey()).getString();
         if (owned > 0 && rarity == QuestRarity.MYTHIC) {
            float textPhase = (float)(System.currentTimeMillis() / 5L % 360L);
            WheelRenderer.drawRainbowText(graphics, this.font, nameText, listX1 + 28, rowY1 + 5, textPhase);
         } else {
            int nameColor = owned > 0 ? rarity.color() : TEXT_DIM;
            graphics.drawString(this.font, nameText, listX1 + 28, rowY1 + 5, nameColor, false);
         }

         Component countText = Component.literal("x" + owned);
         int countWidth = this.font.width(countText);
         graphics.drawString(this.font, countText, listX2 - 10 - countWidth, rowY1 + 5, owned > 0 ? rarity.color() : TEXT_DIM, false);
      }
   }

   private void renderDetailPanel(GuiGraphics graphics) {
      int x1 = this.detailX1();
      int x2 = this.detailX2();
      int centerX = (x1 + x2) / 2;
      int owned = QuestClientState.mysteryBoxCount(this.selectedRarity);
      int titleY = this.topPos + HEADER_H + 6;
      String titleText = Component.translatable(this.selectedRarity.translationKey()).getString();
      if (this.selectedRarity == QuestRarity.MYTHIC) {
         float textPhase = (float)(System.currentTimeMillis() / 5L % 360L);
         WheelRenderer.drawRainbowTextCentered(graphics, this.font, titleText, centerX, titleY, textPhase);
      } else {
         graphics.drawCenteredString(this.font, titleText, centerX, titleY, this.selectedRarity.color());
      }

      int iconCy = titleY + 34;
      float rayRotation = (float)(System.currentTimeMillis() / 25L % 360L);
      int rayColor = 0x40000000 | this.selectedRarity.glowColor() & 16777215;
      WheelRenderer.sunRays(graphics, (float)centerX, (float)iconCy, 22.0F, rayRotation, 8, rayColor);
      ItemStack boxStack = new ItemStack(ModItems.boxFor(this.selectedRarity).get());
      graphics.pose().pushPose();
      graphics.pose().translate((float)centerX, (float)iconCy, 0.0F);
      graphics.pose().scale(1.6F, 1.6F, 1.0F);
      graphics.renderItem(boxStack, -8, -8);
      graphics.pose().popPose();
      Component ownText = Component.translatable("gui.questlog.inventory.you_own", owned);
      graphics.drawCenteredString(this.font, ownText, centerX, iconCy + 22, TEXT_WHITE);
      Component countLabel = Component.literal(String.valueOf(this.openCount));
      graphics.drawCenteredString(this.font, countLabel, centerX, this.stepperY() + 6, TEXT_WHITE);
      if (owned <= 0) {
         this.renderWrappedCenteredMessage(graphics, Component.translatable("gui.questlog.inventory.none_owned"), x1, x2, centerX, this.stepperY() + 80);
      } else if (this.selectedRarity == QuestRarity.MYTHIC) {
         this.renderWrappedCenteredMessage(graphics, Component.translatable("gui.questlog.inventory.unlimited"), x1, x2, centerX, this.stepperY() + 80);
      }
   }

   /** Word-wraps a message to fit within [x1,x2] (with a small margin) instead of letting a long string overflow the panel, then draws each line centered. */
   private void renderWrappedCenteredMessage(GuiGraphics graphics, Component message, int x1, int x2, int centerX, int startY) {
      int maxWidth = Math.max(20, x2 - x1 - 8);
      List<FormattedCharSequence> lines = this.font.split(message, maxWidth);
      int y = startY;

      for (FormattedCharSequence line : lines) {
         graphics.drawCenteredString(this.font, line, centerX, y, TEXT_DIM);
         y += 10;
      }
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      int closeX2 = this.leftPos + WIDTH - PAD;
      int closeX1 = closeX2 - 16;
      int closeY1 = this.topPos + 4;
      if (mouseX >= closeX1 && mouseX < closeX2 && mouseY >= closeY1 && mouseY < closeY1 + 14) {
         this.onClose();
         return true;
      } else if (super.mouseClicked(mouseX, mouseY, button)) {
         return true;
      } else {
         int listX1 = this.leftPos + PAD;
         int listX2 = listX1 + LIST_WIDTH;
         int listY = this.topPos + HEADER_H + 4;

         for (int i = 0; i < QuestRarity.values().length; i++) {
            int rowY1 = listY + i * ROW_H;
            int rowY2 = rowY1 + ROW_H - 2;
            if (mouseX >= listX1 && mouseX < listX2 && mouseY >= rowY1 && mouseY < rowY2) {
               this.selectRarity(QuestRarity.values()[i]);
               return true;
            }
         }

         return false;
      }
   }
}
