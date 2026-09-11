package com.ovo.lastsongofelysian.block;

import com.ovo.lastsongofelysian.dimension.ElysianRealmBlockMutationGuard;
import com.ovo.lastsongofelysian.dimension.ModDimensions;
import com.ovo.lastsongofelysian.event.ElysianRealmExploitFixes;
import com.ovo.lastsongofelysian.registry.ModBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;

import java.util.function.Function;

public class ElysianRealmEntranceBlock extends Block {

    public static final BlockPos ENTRY_BLOCK_POS =
            new BlockPos(18, -50, 2);

    private static final Vec3 ENTRY_POSITION =
            new Vec3(18.75D, -49.0D, 2.63D);

    private static final String RETURN_X =
            "ElysianEntranceReturnX";
    private static final String RETURN_Y =
            "ElysianEntranceReturnY";
    private static final String RETURN_Z =
            "ElysianEntranceReturnZ";
    private static final String RETURN_YAW =
            "ElysianEntranceReturnYaw";
    private static final String RETURN_PITCH =
            "ElysianEntranceReturnPitch";
    private static final String HAS_RETURN =
            "ElysianEntranceHasReturn";

    public ElysianRealmEntranceBlock() {
        super(
                BlockBehaviour.Properties
                        .copy(Blocks.CRYING_OBSIDIAN)
                        .strength(5.0F, 1200.0F)
                        .sound(SoundType.AMETHYST)
                        .lightLevel(state -> 12)
        );
    }

    @Override
    public InteractionResult use(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        MinecraftServer server = serverPlayer.getServer();

        if (server == null) {
            return InteractionResult.FAIL;
        }

        if (level.dimension().equals(ModDimensions.ELYSIAN_REALM)) {
            ServerLevel overworld = server.getLevel(Level.OVERWORLD);

            if (overworld == null) {
                return InteractionResult.FAIL;
            }

            ReturnPoint returnPoint = getReturnPoint(
                    serverPlayer,
                    overworld
            );

            teleport(
                    serverPlayer,
                    overworld,
                    returnPoint.position,
                    returnPoint.yaw,
                    returnPoint.pitch
            );
        } else if (level.dimension().equals(Level.OVERWORLD)) {
            ServerLevel elysianRealm =
                    server.getLevel(ModDimensions.ELYSIAN_REALM);

            if (elysianRealm == null) {
                return InteractionResult.FAIL;
            }

            saveReturnPoint(serverPlayer);
            ensureEntrance(elysianRealm);

            teleport(
                    serverPlayer,
                    elysianRealm,
                    ENTRY_POSITION,
                    serverPlayer.getYRot(),
                    serverPlayer.getXRot()
            );
        } else {
            return InteractionResult.PASS;
        }

        level.playSound(
                null,
                pos,
                SoundEvents.PORTAL_TRAVEL,
                SoundSource.BLOCKS,
                0.45F,
                1.15F
        );

        return InteractionResult.CONSUME;
    }

    @Override
    public void animateTick(
            BlockState state,
            Level level,
            BlockPos pos,
            RandomSource random
    ) {
        if (random.nextInt(3) != 0) {
            return;
        }

        double x = pos.getX() + 0.15D
                + random.nextDouble() * 0.7D;
        double y = pos.getY() + 0.9D
                + random.nextDouble() * 0.35D;
        double z = pos.getZ() + 0.15D
                + random.nextDouble() * 0.7D;

        level.addParticle(
                random.nextBoolean()
                        ? ParticleTypes.PORTAL
                        : ParticleTypes.CHERRY_LEAVES,
                x,
                y,
                z,
                0.0D,
                0.015D,
                0.0D
        );
    }

    @Override
    public int getLightEmission(
            BlockState state,
            BlockGetter level,
            BlockPos pos
    ) {
        return 12;
    }

    public static void ensureEntrance(ServerLevel level) {
        if (
                !level.dimension().equals(
                        ModDimensions.ELYSIAN_REALM
                ) || level.getBlockState(ENTRY_BLOCK_POS)
                        .is(ModBlock.ELYSIAN_REALM_ENTRANCE.get())
        ) {
            return;
        }

        ElysianRealmBlockMutationGuard.runWithBypass(
                () -> level.setBlockAndUpdate(
                        ENTRY_BLOCK_POS,
                        ModBlock.ELYSIAN_REALM_ENTRANCE
                                .get()
                                .defaultBlockState()
                )
        );
    }

    private static void saveReturnPoint(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        data.putDouble(RETURN_X, player.getX());
        data.putDouble(RETURN_Y, player.getY());
        data.putDouble(RETURN_Z, player.getZ());
        data.putFloat(RETURN_YAW, player.getYRot());
        data.putFloat(RETURN_PITCH, player.getXRot());
        data.putBoolean(HAS_RETURN, true);
    }

    private static ReturnPoint getReturnPoint(
            ServerPlayer player,
            ServerLevel overworld
    ) {
        CompoundTag data = player.getPersistentData();

        if (data.getBoolean(HAS_RETURN)) {
            return new ReturnPoint(
                    new Vec3(
                            data.getDouble(RETURN_X),
                            data.getDouble(RETURN_Y),
                            data.getDouble(RETURN_Z)
                    ),
                    data.getFloat(RETURN_YAW),
                    data.getFloat(RETURN_PITCH)
            );
        }

        BlockPos spawn = overworld.getSharedSpawnPos();

        return new ReturnPoint(
                Vec3.atBottomCenterOf(spawn.above()),
                overworld.getSharedSpawnAngle(),
                0.0F
        );
    }

    private static void teleport(
            ServerPlayer player,
            ServerLevel destination,
            Vec3 position,
            float yaw,
            float pitch
    ) {
        ElysianRealmExploitFixes.runEntranceTravel(
                () -> player.changeDimension(
                        destination,
                        new ITeleporter() {
                            @Override
                            public PortalInfo getPortalInfo(
                                    Entity entity,
                                    ServerLevel target,
                                    Function<ServerLevel, PortalInfo> fallback
                            ) {
                                return new PortalInfo(
                                        position,
                                        Vec3.ZERO,
                                        yaw,
                                        pitch
                                );
                            }

                            @Override
                            public boolean playTeleportSound(
                                    ServerPlayer serverPlayer,
                                    ServerLevel source,
                                    ServerLevel target
                            ) {
                                return false;
                            }
                        }
                )
        );
    }

    private record ReturnPoint(
            Vec3 position,
            float yaw,
            float pitch
    ) {
    }
}
