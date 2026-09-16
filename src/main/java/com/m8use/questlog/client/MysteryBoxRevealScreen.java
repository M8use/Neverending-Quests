package com.m8use.questlog.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import com.m8use.questlog.Ids;
import com.m8use.questlog.network.ClientboundMysteryBoxRevealPacket;
import com.m8use.questlog.quest.MysteryBoxLoot;
import com.m8use.questlog.quest.QuestRarity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * "Screen darkens, box appears, opens, items scroll past faster than you can read, slow down,
 * land on the winner, the winner falls into a big satisfying reveal, everything lights up." The
 * server has already decided the outcome (see {@link ClientboundMysteryBoxRevealPacket}) — this
 * only decides how it's revealed, not what it is. The reveal's color/intensity is driven entirely
 * by the WON ITEM's own rarity, not the box's rarity — a Common box landing a Rare item shows a
 * Rare-colored reveal.
 */
public final class MysteryBoxRevealScreen extends Screen {
   private static final long ANTICIPATION_MS = 450L;
   private static final long SPIN_MS = 2500L;
   private static final long FALL_MS = 500L;
   private static final long SETTLED_AT_MS = ANTICIPATION_MS + SPIN_MS + FALL_MS;
   private static final int ITEM_SLOT_WIDTH = 58;
   private static final int STRIP_LENGTH = 36;
   private static final int TARGET_INDEX = 28;

   private final QuestRarity boxRarity;
   private final QuestRarity itemRarity;
   private final ItemStack wonStack;
   private final List<StripSlot> strip;
   private final long startMs = System.currentTimeMillis();
   private final ConfettiBurst confetti = new ConfettiBurst();
   private boolean revealSoundPlayed;
   private int lastTickSlot = Integer.MIN_VALUE;

   public MysteryBoxRevealScreen(ClientboundMysteryBoxRevealPacket packet) {
      super(Component.translatable("gui.questlog.mystery_reveal.title"));
      this.boxRarity = parseRarity(packet.boxRarityId());
      this.itemRarity = parseRarity(packet.itemRarityId());
      Item item = Ids.item(packet.itemId());
      this.wonStack = item == null ? ItemStack.EMPTY : new ItemStack(item, Math.max(1, packet.amount()));
      this.strip = buildStrip(this.boxRarity, this.wonStack, this.itemRarity);
   }

   private static QuestRarity parseRarity(String name) {
      try {
         return QuestRarity.valueOf(name);
      } catch (IllegalArgumentException exception) {
         return QuestRarity.COMMON;
      }
   }

   private record StripSlot(ItemStack stack, QuestRarity rarity) {
   }

   private static List<StripSlot> buildStrip(QuestRarity boxRarity, ItemStack wonStack, QuestRarity itemRarity) {
      List<MysteryBoxLoot.PreviewEntry> preview = MysteryBoxLoot.previewEntries(boxRarity);
      ThreadLocalRandom random = ThreadLocalRandom.current();
      List<StripSlot> strip = new ArrayList<>(STRIP_LENGTH);

      for (int i = 0; i < STRIP_LENGTH; i++) {
         if (i == TARGET_INDEX) {
            ItemStack winner = !wonStack.isEmpty() ? wonStack.copy() : preview.isEmpty() ? ItemStack.EMPTY : new ItemStack(preview.get(0).item());
            strip.add(new StripSlot(winner, itemRarity));
         } else if (preview.isEmpty()) {
            strip.add(new StripSlot(ItemStack.EMPTY, QuestRarity.COMMON));
         } else {
            MysteryBoxLoot.PreviewEntry entry = preview.get(random.nextInt(preview.size()));
            int amount = entry.min() >= entry.max() ? entry.min() : entry.min() + random.nextInt(entry.max() - entry.min() + 1);
            strip.add(new StripSlot(new ItemStack(entry.item(), Math.max(1, amount)), entry.itemRarity()));
         }
      }

      return strip;
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   private long elapsed() {
      return System.currentTimeMillis() - this.startMs;
   }

   private boolean isSettled() {
      return this.elapsed() >= SETTLED_AT_MS;
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.isSettled()) {
         this.onClose();
      }

      return true;
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.isSettled()) {
         this.onClose();
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   @Override
   public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      long elapsed = this.elapsed();
      WheelRenderer.dimBackground(graphics, this.width, this.height, 0xD2080810);
      int centerX = this.width / 2;
      int centerY = this.height / 2 - 10;
      boolean anticipation = elapsed < ANTICIPATION_MS;
      boolean inFallPhase = elapsed >= ANTICIPATION_MS + SPIN_MS;
      boolean settled = elapsed >= SETTLED_AT_MS;
      float spinT = clamp01((float)(elapsed - ANTICIPATION_MS) / (float)SPIN_MS);
      float scrollOffset = anticipation ? 0.0F : easeOutQuint(spinT) * (float)(TARGET_INDEX * ITEM_SLOT_WIDTH);
      if (!anticipation && !inFallPhase) {
         this.playTickIfCrossedSlot(scrollOffset);
      }
      float fallT = clamp01((float)(elapsed - ANTICIPATION_MS - SPIN_MS) / (float)FALL_MS);
      int stripTop = centerY - 30;
      int stripBottom = centerY + 30;
      // The strip stays visible the whole time — including everything the player *didn't* get —
      // so it reads like the mid-spin view in the reference screenshot. Only the one slot the big
      // card now represents is skipped once the fall phase starts, so there's never a duplicate
      // small icon sitting underneath/behind the enlarged final card.
      this.renderStrip(graphics, centerX, centerY, scrollOffset, stripTop, stripBottom, inFallPhase ? TARGET_INDEX : -1);
      if (inFallPhase) {
         this.renderFallingWinner(graphics, centerX, centerY, easeOutBack(fallT), fallT);
      }

      graphics.pose().pushPose();
      graphics.pose().translate(0.0F, 0.0F, 300.0F);
      if (anticipation) {
         graphics.drawCenteredString(
            this.font, Component.translatable("gui.questlog.mystery_reveal.opening", Component.translatable(this.boxRarity.translationKey())), centerX, stripTop - 30, this.boxRarity.color()
         );
      } else if (!inFallPhase) {
         graphics.drawCenteredString(this.font, Component.translatable("gui.questlog.mystery_reveal.title"), centerX, stripTop - 30, -1);
      }

      if (settled) {
         if (!this.revealSoundPlayed) {
            this.revealSoundPlayed = true;
            this.onRevealed(centerX, centerY);
         }

         this.renderSettledInfo(graphics, centerX, centerY);
      }

      graphics.pose().popPose();
      this.confetti.render(graphics);
   }

