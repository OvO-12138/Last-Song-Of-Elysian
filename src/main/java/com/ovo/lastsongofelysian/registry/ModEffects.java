package com.ovo.lastsongofelysian.registry;

import com.ovo.lastsongofelysian.effect.BlueInkEffect;
import com.ovo.lastsongofelysian.effect.FlawlessBloomEffect;
import com.ovo.lastsongofelysian.effect.PinkInkEffect;
import com.ovo.lastsongofelysian.effect.RedInkEffect;
import com.ovo.lastsongofelysian.effect.TuolinEffect;
import com.ovo.lastsongofelysian.lastsongofelysian;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {

    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(
                    ForgeRegistries.MOB_EFFECTS,
                    lastsongofelysian.MODID
            );

    public static final RegistryObject<MobEffect> RED_INK =
            EFFECTS.register(
                    "red_ink",
                    RedInkEffect::new
            );

    public static final RegistryObject<MobEffect> BLUE_INK =
            EFFECTS.register(
                    "blue_ink",
                    BlueInkEffect::new
            );

    public static final RegistryObject<MobEffect> LACERATION =
            EFFECTS.register(
                    "laceration",
                    () -> new MobEffect(
                            MobEffectCategory.HARMFUL,
                            0x4CAF50
                    ) {
                    }
            );

    public static final RegistryObject<MobEffect> TUOLIN =
            EFFECTS.register(
                    "tuolin",
                    TuolinEffect::new
            );

    public static final RegistryObject<MobEffect> PINK_INK =
            EFFECTS.register(
                    "pink_ink",
                    PinkInkEffect::new
            );

    public static final RegistryObject<MobEffect> FLAWLESS_BLOOM =
            EFFECTS.register(
                    "flawless_bloom",
                    FlawlessBloomEffect::new
            );
}
