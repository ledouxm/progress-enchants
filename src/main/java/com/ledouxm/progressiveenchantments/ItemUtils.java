package com.ledouxm.progressiveenchantments;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;

public class ItemUtils {
    public static CaughtTypes getItemCaughtType(ItemStack item) {
        if (item.is(Items.COD) || item.is(Items.SALMON) || item.is(Items.PUFFERFISH) || item.is(Items.TROPICAL_FISH)) {
            return CaughtTypes.FISH;
        }

        boolean isBow = item.getItem() == Items.BOW;
        boolean isFishingRod = item.getItem() == Items.FISHING_ROD;
        boolean isEnchanted = item.isEnchanted();

        if (isBow || isFishingRod) {
            if (isEnchanted) {
                return CaughtTypes.TREASURE;
            } else {
                return CaughtTypes.JUNK;
            }
        }

        if (item.is(Items.ENCHANTED_BOOK) || item.is(Items.NAME_TAG) || item.is(Items.NAUTILUS_SHELL)
                || item.is(Items.SADDLE)) {
            return CaughtTypes.TREASURE;
        }

        return CaughtTypes.JUNK;
    }

    public static boolean isTool(ItemStack itemStack) {
        Item item = itemStack.getItem();
        return itemStack.is(Items.BOW) || itemStack.is(Items.FISHING_ROD) || itemStack.is(Items.SHEARS)
                || itemStack.is(Items.FLINT_AND_STEEL)
                || item instanceof SwordItem || item instanceof AxeItem || item instanceof PickaxeItem
                || item instanceof ShovelItem || item instanceof HoeItem || item instanceof ShieldItem
                || item instanceof TridentItem || item instanceof CrossbowItem || item instanceof ArmorItem;
    }

    public enum CaughtTypes {
        JUNK, TREASURE, FISH
    }
}
