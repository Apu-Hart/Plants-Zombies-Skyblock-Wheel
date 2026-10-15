// Copyright (c) 2026 Apu-Hart. All rights reserved.
package apuhart.pvzkongdao.mixin;

import apuhart.pvzkongdao.ModData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerPortalMixin {

    @Unique
    private ResourceKey<Level> pvzkongdao$fromDimension;

    @Unique
    private BlockPos pvzkongdao$fromPos;

    @Inject(method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At("HEAD"))
    private void pvzkongdao$captureOrigin(TeleportTransition transition, CallbackInfoReturnable<ServerPlayer> cir) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        pvzkongdao$fromDimension = player.level().dimension();
        pvzkongdao$fromPos = player.blockPosition();
    }

    @Inject(method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At("RETURN"))
    private void pvzkongdao$handlePortalReturn(TeleportTransition transition, CallbackInfoReturnable<ServerPlayer> cir) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        ResourceKey<Level> from = pvzkongdao$fromDimension;
        ModData data = ModData.get(player.level().getServer().overworld());

        if (from == Level.OVERWORLD && transition.newLevel().dimension() == Level.NETHER) {
            data.setPortal(player.getUUID(), pvzkongdao$fromPos);
            return;
        }

        if (from == Level.NETHER && transition.newLevel().dimension() == Level.OVERWORLD) {
            BlockPos dest = data.getPortal(player.getUUID());
            if (dest != null) {
                ServerLevel overworld = player.level().getServer().overworld();
                BlockPos spot = pvzkongdao$ensurePortal(overworld, dest);
                player.teleportTo(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5);
            }
        }
    }

    @Unique
    private static BlockPos pvzkongdao$ensurePortal(ServerLevel level, BlockPos dest) {
        BlockPos found = pvzkongdao$findPortal(level, dest);
        if (found != null) {
            return found.above();
        }
        BlockPos base = pvzkongdao$findGround(level, dest);
        Direction.Axis axis = (level.getRandom().nextBoolean()) ? Direction.Axis.X : Direction.Axis.Z;
        Direction right = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;

        for (int dy = 0; dy < 5; dy++) {
            for (int dx = 0; dx < 4; dx++) {
                BlockPos frame = base.offset(right.getStepX() * dx, dy, right.getStepZ() * dx);
                boolean edge = dx == 0 || dx == 3 || dy == 0 || dy == 4;
                level.setBlockAndUpdate(frame, edge ? Blocks.OBSIDIAN.defaultBlockState()
                        : Blocks.NETHER_PORTAL.defaultBlockState()
                        .setValue(NetherPortalBlock.AXIS, axis));
            }
        }
        level.setBlockAndUpdate(base.above(5), Blocks.OBSIDIAN.defaultBlockState());
        return base.above();
    }

    @Unique
    private static BlockPos pvzkongdao$findPortal(ServerLevel level, BlockPos center) {
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-8, -4, -8), center.offset(8, 4, 8))) {
            if (level.getBlockState(pos).is(Blocks.NETHER_PORTAL)) {
                return pos.immutable();
            }
        }
        return null;
    }

    @Unique
    private static BlockPos pvzkongdao$findGround(ServerLevel level, BlockPos center) {
        for (int dy = 0; dy <= 8; dy++) {
            int y = dy % 2 == 0 ? center.getY() - dy / 2 : center.getY() + (dy + 1) / 2;
            BlockPos pos = new BlockPos(center.getX(), y, center.getZ());
            if (pos.getY() <= level.getMinY() || pos.getY() >= level.getMaxY() - 6) {
                continue;
            }
            if (!level.getBlockState(pos.below()).isAir() && level.getBlockState(pos).isAir()
                    && level.getBlockState(pos.above()).isAir()) {
                return pos;
            }
        }
        return center;
    }
}
