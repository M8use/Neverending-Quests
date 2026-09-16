package com.m8use.questlog.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Locale;
import com.m8use.questlog.quest.QuestCategory;
import com.m8use.questlog.quest.QuestManager;
import com.m8use.questlog.quest.QuestRarity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Development/testing commands for QuestLog. Every branch is gated behind {@code hasPermission(2)}
 * — the exact same permission level vanilla cheat commands like {@code /give} and {@code /gamemode}
 * require — so these only work with cheats enabled (singleplayer) or as an operator (multiplayer).
 * They're not a normal gameplay mechanic: nothing here is reachable without that permission, and
 * nothing here is advertised or required for regular play.
 */
public final class QuestModCommand {
   public static void register(RegisterCommandsEvent event) {
      register(event.getDispatcher());
   }

   private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
      dispatcher.register(
         Commands.literal("questsmod")
            .requires(source -> source.hasPermission(2))
            .then(mysteryBranch())
            .then(claimBranch())
            .then(boxesBranch())
      );
   }

   // --- /questsmod mystery complete <rarity> -------------------------------------------------------------------

   private static LiteralArgumentBuilder<CommandSourceStack> mysteryBranch() {
      LiteralArgumentBuilder<CommandSourceStack> complete = Commands.literal("complete");

      for (QuestRarity rarity : QuestRarity.values()) {
         complete.then(Commands.literal(rarity.name().toLowerCase(Locale.ROOT)).executes(context -> completeMystery(context, rarity)));
      }

      return Commands.literal("mystery").then(complete);
   }

   private static int completeMystery(CommandContext<CommandSourceStack> context, QuestRarity rarity) throws CommandSyntaxException {
      ServerPlayer player = context.getSource().getPlayerOrException();
      boolean ok = QuestManager.cheatCompleteMystery(player, rarity);
      if (ok) {
         context.getSource()
            .sendSuccess(() -> Component.literal("[QuestLog] Completed the " + displayName(rarity) + " Mystery Quest — go claim it in the QuestLog."), true);
         return 1;
      } else {
         context.getSource().sendFailure(Component.literal("[QuestLog] Couldn't find a " + displayName(rarity) + " Mystery Quest slot."));
         return 0;
      }
   }

   // --- /questsmod claim <daily|weekly|monthly> <slot> ---------------------------------------------------------

   private static LiteralArgumentBuilder<CommandSourceStack> claimBranch() {
      return Commands.literal("claim")
         .then(categoryClaimBranch("daily", QuestCategory.DAILY))
         .then(categoryClaimBranch("weekly", QuestCategory.WEEKLY))
         .then(categoryClaimBranch("monthly", QuestCategory.MONTHLY));
   }

   private static LiteralArgumentBuilder<CommandSourceStack> categoryClaimBranch(String name, QuestCategory category) {
      return Commands.literal(name)
         .then(Commands.argument("slot", IntegerArgumentType.integer(1)).executes(context -> claimSlot(context, category)));
   }

   /** The slot number is the displayed 1-based top-to-bottom position; it's force-completed and claimed in one step. */
   private static int claimSlot(CommandContext<CommandSourceStack> context, QuestCategory category) throws CommandSyntaxException {
      ServerPlayer player = context.getSource().getPlayerOrException();
      int displayedSlot = IntegerArgumentType.getInteger(context, "slot");
      boolean ok = QuestManager.cheatClaimSlot(player, category, displayedSlot - 1);
      if (ok) {
         context.getSource().sendSuccess(() -> Component.literal("[QuestLog] Claimed " + category.name() + " slot " + displayedSlot + "."), true);
         return 1;
      } else {
         context.getSource().sendFailure(Component.literal("[QuestLog] No " + category.name() + " quest in slot " + displayedSlot + "."));
         return 0;
      }
   }

   // --- /questsmod boxes <rarity> <count> ----------------------------------------------------------------------

   private static LiteralArgumentBuilder<CommandSourceStack> boxesBranch() {
      LiteralArgumentBuilder<CommandSourceStack> boxes = Commands.literal("boxes");

      for (QuestRarity rarity : QuestRarity.values()) {
         boxes.then(
            Commands.literal(rarity.name().toLowerCase(Locale.ROOT))
               .then(Commands.argument("count", IntegerArgumentType.integer(1)).executes(context -> giveBoxes(context, rarity)))
         );
      }

      return boxes;
   }

   /** Adds straight to the QuestLog Mystery Box collection — never a physical item in the normal inventory. */
   private static int giveBoxes(CommandContext<CommandSourceStack> context, QuestRarity rarity) throws CommandSyntaxException {
      ServerPlayer player = context.getSource().getPlayerOrException();
      int count = IntegerArgumentType.getInteger(context, "count");
      QuestManager.cheatGiveBoxes(player, rarity, count);
      context.getSource()
         .sendSuccess(() -> Component.literal("[QuestLog] Added " + count + " " + displayName(rarity) + " box(es) to your collection."), true);
      return count;
   }

   private static String displayName(QuestRarity rarity) {
      String name = rarity.name();
      return name.charAt(0) + name.substring(1).toLowerCase(Locale.ROOT);
   }

   private QuestModCommand() {
   }
}
