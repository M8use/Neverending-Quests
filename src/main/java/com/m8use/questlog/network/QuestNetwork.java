package com.m8use.questlog.network;

import net.minecraft.server.level.ServerPlayer;

public final class QuestNetwork {
   private static QuestNetwork.Sender sender = new QuestNetwork.Sender() {
      @Override
      public void toClient(ServerPlayer player, Object message) {
      }

      @Override
      public void toServer(Object message) {
      }
   };

   public static void bind(QuestNetwork.Sender platform) {
      sender = platform;
   }

   public static void toClient(ServerPlayer player, Object message) {
      sender.toClient(player, message);
   }

   public static void toServer(Object message) {
      sender.toServer(message);
   }

   private QuestNetwork() {
   }

   public interface Sender {
      void toClient(ServerPlayer player, Object message);

      void toServer(Object message);
   }
}
