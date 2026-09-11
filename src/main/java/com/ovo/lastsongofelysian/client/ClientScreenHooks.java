package com.ovo.lastsongofelysian.client;

import com.ovo.lastsongofelysian.screen.ShiJiBookScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientScreenHooks {

    public static void openShiJiScreen() {
        Minecraft.getInstance().setScreen(new ShiJiBookScreen());
    }
}
