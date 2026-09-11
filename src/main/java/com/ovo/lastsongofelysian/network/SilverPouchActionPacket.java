package com.ovo.lastsongofelysian.network;

import com.ovo.lastsongofelysian.item.SilverPouchItem;
import com.ovo.lastsongofelysian.registry.ModItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SilverPouchActionPacket {

    public enum Action {
        DEPOSIT_ALL,
        WITHDRAW_64,
        WITHDRAW_CUSTOM
    }

    private final Action action;
    private final long amount;

    public SilverPouchActionPacket(Action action) {
        this(action, 0L);
    }

    public SilverPouchActionPacket(Action action, long amount) {
        this.action = action;
        this.amount = amount;
    }

    public static void encode(
            SilverPouchActionPacket packet,
            FriendlyByteBuf buffer
    ) {
        buffer.writeEnum(packet.action);
        buffer.writeVarLong(packet.amount);
    }

    public static SilverPouchActionPacket decode(
            FriendlyByteBuf buffer
    ) {
        return new SilverPouchActionPacket(
                buffer.readEnum(Action.class),
                buffer.readVarLong()
        );
    }

    public static void handle(
            SilverPouchActionPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            if (player == null) {
                return;
            }

            ItemStack pouch = findPouch(player);

            if (pouch.isEmpty()) {
                return;
            }

            long balance = SilverPouchItem.getBalance(pouch);

            if (packet.action == Action.DEPOSIT_ALL) {
                depositAll(player, pouch, balance);
                return;
            }

            if (packet.action == Action.WITHDRAW_64) {
                withdrawUpTo64(player, pouch, balance);
                return;
            }

            if (packet.action == Action.WITHDRAW_CUSTOM && packet.amount > 0L) {
                withdrawCustom(player, pouch, balance, packet.amount);
            }
        });

        context.setPacketHandled(true);
    }

    private static ItemStack findPouch(ServerPlayer player) {
        for (
                int slot = 0;
                slot < player.getInventory().getContainerSize();
                slot++
        ) {
            ItemStack stack =
                    player.getInventory().getItem(slot);

            if (stack.is(ModItems.SILVER_POUCH.get())) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    private static void depositAll(
            ServerPlayer player,
            ItemStack pouch,
            long currentBalance
    ) {
        long deposited = 0L;

        for (
                int slot = 0;
                slot < player.getInventory().getContainerSize();
                slot++
        ) {
            ItemStack stack =
                    player.getInventory().getItem(slot);

            if (!stack.is(ModItems.SHINY_SILVER.get())) {
                continue;
            }

            deposited += stack.getCount();
            player.getInventory().setItem(
                    slot,
                    ItemStack.EMPTY
            );
        }

        if (deposited <= 0L) {
            return;
        }

        SilverPouchItem.setBalance(
                pouch,
                currentBalance + deposited
        );

        syncInventory(player);
    }

    private static void withdrawUpTo64(
            ServerPlayer player,
            ItemStack pouch,
            long currentBalance
    ) {
        int requested = (int) Math.min(
                64L,
                currentBalance
        );

        if (requested <= 0) {
            return;
        }

        ItemStack silver = new ItemStack(
                ModItems.SHINY_SILVER.get(),
                requested
        );

        int delivered = giveOrDrop(player, silver);

        if (delivered > 0) {
            SilverPouchItem.setBalance(
                    pouch,
                    currentBalance - delivered
            );
        }

        syncInventory(player);
    }

    private static int giveOrDrop(
            ServerPlayer player,
            ItemStack stack
    ) {
        if (stack.isEmpty()) {
            return 0;
        }

        int requested = stack.getCount();

        player.getInventory().add(stack);

        int deliveredToInventory =
                requested - stack.getCount();

        int deliveredByDrop = 0;

        if (!stack.isEmpty()) {
            ItemStack remainder = stack.copy();

            ItemEntity dropped = player.drop(
                    remainder,
                    false
            );

            if (dropped != null) {
                deliveredByDrop = remainder.getCount();
                stack.setCount(0);
            }
        }

        return deliveredToInventory + deliveredByDrop;
    }

    private static void withdrawCustom(
            ServerPlayer player,
            ItemStack pouch,
            long currentBalance,
            long requestedAmount
    ) {
        long remaining = Math.min(currentBalance, requestedAmount);
        long delivered = 0L;

        while (remaining > 0L) {
            int stackSize = (int) Math.min(
                    ModItems.SHINY_SILVER.get().getMaxStackSize(),
                    remaining
            );
            ItemStack silver = new ItemStack(ModItems.SHINY_SILVER.get(), stackSize);
            player.getInventory().add(silver);
            int inserted = stackSize - silver.getCount();

            if (inserted <= 0) break;

            delivered += inserted;
            remaining -= inserted;

            if (!silver.isEmpty()) break;
        }

        if (delivered > 0L) {
            SilverPouchItem.setBalance(pouch, currentBalance - delivered);
            syncInventory(player);
        }
    }

    private static void syncInventory(ServerPlayer player) {
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
        player.inventoryMenu.broadcastChanges();
    }
}
