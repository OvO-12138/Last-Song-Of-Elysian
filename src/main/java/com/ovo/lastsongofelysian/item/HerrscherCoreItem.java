package com.ovo.lastsongofelysian.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class HerrscherCoreItem extends Item {

    public HerrscherCoreItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return stack.copyWithCount(1);
    }
}
