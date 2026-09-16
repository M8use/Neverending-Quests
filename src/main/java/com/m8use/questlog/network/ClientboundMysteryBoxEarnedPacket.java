package com.m8use.questlog.network;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Sent right after a Mystery Quest is claimed. The box has already been added to the player's
 * QuestLog Mystery Box collection by the time this arrives — this only triggers the client-side
 * "Save it, or open it right now?" prompt. Choosing "Open Now" simply sends a
 * {@link ServerboundOpenStoredBoxesPacket} for one box of this rarity; ignoring the prompt leaves
 * the box safely stored, which is exactly the "don't force the player to open it" behavior.
 */
public record ClientboundMysteryBoxEarnedPacket(String rarityId) {
   public void encode(FriendlyByteBuf buf) {
      buf.writeUtf(this.rarityId, 32);
   }

   public static ClientboundMysteryBoxEarnedPacket decode(FriendlyByteBuf buf) {
      return new ClientboundMysteryBoxEarnedPacket(buf.readUtf(32));
   }
}
