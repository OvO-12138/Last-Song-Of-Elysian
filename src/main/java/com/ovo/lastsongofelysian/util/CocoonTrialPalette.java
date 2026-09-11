package com.ovo.lastsongofelysian.util;

import net.minecraft.ChatFormatting;

public final class CocoonTrialPalette {

    private static final ChatFormatting[] COLORS = {
            ChatFormatting.BLUE,
            ChatFormatting.LIGHT_PURPLE,
            ChatFormatting.LIGHT_PURPLE,
            ChatFormatting.GREEN,
            ChatFormatting.AQUA,
            ChatFormatting.DARK_GREEN,
            ChatFormatting.RED,
            ChatFormatting.YELLOW,
            ChatFormatting.GOLD,
            ChatFormatting.YELLOW,
            ChatFormatting.GOLD,
            ChatFormatting.LIGHT_PURPLE
    };

    private CocoonTrialPalette() {
    }

    public static ChatFormatting color(int trial) {
        if (trial < 0 || trial >= COLORS.length) {
            return ChatFormatting.LIGHT_PURPLE;
        }
        return COLORS[trial];
    }
}
