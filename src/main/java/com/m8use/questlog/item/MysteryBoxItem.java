package com.m8use.questlog.item;

import com.m8use.questlog.quest.QuestManager;
import com.m8use.questlog.quest.QuestRarity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MysteryBoxItem extends Item {
    private final QuestRarity rarity;

    public MysteryBoxItem(QuestRarity rarity, Item.Properties properties) {
        super(properties);
        this.rarity = rarity;
    }

    public QuestRarity rarity() {
        return this.rarity;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            QuestManager.openMysteryBox(serverPlayer, this.rarity);
            stack.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}