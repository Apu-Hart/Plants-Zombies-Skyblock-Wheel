// Copyright (c) 2026 Apu-Hart. All rights reserved.
package apuhart.pvzkongdao;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;

public class Rewards {
    public static final String REWARD_TAG = "pvzkongdao_reward";
    private static final int CHEST_XZ = 12;
    private static final int CHEST_Y = 32;
    private static final int GROUND_DEPTH = 320;

    private Rewards() {
    }

    public static void give(ServerPlayer player, ItemStack stack) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        if (player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) {
            Inventory inventory = player.getInventory();
            fill(inventory, inventory.getNonEquipmentItems().size(), stack);
            inventory.setChanged();
            if (stack.isEmpty()) {
                return;
            }
            if (SpawnFinder.hasFloor(level, player.blockPosition(), GROUND_DEPTH)) {
                dropAt(level, player.getX(), player.getY(), player.getZ(), stack);
                return;
            }
        }
        Container chest = nearestChest(level, player.blockPosition(), stack);
        if (chest != null) {
            fill(chest, chest.getContainerSize(), stack);
            chest.setChanged();
            if (stack.isEmpty()) {
                return;
            }
        }
        BlockPos spot = lastZombieSpot(level);
        if (spot == null) {
            spot = IslandPlacer.findSpawn(level);
        }
        dropAt(level, spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, stack);
    }

    public static boolean canHold(Inventory inventory, ItemStack stack) {
        return hasRoom(inventory, inventory.getNonEquipmentItems().size(), stack);
    }

    private static void dropAt(Level level, double x, double y, double z, ItemStack stack) {
        ItemEntity item = new ItemEntity(level, x, y, z, stack);
        item.setDefaultPickUpDelay();
        item.addTag(REWARD_TAG);
        level.addFreshEntity(item);
    }

    private static void fill(Container target, int size, ItemStack stack) {
        for (int i = 0; i < size && !stack.isEmpty(); i++) {
            ItemStack slot = target.getItem(i);
            if (slot.isEmpty()) {
                target.setItem(i, stack.split(Math.min(stack.getMaxStackSize(), stack.getCount())));
            } else if (ItemStack.isSameItemSameComponents(slot, stack)) {
                int move = Math.min(slot.getMaxStackSize() - slot.getCount(), stack.getCount());
                if (move > 0) {
                    slot.grow(move);
                    stack.shrink(move);
                }
            }
        }
    }

    private static Container nearestChest(ServerLevel level, BlockPos center, ItemStack stack) {
        Container best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-CHEST_XZ, -CHEST_Y, -CHEST_XZ), center.offset(CHEST_XZ, CHEST_Y, CHEST_XZ))) {
            if (!(level.getBlockEntity(pos) instanceof Container container) || !hasRoom(container, container.getContainerSize(), stack)) {
                continue;
            }
            double dist = center.distSqr(pos);
            if (dist < bestDist) {
                bestDist = dist;
                best = container;
            }
        }
        return best;
    }

    private static boolean hasRoom(Container container, int size, ItemStack stack) {
        for (int i = 0; i < size; i++) {
            ItemStack slot = container.getItem(i);
            if (slot.isEmpty()) {
                return true;
            }
            if (ItemStack.isSameItemSameComponents(slot, stack) && slot.getCount() < slot.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }

    private static BlockPos lastZombieSpot(ServerLevel level) {
        BlockPos spot = null;
        for (Entity entity : level.getAllEntities()) {
            if (!(entity instanceof Mob mob) || mob.isNoGravity()) {
                continue;
            }
            if (!mob.entityTags().contains(WaveManager.ZOMBIE_TAG) || !SpawnFinder.hasGround(level, mob.blockPosition())) {
                continue;
            }
            spot = mob.blockPosition();
        }
        return spot;
    }
}