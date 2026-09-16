package com.m8use.questlog.client;

import java.util.List;
import com.m8use.questlog.Ids;
import com.m8use.questlog.network.ClientboundQuestClaimResultPacket;
import com.m8use.questlog.network.QuestNetwork;
import com.m8use.questlog.network.QuestSlotDto;
import com.m8use.questlog.network.ServerboundClaimQuestPacket;
import com.m8use.questlog.network.ServerboundRequestQuestSyncPacket;
import com.m8use.questlog.quest.QuestCategory;
import com.m8use.questlog.quest.QuestManager;
import com.m8use.questlog.quest.QuestRarity;
import com.m8use.questlog.quest.QuestTemplate;
import com.m8use.questlog.quest.QuestTemplates;
import com.m8use.questlog.registry.ModItems;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class QuestScreen extends Screen {
   private static final int WIDTH = 340;
   private static final int HEIGHT = 300;
   private static final int PAD = 6;
   private static final int HEADER_H = 24;
   private static final int TAB_H = 22;
   private static final int CARD_H = 36;
   private static final int CARD_GAP = 4;
   private static final int ROOT_BORDER = -13024934;
   private static final int CARD_LOCKED_TOP = 0xFF2A2C36;
   private static final int CARD_LOCKED_BOTTOM = 0xFF1D1E26;
   private static final int CARD_READY_TOP = 0xFF3A3320;
   private static final int CARD_READY_BOTTOM = 0xFF2A2415;
   private static final int CARD_CLAIMED_TOP = 0xFF1D2E22;
   private static final int CARD_CLAIMED_BOTTOM = 0xFF161F1A;
   private static final int TEXT_WHITE = -1;
   private static final int TEXT_DIM = -7892829;
   private static final int TOP_BUTTON_SIZE = 18;
   /** Full lap period, in milliseconds, for the various "moving" animations — kept slow so they read as ambient, not distracting. */
   private static final long BORDER_LOOP_MS = 4200L;
   private static final long BACKGROUND_LOOP_MS = 9000L;
   private static final long OUTLINE_LOOP_MS = 2600L;

   private int leftPos;
   private int topPos;
   private QuestCategory activeCategory = QuestCategory.DAILY;
   private final ConfettiBurst confetti = new ConfettiBurst();

   public QuestScreen() {
      super(Component.translatable("gui.questlog.title"));
   }

   @Override
   protected void init() {
      this.leftPos = (this.width - WIDTH) / 2;
      this.topPos = (this.height - HEIGHT) / 2;
      QuestNetwork.toServer(new ServerboundRequestQuestSyncPacket());
   }

   public void onClaimResult(ClientboundQuestClaimResultPacket packet) {
      Item item = Ids.item(packet.itemId());
      ItemStack stack = item == null ? ItemStack.EMPTY : new ItemStack(item, Math.max(1, packet.amount()));
      float cx = (float)(this.leftPos + WIDTH / 2);
      float cy = (float)(this.topPos + HEADER_H + TAB_H + 40);
      this.confetti.spawnConfetti(cx, cy, 32);
      if (!stack.isEmpty()) {
         this.confetti.spawnItemRain(0, 0, this.width, this.height, stack, 14, 1200L);
      }

      if (this.minecraft != null) {
         this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 1.3F, 0.5F));
      }
   }

   @Override
   public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      WheelRenderer.dimBackground(graphics, this.width, this.height, 0x60000000);
      long now = System.currentTimeMillis();
      WheelRenderer.roundedRect(graphics, this.leftPos, this.topPos, this.leftPos + WIDTH, this.topPos + HEIGHT, 6, ROOT_BORDER);
      float backgroundPhase = (float)(now % BACKGROUND_LOOP_MS) / (float)BACKGROUND_LOOP_MS;
      this.renderCategoryBackdrop(graphics, backgroundPhase);
      float borderPhase = (float)(now % BORDER_LOOP_MS) / (float)BORDER_LOOP_MS * 360.0F;
      WheelRenderer.movingOutline(
         graphics, this.leftPos - 3, this.topPos - 3, this.leftPos + WIDTH + 3, this.topPos + HEIGHT + 3, 5, 0, borderPhase, true, 70
      );
      WheelRenderer.movingOutline(graphics, this.leftPos - 1, this.topPos - 1, this.leftPos + WIDTH + 1, this.topPos + HEIGHT + 1, 2, 0, borderPhase, true);
      this.renderHeader(graphics, mouseX, mouseY);
      this.renderTabs(graphics, mouseX, mouseY);
      this.renderCards(graphics, mouseX, mouseY);
      this.confetti.render(graphics);
   }

   /** The animated panel background behind everything else, themed to whichever category tab is open. Always subtle — every card drawn on top of it is fully opaque. */
   private void renderCategoryBackdrop(GuiGraphics graphics, float phase01) {
      int x1 = this.leftPos + 1;
      int y1 = this.topPos + 1;
      int x2 = this.leftPos + WIDTH - 1;
      int y2 = this.topPos + HEIGHT - 1;
      boolean rainbow = this.activeCategory == QuestCategory.MYSTERY;
      int darkBase = switch (this.activeCategory) {
         case DAILY -> 0xFF0B1A24;
         case WEEKLY -> 0xFF17102A;
         case MONTHLY -> 0xFF241C0C;
         case MYSTERY -> 0xFF120C18;
      };
      int accent = categoryAccent(this.activeCategory);
      WheelRenderer.movingSheenBackground(graphics, x1, y1, x2, y2, 5, darkBase, accent, phase01, rainbow);
   }

   private void renderHeader(GuiGraphics graphics, int mouseX, int mouseY) {
      graphics.pose().pushPose();
      graphics.pose().translate((float)(this.leftPos + PAD + 4), (float)(this.topPos + 6), 0.0F);
      graphics.pose().scale(1.2F, 1.2F, 1.0F);
      graphics.drawString(this.font, this.getTitle(), 0, 0, TEXT_WHITE, false);
      graphics.pose().popPose();
      int closeX2 = this.leftPos + WIDTH - PAD;
      int closeX1 = closeX2 - 18;
      int closeY1 = this.topPos + 4;
      WheelRenderer.card(graphics, closeX1, closeY1, closeX2, closeY1 + 16, 4, 0xFF7A140C, 0xFFF15A4E, 0xFFD8332A);
      graphics.drawCenteredString(this.font, "X", (closeX1 + closeX2) / 2, closeY1 + 4, TEXT_WHITE);
      this.renderInventoryButton(graphics, mouseX, mouseY, closeX1);
      this.renderResetTimer(graphics);
   }

   /** A vibrant reset countdown, immediately left of the Inventory button, tied to whichever category tab is currently open. Mystery has no legitimate reset, so no timer is shown for it. */
   private void renderResetTimer(GuiGraphics graphics) {
      if (this.activeCategory != QuestCategory.MYSTERY) {
         long resetAtMillis = switch (this.activeCategory) {
            case DAILY -> QuestClientState.dailyResetEpochMillis();
            case WEEKLY -> QuestClientState.weeklyResetEpochMillis();
            case MONTHLY -> QuestClientState.monthlyResetEpochMillis();
            case MYSTERY -> 0L;
         };
         if (resetAtMillis > 0L) {
            long remainingMs = Math.max(0L, resetAtMillis - System.currentTimeMillis());
            Component label = Component.literal(formatDuration(remainingMs));
            int accent = categoryAccent(this.activeCategory);
            int closeX1 = this.leftPos + WIDTH - PAD - 18;
            int[] invBounds = this.inventoryButtonBounds(closeX1);
            int textWidth = this.font.width(label);
            int textX = invBounds[0] - 8 - textWidth;
            int textY = this.topPos + 8;
            float pulse = 0.5F + 0.5F * (float)Math.sin((double)System.currentTimeMillis() / 500.0);
            int glowAlpha = (int)(50.0F + 40.0F * pulse);
            WheelRenderer.roundedRect(graphics, textX - 4, textY - 3, textX + textWidth + 4, textY + 10, 3, glowAlpha << 24 | accent & 16777215);
            graphics.drawString(this.font, label, textX, textY, accent, true);
         }
      }
   }

   private static String formatDuration(long millis) {
      long totalSeconds = millis / 1000L;
      long days = totalSeconds / 86400L;
      long hours = totalSeconds % 86400L / 3600L;
      long minutes = totalSeconds % 3600L / 60L;
      long seconds = totalSeconds % 60L;
      if (days > 0L) {
         return days + "d " + hours + "h";
      } else if (hours > 0L) {
         return hours + "h " + minutes + "m";
      } else if (minutes > 0L) {
         return minutes + "m " + seconds + "s";
      } else {
         return seconds + "s";
      }
   }

   private int[] inventoryButtonBounds(int closeX1) {
      int x2 = closeX1 - 4;
      int x1 = x2 - TOP_BUTTON_SIZE;
      int y1 = this.topPos + 3;
      int y2 = y1 + TOP_BUTTON_SIZE;
      return new int[]{x1, y1, x2, y2};
   }

   private void renderInventoryButton(GuiGraphics graphics, int mouseX, int mouseY, int closeX1) {
      int[] bounds = this.inventoryButtonBounds(closeX1);
      int x1 = bounds[0];
      int y1 = bounds[1];
      int x2 = bounds[2];
      int y2 = bounds[3];
      int cx = (x1 + x2) / 2;
      int cy = (y1 + y2) / 2;
      float rayRotation = (float)(System.currentTimeMillis() / 30L % 360L);
      WheelRenderer.sunRays(graphics, (float)cx, (float)cy, 16.0F, rayRotation, 8, 0x50FFE9A8);
      boolean hovered = mouseX >= x1 && mouseX < x2 && mouseY >= y1 && mouseY < y2;
      int border = hovered ? 0xFFB8790A : 0xFF8A5900;
      WheelRenderer.card(graphics, x1, y1, x2, y2, 4, border, 0xFFFFD24C, 0xFFF2A31C);
      drawGiftIcon(graphics, cx, cy);
      if (QuestClientState.hasAnyClaimable()) {
         int badgeCx = x2 - 2;
         int badgeCy = y1 + 2;
         WheelRenderer.disc(graphics, (float)badgeCx, (float)badgeCy, 6.0F, 0xFFCC2A2A);
         WheelRenderer.disc(graphics, (float)badgeCx, (float)badgeCy, 6.0F, 0x55FFFFFF);
         graphics.drawCenteredString(this.font, "!", badgeCx, badgeCy - 4, TEXT_WHITE);
      }
   }

   /** Bounds of a card's info button, given the button's top-left corner (kept in sync between rendering and click handling). */
   private static int[] infoButtonBounds(int x, int y) {
      return new int[]{x, y, x + 11, y + 11};
   }

   private void renderInfoButton(GuiGraphics graphics, int x, int y, int mouseX, int mouseY) {
      int[] bounds = infoButtonBounds(x, y);
      boolean hovered = mouseX >= bounds[0] && mouseX < bounds[2] && mouseY >= bounds[1] && mouseY < bounds[3];
      int border = hovered ? 0xFFB0B0C0 : 0xFF5A5A68;
      int top = hovered ? 0xFF7A8AA0 : 0xFF4A4E5C;
      int bottom = hovered ? 0xFF5A6A80 : 0xFF32343E;
      WheelRenderer.card(graphics, bounds[0], bounds[1], bounds[2], bounds[3], 2, border, top, bottom);
      // Drawn as plain geometry (a dot + a stem) rather than the text glyph "i" — Minecraft's font
      // has asymmetric left/right spacing baked into narrow characters like "i", which threw off
      // every text-based centering approach. Plain rectangles have no such ambiguity: this is
      // exactly centered on the button regardless of font metrics.
      int cx = (bounds[0] + bounds[2]) / 2;
      int cy = (bounds[1] + bounds[3]) / 2;
      graphics.fill(cx - 1, cy - 3, cx + 1, cy - 1, TEXT_WHITE);
      graphics.fill(cx - 1, cy, cx + 1, cy + 4, TEXT_WHITE);
   }

   private static void drawGiftIcon(GuiGraphics graphics, int cx, int cy) {
      WheelRenderer.card(graphics, cx - 6, cy - 3, cx + 6, cy + 7, 1, 0xFF6B4A12, 0xFFFFFFFF, 0xFFE4E4E4);
      graphics.fill(cx - 1, cy - 3, cx + 1, cy + 7, 0xFFCB9A2E);
      graphics.fill(cx - 6, cy + 1, cx + 6, cy + 3, 0xFFCB9A2E);
      graphics.fill(cx - 3, cy - 7, cx + 3, cy - 3, 0xFFCB9A2E);
   }

   private void renderTabs(GuiGraphics graphics, int mouseX, int mouseY) {
      int tabY1 = this.topPos + HEADER_H + 2;
      int tabY2 = tabY1 + TAB_H - 4;
      int tabWidth = (WIDTH - PAD * 2) / QuestCategory.values().length;
      long now = System.currentTimeMillis();
      float outlinePhase = (float)(now % OUTLINE_LOOP_MS) / (float)OUTLINE_LOOP_MS * 360.0F;

      for (int i = 0; i < QuestCategory.values().length; i++) {
         QuestCategory category = QuestCategory.values()[i];
         int x1 = this.leftPos + PAD + i * tabWidth;
         int x2 = x1 + tabWidth - 2;
         boolean active = category == this.activeCategory;
         int accent = categoryAccent(category);
         if (category == QuestCategory.MYSTERY && active) {
            float phase = (float)(now / 6L % 360L);
            WheelRenderer.rainbowRect(graphics, x1, tabY1, x2, tabY2, 4, phase);
         } else if (active) {
            int top = WheelRenderer.lerpColor(accent, -1, 0.2F);
            WheelRenderer.card(graphics, x1, tabY1, x2, tabY2, 4, accent, top, accent);
         } else {
            int darkTop = WheelRenderer.lerpColor(accent, 0xFF000000, 0.55F);
            int darkBottom = WheelRenderer.lerpColor(accent, 0xFF000000, 0.72F);
            int darkBorder = WheelRenderer.lerpColor(accent, 0xFF000000, 0.35F);
            WheelRenderer.card(graphics, x1, tabY1, x2, tabY2, 4, darkBorder, darkTop, darkBottom);
            boolean rainbow = category == QuestCategory.MYSTERY;
            WheelRenderer.movingOutline(graphics, x1, tabY1, x2, tabY2, 2, accent, outlinePhase, rainbow);
         }

         boolean hovered = mouseX >= x1 && mouseX < x2 && mouseY >= tabY1 && mouseY < tabY2;
         if (hovered) {
            WheelRenderer.roundedRect(graphics, x1 + 1, tabY1 + 1, x2 - 1, tabY2 - 1, 3, 0x22FFFFFF);
         }

         graphics.drawCenteredString(this.font, Component.translatable(category.translationKey()), (x1 + x2) / 2, tabY1 + (TAB_H - 4 - 8) / 2, TEXT_WHITE);
      }
   }

   private static int categoryAccent(QuestCategory category) {
      return switch (category) {
         case DAILY -> 0xFF3F9BFF;
         case WEEKLY -> 0xFFB24EFF;
         case MONTHLY -> 0xFFFFB020;
         case MYSTERY -> 0xFFFF4FA3;
      };
   }

   private void renderCards(GuiGraphics graphics, int mouseX, int mouseY) {
      List<QuestSlotDto> slots = QuestClientState.forCategory(this.activeCategory);
      int cardX1 = this.leftPos + PAD;
      int cardX2 = this.leftPos + WIDTH - PAD;
      int y = this.topPos + HEADER_H + TAB_H + 6;

      for (QuestSlotDto slot : slots) {
         this.renderCard(graphics, cardX1, y, cardX2, y + CARD_H, slot, mouseX, mouseY);
         y += CARD_H + CARD_GAP;
      }
   }

   private void renderCard(GuiGraphics graphics, int x1, int y1, int x2, int y2, QuestSlotDto slot, int mouseX, int mouseY) {
      QuestTemplate template = safeTemplate(slot.templateId());
      if (template != null) {
         boolean complete = slot.progress() >= template.amount();
         boolean claimed = slot.claimed();
         int top;
         int bottom;
         int border = 0xFF14141C;
         if (claimed) {
            top = CARD_CLAIMED_TOP;
            bottom = CARD_CLAIMED_BOTTOM;
         } else if (complete) {
            top = CARD_READY_TOP;
            bottom = CARD_READY_BOTTOM;
            border = template.isMystery() ? template.rarity().color() : 0xFFFFC24E;
         } else {
            top = CARD_LOCKED_TOP;
            bottom = CARD_LOCKED_BOTTOM;
         }

         WheelRenderer.card(graphics, x1, y1, x2, y2, 4, border, top, bottom);
         if (this.activeCategory == QuestCategory.DAILY && template.rarity() != null && !claimed) {
            this.renderDailyRarityAccent(graphics, x1, y1, x2, y2, template.rarity());
         }

         if (complete && !claimed) {
            if (template.isMystery()) {
               float outlinePhase = (float)(System.currentTimeMillis() % 2200L) / 2200.0F * 360.0F;
               boolean rainbow = template.rarity() == QuestRarity.MYTHIC;
               WheelRenderer.movingOutline(graphics, x1 + 1, y1 + 1, x2 - 1, y2 - 1, 2, template.rarity().glowColor(), outlinePhase, rainbow);
            } else {
               float pulse = 0.5F + 0.5F * (float)Math.sin((double)System.currentTimeMillis() / 220.0);
               int glowAlpha = (int)(40.0F + 40.0F * pulse);
               WheelRenderer.roundedRect(graphics, x1 + 1, y1 + 1, x2 - 1, y2 - 1, 3, glowAlpha << 24 | 16764746);
            }
         }

         int iconCx = x1 + 16;
         int iconCy = (y1 + y2) / 2 - 3;
         float rayRotation = (float)(System.currentTimeMillis() / 25L % 360L);
         int rayColor = template.isMystery() ? template.rarity().glowColor() : 0xFFFFE9A8;
         WheelRenderer.sunRays(graphics, (float)iconCx, (float)iconCy, 13.0F, rayRotation, 6, 0x40000000 | rayColor & 16777215);
         ItemStack iconStack = iconFor(template);
         graphics.renderItem(iconStack, iconCx - 8, iconCy - 8);
         Component quantityText = Component.literal(quantityLabel(template));
         graphics.drawCenteredString(this.font, quantityText, iconCx, iconCy + 11, TEXT_DIM);
         int textX = x1 + 34;
         if (!template.isMystery()) {
            this.renderInfoButton(graphics, textX, y1 + 4, mouseX, mouseY);
            textX += 15;
         }

         if (template.id().equals("daily_mythic_bacon")) {
            float namePhase = (float)(System.currentTimeMillis() / 4L % 360L);
            WheelRenderer.drawRainbowText(graphics, this.font, Component.translatable(template.nameKey()).getString(), textX, y1 + 4, namePhase);
         } else {
            graphics.drawString(this.font, Component.translatable(template.nameKey()), textX, y1 + 4, TEXT_WHITE, false);
         }
         int barX1 = textX;
         int barX2 = x2 - 76;
         int barY1 = y1 + 18;
         int barY2 = barY1 + 10;
         this.renderProgressBar(graphics, barX1, barY1, barX2, barY2, slot.progress(), template.amount());
         int pillX1 = x2 - 70;
         int pillX2 = x2 - 4;
         int pillY1 = (y1 + y2) / 2 - 8;
         int pillY2 = pillY1 + 16;
         Component pillText;
         int pillOutline;
         int pillTop;
         int pillBottom;
         int pillTextColor;
         if (claimed) {
            pillText = Component.translatable("gui.questlog.claimed");
            pillOutline = 0xFF1D6B3B;
            pillTop = 0xFF6EDB8F;
            pillBottom = 0xFF34A15C;
            pillTextColor = TEXT_WHITE;
         } else if (complete) {
            pillText = Component.translatable("gui.questlog.claim");
            pillOutline = 0xFF8A5900;
            pillTop = 0xFFFFD24C;
            pillBottom = 0xFFF2A31C;
            pillTextColor = TEXT_WHITE;
         } else {
            pillText = Component.translatable("gui.questlog.in_progress");
            pillOutline = 0xFF14141C;
            pillTop = 0xFF3A3E4A;
            pillBottom = 0xFF262933;
            pillTextColor = TEXT_DIM;
         }

         WheelRenderer.card(graphics, pillX1, pillY1, pillX2, pillY2, 4, pillOutline, pillTop, pillBottom);
         graphics.drawCenteredString(this.font, pillText, (pillX1 + pillX2) / 2, pillY1 + 4, pillTextColor);
      }
   }

   /** Subtle moving gradient + glow along one edge of a Daily Quest card, colored by its rolled rarity. */
   private void renderDailyRarityAccent(GuiGraphics graphics, int x1, int y1, int x2, int y2, QuestRarity rarity) {
      long now = System.currentTimeMillis();
      float phase01 = (float)(now % BACKGROUND_LOOP_MS) / (float)BACKGROUND_LOOP_MS;
      boolean rainbow = rarity == QuestRarity.MYTHIC;
      int accent = rarity.color();
      int stripeX2 = x1 + 3;
      WheelRenderer.movingSheenBackground(graphics, x1 + 1, y1 + 1, stripeX2, y2 - 1, 1, 0, accent, phase01, rainbow);
      float glowPulse = 0.5F + 0.5F * (float)Math.sin((double)now / 400.0);
      int glowAlpha = (int)(30.0F + 25.0F * glowPulse);
      WheelRenderer.roundedRect(graphics, x1 + 1, y1 + 1, x2 - 1, y2 - 1, 3, glowAlpha << 24 | rarity.glowColor() & 16777215);
   }

   private static String quantityLabel(QuestTemplate template) {
      if (template.isMystery()) {
         return "x1";
      } else if (template.rewardMin() >= template.rewardMax()) {
         return "x" + template.rewardMin();
      } else {
         return "x" + template.rewardMin() + "-" + template.rewardMax();
      }
   }

   private void renderProgressBar(GuiGraphics graphics, int x1, int y1, int x2, int y2, int progress, int amount) {
      WheelRenderer.roundedRect(graphics, x1, y1, x2, y2, 4, 0xFF14141C);
      float fraction = amount <= 0 ? 0.0F : Math.min(1.0F, (float)progress / (float)amount);
      int filledX2 = x1 + Math.round((float)(x2 - x1) * fraction);
      if (filledX2 > x1 + 2) {
         WheelRenderer.roundedRect(graphics, x1 + 1, y1 + 1, filledX2, y2 - 1, 3, 0xFF3FCB6B, 0xFF1E9E4A);
      }

      Component text = Component.literal(progress + " / " + amount);
      graphics.drawCenteredString(this.font, text, (x1 + x2) / 2, y1 + 1, TEXT_WHITE);
   }

   private ItemStack iconFor(QuestTemplate template) {
      if (template.isMystery()) {
         return new ItemStack(ModItems.boxFor(template.rarity()).get());
      } else if (QuestManager.RANDOM_LOG_REWARD_ID.equals(template.rewardItemId())) {
         List<Item> logs = QuestManager.RANDOM_LOG_TYPES;
         int index = (int)(System.currentTimeMillis() / 500L % (long)logs.size());
         return new ItemStack(logs.get(index), template.rewardMax());
      } else if (QuestManager.RANDOM_BLOCK_REWARD_ID.equals(template.rewardItemId())) {
         List<Item> blocks = QuestManager.randomBlockPool();
         int index = (int)(System.currentTimeMillis() / 150L % (long)blocks.size());
         return new ItemStack(blocks.get(index), template.rewardMax());
      } else if (this.minecraft != null && this.minecraft.level != null) {
         return QuestManager.buildRewardStack(template.rewardItemId(), template.rewardMax(), this.minecraft.level.registryAccess());
      } else {
         Item item = Ids.item(template.rewardItemId());
         return item == null ? new ItemStack(Items.PAPER) : new ItemStack(item);
      }
   }

   private static QuestTemplate safeTemplate(String templateId) {
      try {
         return QuestTemplates.byId(templateId);
      } catch (IllegalArgumentException exception) {
         return null;
      }
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (super.mouseClicked(mouseX, mouseY, button)) {
         return true;
      } else {
         int closeX2 = this.leftPos + WIDTH - PAD;
         int closeX1 = closeX2 - 18;
         int closeY1 = this.topPos + 4;
         if (mouseX >= closeX1 && mouseX < closeX2 && mouseY >= closeY1 && mouseY < closeY1 + 16) {
            this.onClose();
            return true;
         }

         int[] invBounds = this.inventoryButtonBounds(closeX1);
         if (mouseX >= invBounds[0] && mouseX < invBounds[2] && mouseY >= invBounds[1] && mouseY < invBounds[3]) {
            if (this.minecraft != null) {
               this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 0.5F));
               this.minecraft.setScreen(new MysteryBoxInventoryScreen(this));
            }

            return true;
         }

         int tabY1 = this.topPos + HEADER_H + 2;
         int tabY2 = tabY1 + TAB_H - 4;
         int tabWidth = (WIDTH - PAD * 2) / QuestCategory.values().length;
         if (mouseY >= tabY1 && mouseY < tabY2) {
            for (int i = 0; i < QuestCategory.values().length; i++) {
               int x1 = this.leftPos + PAD + i * tabWidth;
               int x2 = x1 + tabWidth - 2;
               if (mouseX >= x1 && mouseX < x2) {
                  this.activeCategory = QuestCategory.values()[i];
                  return true;
               }
            }
         }

         List<QuestSlotDto> slots = QuestClientState.forCategory(this.activeCategory);
         int cardX1 = this.leftPos + PAD;
         int cardX2 = this.leftPos + WIDTH - PAD;
         int y = this.topPos + HEADER_H + TAB_H + 6;

         for (int i = 0; i < slots.size(); i++) {
            QuestSlotDto slot = slots.get(i);
            if (this.activeCategory != QuestCategory.MYSTERY) {
               QuestTemplate template = safeTemplate(slot.templateId());
               if (template != null) {
                  int[] infoBounds = infoButtonBounds(cardX1 + 34, y + 4);
                  if (mouseX >= infoBounds[0] && mouseX < infoBounds[2] && mouseY >= infoBounds[1] && mouseY < infoBounds[3]) {
                     if (this.minecraft != null) {
                        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F));
                        this.minecraft.setScreen(new QuestInfoScreen(this, template));
                     }

                     return true;
                  }
               }
            }

            int pillX1 = cardX2 - 70;
            int pillX2 = cardX2 - 4;
            int pillY1 = y + CARD_H / 2 - 8;
            int pillY2 = pillY1 + 16;
            if (!slot.claimed() && mouseX >= pillX1 && mouseX < pillX2 && mouseY >= pillY1 && mouseY < pillY2) {
               QuestNetwork.toServer(new ServerboundClaimQuestPacket(this.activeCategory, i));
               return true;
            }

            y += CARD_H + CARD_GAP;
         }

         return false;
      }
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }
}
