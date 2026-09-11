package com.ovo.lastsongofelysian.mixin;

import com.ovo.lastsongofelysian.lastsongofelysian;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

@Mixin(value = ItemStackHandler.class, remap = false)
public abstract class ItemStackHandlerMixin {
    private static final StackWalker LASTSONGOFELYSIAN_STACK_WALKER = StackWalker.getInstance();

    @Inject(method = "setStackInSlot", at = @At("HEAD"), cancellable = true)
    private void lastsongofelysian$protectEquippedAccessory(
            int slot,
            ItemStack replacement,
            CallbackInfo callback
    ) {
        if (!(this instanceof IDynamicStackHandler handler)) {
            return;
        }

        ItemStack current = handler.getStackInSlot(slot);
        if (lastsongofelysian$isProtected(current)
                && !ItemStack.isSameItemSameTags(current, replacement)
                && lastsongofelysian$isEnigmaticCaller()) {
            callback.cancel();
        }
    }

    @Inject(method = "extractItem", at = @At("HEAD"), cancellable = true)
    private void lastsongofelysian$protectExtractedAccessory(
            int slot,
            int amount,
            boolean simulate,
            CallbackInfoReturnable<ItemStack> callback
    ) {
        if (amount <= 0 || !(this instanceof IDynamicStackHandler handler)) {
            return;
        }

        if (lastsongofelysian$isProtected(handler.getStackInSlot(slot))
                && lastsongofelysian$isEnigmaticCaller()) {
            callback.setReturnValue(ItemStack.EMPTY);
        }
    }

    private static boolean lastsongofelysian$isProtected(ItemStack stack) {
        return !stack.isEmpty()
                && BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace()
                .equals(lastsongofelysian.MODID);
    }

    private static boolean lastsongofelysian$isEnigmaticCaller() {
        return LASTSONGOFELYSIAN_STACK_WALKER.walk(frames -> frames
                .limit(32)
                .map(StackWalker.StackFrame::getClassName)
                .anyMatch(name -> name.startsWith("com.aizistral.enigmaticlegacy.")
                        || name.startsWith("auviotre.enigmatic.addon.")));
    }
}