   private void renderStrip(GuiGraphics graphics, int centerX, int centerY, float scrollOffset, int stripTop, int stripBottom, int hideIndex) {
      graphics.enableScissor(0, stripTop - 4, this.width, stripBottom + 4);
      WheelRenderer.roundedRect(
         graphics,
         centerX - ITEM_SLOT_WIDTH / 2 - 4,
         stripTop - 4,
         centerX + ITEM_SLOT_WIDTH / 2 + 4,
         stripBottom + 4,
         6,
         0x40000000 | this.itemRarity.glowColor() & 16777215
      );

      for (int i = 0; i < STRIP_LENGTH; i++) {
         float slotCenterX = (float)centerX - scrollOffset + (float)i * (float)ITEM_SLOT_WIDTH;
         if (i != hideIndex && slotCenterX > -ITEM_SLOT_WIDTH && slotCenterX < (float)this.width + ITEM_SLOT_WIDTH) {
            StripSlot slot = this.strip.get(i);
            if (!slot.stack().isEmpty()) {
               renderRarityCard(graphics, slotCenterX, (float)centerY, 22.0F, 26.0F, slot.rarity());
               graphics.pose().pushPose();
               graphics.pose().translate(slotCenterX, (float)centerY - 5.0F, 0.0F);
               graphics.pose().scale(1.15F, 1.15F, 1.0F);
               graphics.renderItem(slot.stack(), -8, -8);
               graphics.pose().popPose();
               Component qty = Component.literal("x" + slot.stack().getCount());
               graphics.drawCenteredString(this.font, qty, Math.round(slotCenterX), centerY + 15, -1);
            }
         }
      }

      graphics.disableScissor();
      graphics.fill(centerX - 1, stripTop - 10, centerX + 1, stripBottom + 10, 0xFFFFD24C);
   }

   /** The winner falling from above into an enlarged final position, with an ease-out-back "landing" bounce. */
   private void renderFallingWinner(GuiGraphics graphics, int centerX, int centerY, float easedFallT, float linearFallT) {
      ItemStack stack = this.strip.get(TARGET_INDEX).stack();
      if (!stack.isEmpty()) {
         float startY = (float)centerY - 130.0F;
         float endY = (float)centerY - 18.0F;
         float y = startY + (endY - startY) * easedFallT;
         float scale = 1.15F + (2.55F - 1.15F) * easeOutQuint(linearFallT);
         float cardHalfW = 22.0F + 22.0F * easeOutQuint(linearFallT);
         float cardHalfH = 26.0F + 26.0F * easeOutQuint(linearFallT);
         float pulse = 0.5F + 0.5F * (float)Math.sin((double)System.currentTimeMillis() / 200.0);
         if (linearFallT >= 0.999F) {
            float rayRotation = (float)(System.currentTimeMillis() / 22L % 360L);
            int rayColor = (int)(70.0F + 50.0F * pulse) << 24 | this.itemRarity.glowColor() & 16777215;
            WheelRenderer.sunRays(graphics, (float)centerX, y, cardHalfW + 26.0F, rayRotation, 10, rayColor);
         }

         renderRarityCard(graphics, (float)centerX, y, cardHalfW, cardHalfH, this.itemRarity);
         graphics.pose().pushPose();
         graphics.pose().translate((float)centerX, y, 0.0F);
         graphics.pose().scale(scale, scale, 1.0F);
         graphics.renderItem(stack, -8, -8);
         graphics.pose().popPose();
         // The amount stays attached to the item the whole time it's growing — not just once
         // everything settles — so it's always clear this specific number is what was won. Placed
         // inside the card near its bottom edge, tracking the card's current size as it grows.
         Component qty = Component.literal("x" + stack.getCount());
         graphics.drawCenteredString(this.font, qty, centerX, Math.round(y + cardHalfH - 12.0F), -1);
      }
   }

