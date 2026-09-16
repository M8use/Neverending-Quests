package com.m8use.questlog.neoforge;

import com.m8use.questlog.client.ClientPacketHandler;
import com.m8use.questlog.network.ClientboundBoxUpgradedPacket;
import com.m8use.questlog.network.ClientboundMysteryBoxBulkRevealPacket;
import com.m8use.questlog.network.ClientboundMysteryBoxEarnedPacket;
import com.m8use.questlog.network.ClientboundMysteryBoxRevealPacket;
import com.m8use.questlog.network.ClientboundQuestClaimResultPacket;
import com.m8use.questlog.network.ClientboundQuestCompletedPacket;
import com.m8use.questlog.network.ClientboundQuestSyncPacket;
import com.m8use.questlog.network.QuestNetwork;
import com.m8use.questlog.network.ServerboundClaimQuestPacket;
import com.m8use.questlog.network.ServerboundOpenStoredBoxesPacket;
import com.m8use.questlog.network.ServerboundRequestQuestSyncPacket;
import com.m8use.questlog.network.ServerboundUpgradeBoxPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class NeoForgeNetwork {
   static ResourceLocation id(String path) {
      return ResourceLocation.fromNamespaceAndPath("questlog", path);
   }

   public static void register(RegisterPayloadHandlersEvent event) {
      PayloadRegistrar registrar = event.registrar("1");
      registrar.playToServer(
         RequestSync.TYPE, RequestSync.CODEC, (payload, context) -> context.enqueueWork(() -> payload.inner().handle((ServerPlayer)context.player()))
      );
      registrar.playToServer(
         ClaimQuest.TYPE, ClaimQuest.CODEC, (payload, context) -> context.enqueueWork(() -> payload.inner().handle((ServerPlayer)context.player()))
      );
      registrar.playToServer(
         OpenStoredBoxes.TYPE,
         OpenStoredBoxes.CODEC,
         (payload, context) -> context.enqueueWork(() -> payload.inner().handle((ServerPlayer)context.player()))
      );
      registrar.playToServer(
         UpgradeBox.TYPE, UpgradeBox.CODEC, (payload, context) -> context.enqueueWork(() -> payload.inner().handle((ServerPlayer)context.player()))
      );
      registrar.playToClient(Sync.TYPE, Sync.CODEC, (payload, context) -> context.enqueueWork(() -> ClientPacketHandler.handleSync(payload.inner())));
      registrar.playToClient(
         ClaimResult.TYPE, ClaimResult.CODEC, (payload, context) -> context.enqueueWork(() -> ClientPacketHandler.handleClaimResult(payload.inner()))
      );
      registrar.playToClient(
         MysteryReveal.TYPE, MysteryReveal.CODEC, (payload, context) -> context.enqueueWork(() -> ClientPacketHandler.handleMysteryReveal(payload.inner()))
      );
      registrar.playToClient(
         MysteryEarned.TYPE,
         MysteryEarned.CODEC,
         (payload, context) -> context.enqueueWork(() -> ClientPacketHandler.handleMysteryEarned(payload.inner()))
      );
      registrar.playToClient(
         MysteryBulkReveal.TYPE,
         MysteryBulkReveal.CODEC,
         (payload, context) -> context.enqueueWork(() -> ClientPacketHandler.handleMysteryBulkReveal(payload.inner()))
      );
      registrar.playToClient(
         QuestCompleted.TYPE, QuestCompleted.CODEC, (payload, context) -> context.enqueueWork(() -> ClientPacketHandler.handleQuestCompleted(payload.inner()))
      );
      registrar.playToClient(
         BoxUpgraded.TYPE, BoxUpgraded.CODEC, (payload, context) -> context.enqueueWork(() -> ClientPacketHandler.handleBoxUpgraded(payload.inner()))
      );
      QuestNetwork.bind(new QuestNetwork.Sender() {
         @Override
         public void toClient(ServerPlayer player, Object message) {
            if (message instanceof ClientboundQuestSyncPacket packet) {
               PacketDistributor.sendToPlayer(player, new Sync(packet), new CustomPacketPayload[0]);
            } else if (message instanceof ClientboundQuestClaimResultPacket packet) {
               PacketDistributor.sendToPlayer(player, new ClaimResult(packet), new CustomPacketPayload[0]);
            } else if (message instanceof ClientboundMysteryBoxRevealPacket packet) {
               PacketDistributor.sendToPlayer(player, new MysteryReveal(packet), new CustomPacketPayload[0]);
            } else if (message instanceof ClientboundMysteryBoxEarnedPacket packet) {
               PacketDistributor.sendToPlayer(player, new MysteryEarned(packet), new CustomPacketPayload[0]);
            } else if (message instanceof ClientboundMysteryBoxBulkRevealPacket packet) {
               PacketDistributor.sendToPlayer(player, new MysteryBulkReveal(packet), new CustomPacketPayload[0]);
            } else if (message instanceof ClientboundQuestCompletedPacket packet) {
               PacketDistributor.sendToPlayer(player, new QuestCompleted(packet), new CustomPacketPayload[0]);
            } else if (message instanceof ClientboundBoxUpgradedPacket packet) {
               PacketDistributor.sendToPlayer(player, new BoxUpgraded(packet), new CustomPacketPayload[0]);
            }
         }

         @Override
         public void toServer(Object message) {
            if (message instanceof ServerboundRequestQuestSyncPacket packet) {
               PacketDistributor.sendToServer(new RequestSync(packet), new CustomPacketPayload[0]);
            } else if (message instanceof ServerboundClaimQuestPacket packet) {
               PacketDistributor.sendToServer(new ClaimQuest(packet), new CustomPacketPayload[0]);
            } else if (message instanceof ServerboundOpenStoredBoxesPacket packet) {
               PacketDistributor.sendToServer(new OpenStoredBoxes(packet), new CustomPacketPayload[0]);
            } else if (message instanceof ServerboundUpgradeBoxPacket packet) {
               PacketDistributor.sendToServer(new UpgradeBox(packet), new CustomPacketPayload[0]);
            }
         }
      });
   }

   private NeoForgeNetwork() {
   }

   public static record RequestSync(ServerboundRequestQuestSyncPacket inner) implements CustomPacketPayload {
      public static final Type<RequestSync> TYPE = new Type(NeoForgeNetwork.id("request_sync"));
      public static final StreamCodec<RegistryFriendlyByteBuf, RequestSync> CODEC = StreamCodec.of(
         (buf, msg) -> msg.inner().encode(buf), buf -> new RequestSync(ServerboundRequestQuestSyncPacket.decode(buf))
      );

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public static record ClaimQuest(ServerboundClaimQuestPacket inner) implements CustomPacketPayload {
      public static final Type<ClaimQuest> TYPE = new Type(NeoForgeNetwork.id("claim_quest"));
      public static final StreamCodec<RegistryFriendlyByteBuf, ClaimQuest> CODEC = StreamCodec.of(
         (buf, msg) -> msg.inner().encode(buf), buf -> new ClaimQuest(ServerboundClaimQuestPacket.decode(buf))
      );

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public static record Sync(ClientboundQuestSyncPacket inner) implements CustomPacketPayload {
      public static final Type<Sync> TYPE = new Type(NeoForgeNetwork.id("sync"));
      public static final StreamCodec<RegistryFriendlyByteBuf, Sync> CODEC = StreamCodec.of(
         (buf, msg) -> msg.inner().encode(buf), buf -> new Sync(ClientboundQuestSyncPacket.decode(buf))
      );

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public static record ClaimResult(ClientboundQuestClaimResultPacket inner) implements CustomPacketPayload {
      public static final Type<ClaimResult> TYPE = new Type(NeoForgeNetwork.id("claim_result"));
      public static final StreamCodec<RegistryFriendlyByteBuf, ClaimResult> CODEC = StreamCodec.of(
         (buf, msg) -> msg.inner().encode(buf), buf -> new ClaimResult(ClientboundQuestClaimResultPacket.decode(buf))
      );

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public static record MysteryReveal(ClientboundMysteryBoxRevealPacket inner) implements CustomPacketPayload {
      public static final Type<MysteryReveal> TYPE = new Type(NeoForgeNetwork.id("mystery_reveal"));
      public static final StreamCodec<RegistryFriendlyByteBuf, MysteryReveal> CODEC = StreamCodec.of(
         (buf, msg) -> msg.inner().encode(buf), buf -> new MysteryReveal(ClientboundMysteryBoxRevealPacket.decode(buf))
      );

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public static record MysteryEarned(ClientboundMysteryBoxEarnedPacket inner) implements CustomPacketPayload {
      public static final Type<MysteryEarned> TYPE = new Type(NeoForgeNetwork.id("mystery_earned"));
      public static final StreamCodec<RegistryFriendlyByteBuf, MysteryEarned> CODEC = StreamCodec.of(
         (buf, msg) -> msg.inner().encode(buf), buf -> new MysteryEarned(ClientboundMysteryBoxEarnedPacket.decode(buf))
      );

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public static record MysteryBulkReveal(ClientboundMysteryBoxBulkRevealPacket inner) implements CustomPacketPayload {
      public static final Type<MysteryBulkReveal> TYPE = new Type(NeoForgeNetwork.id("mystery_bulk_reveal"));
      public static final StreamCodec<RegistryFriendlyByteBuf, MysteryBulkReveal> CODEC = StreamCodec.of(
         (buf, msg) -> msg.inner().encode(buf), buf -> new MysteryBulkReveal(ClientboundMysteryBoxBulkRevealPacket.decode(buf))
      );

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public static record OpenStoredBoxes(ServerboundOpenStoredBoxesPacket inner) implements CustomPacketPayload {
      public static final Type<OpenStoredBoxes> TYPE = new Type(NeoForgeNetwork.id("open_stored_boxes"));
      public static final StreamCodec<RegistryFriendlyByteBuf, OpenStoredBoxes> CODEC = StreamCodec.of(
         (buf, msg) -> msg.inner().encode(buf), buf -> new OpenStoredBoxes(ServerboundOpenStoredBoxesPacket.decode(buf))
      );

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public static record UpgradeBox(ServerboundUpgradeBoxPacket inner) implements CustomPacketPayload {
      public static final Type<UpgradeBox> TYPE = new Type(NeoForgeNetwork.id("upgrade_box"));
      public static final StreamCodec<RegistryFriendlyByteBuf, UpgradeBox> CODEC = StreamCodec.of(
         (buf, msg) -> msg.inner().encode(buf), buf -> new UpgradeBox(ServerboundUpgradeBoxPacket.decode(buf))
      );

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public static record QuestCompleted(ClientboundQuestCompletedPacket inner) implements CustomPacketPayload {
      public static final Type<QuestCompleted> TYPE = new Type(NeoForgeNetwork.id("quest_completed"));
      public static final StreamCodec<RegistryFriendlyByteBuf, QuestCompleted> CODEC = StreamCodec.of(
         (buf, msg) -> msg.inner().encode(buf), buf -> new QuestCompleted(ClientboundQuestCompletedPacket.decode(buf))
      );

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public static record BoxUpgraded(ClientboundBoxUpgradedPacket inner) implements CustomPacketPayload {
      public static final Type<BoxUpgraded> TYPE = new Type(NeoForgeNetwork.id("box_upgraded"));
      public static final StreamCodec<RegistryFriendlyByteBuf, BoxUpgraded> CODEC = StreamCodec.of(
         (buf, msg) -> msg.inner().encode(buf), buf -> new BoxUpgraded(ClientboundBoxUpgradedPacket.decode(buf))
      );

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }
}