   private void renderSettledInfo(GuiGraphics graphics, int centerX, int centerY) {
      int textY = centerY + 108;
      graphics.drawCenteredString(this.font, this.wonStack.getHoverName(), centerX, textY, -1);
      graphics.drawCenteredString(this.font, Component.translatable(this.itemRarity.translationKey()), centerX, textY + 12, this.itemRarity.glowColor());
      graphics.drawCenteredString(this.font, Component.translatable("gui.questlog.mystery_reveal.dismiss"), centerX, this.height - 20, 0xFFB0B0B0);
   }

   /** A bright, glossy gradient card colored by {@code rarity} — Mythic gets an animated rainbow instead of a fixed color. */
   private static void renderRarityCard(GuiGraphics graphics, float cx, float cy, float halfW, float halfH, QuestRarity rarity) {
      int x1 = Math.round(cx - halfW);
      int y1 = Math.round(cy - halfH);
      int x2 = Math.round(cx + halfW);
      int y2 = Math.round(cy + halfH);
      if (rarity == QuestRarity.MYTHIC) {
         float phase = (float)(System.currentTimeMillis() / 5L % 360L);
         WheelRenderer.rainbowRect(graphics, x1, y1, x2, y2, 5, phase);
         WheelRenderer.movingOutline(graphics, x1, y1, x2, y2, 2, 0, phase, true);
      } else {
         int accent = rarity.color();
         int top = WheelRenderer.lerpColor(accent, -1, 0.3F);
         int bottom = WheelRenderer.lerpColor(accent, 0xFF000000, 0.28F);
         int border = WheelRenderer.lerpColor(accent, 0xFF000000, 0.5F);
         WheelRenderer.card(graphics, x1, y1, x2, y2, 5, border, top, bottom);
      }
   }

   private void onRevealed(int centerX, int centerY) {
      int confettiCount = switch (this.itemRarity) {
         case COMMON -> 24;
         case UNCOMMON -> 36;
         case RARE -> 55;
         case EPIC -> 75;
         case LEGENDARY -> 100;
         case MYTHIC -> 150;
      };
      this.confetti.spawnConfetti((float)centerX, (float)(centerY - 18), confettiCount);
      boolean big = this.itemRarity == QuestRarity.LEGENDARY || this.itemRarity == QuestRarity.MYTHIC;
      if (this.minecraft != null) {
         this.minecraft
            .getSoundManager()
            .play(SimpleSoundInstance.forUI(big ? SoundEvents.UI_TOAST_CHALLENGE_COMPLETE : SoundEvents.PLAYER_LEVELUP, big ? 0.9F : 1.2F, 0.6F));
      }
   }

   /**
    * Plays a short tick the moment the scroll crosses into a new slot. Ties the sound directly to
    * the actual animation position rather than a separate timer, so it automatically speeds up and
    * slows down exactly in sync with the real deceleration curve — no separate pitch/rate logic
    * needed, and it can never drift out of sync with what's on screen.
    */
   private void playTickIfCrossedSlot(float scrollOffset) {
      int currentSlot = Math.round(scrollOffset / (float)ITEM_SLOT_WIDTH);
      if (currentSlot != this.lastTickSlot) {
         this.lastTickSlot = currentSlot;
         if (this.minecraft != null) {
            // Slightly randomized pitch keeps a couple hundred milliseconds of rapid ticking (near
            // the start, before it decelerates) from sounding like a single note repeating.
            float pitch = 1.5F + ThreadLocalRandom.current().nextFloat() * 0.3F;
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), pitch, 0.5F));
         }
      }
   }

   private static float clamp01(float t) {
      return Math.max(0.0F, Math.min(1.0F, t));
   }

   private static float easeOutQuint(float t) {
      float p = 1.0F - t;
      return 1.0F - p * p * p * p * p;
   }

   /** Overshoots slightly past 1.0 before settling exactly at 1.0 — a satisfying "landing" bounce for the falling item. */
   private static float easeOutBack(float t) {
      float c1 = 1.70158F;
      float c3 = c1 + 1.0F;
      float p = t - 1.0F;
      return 1.0F + c3 * p * p * p + c1 * p * p;
   }
}
