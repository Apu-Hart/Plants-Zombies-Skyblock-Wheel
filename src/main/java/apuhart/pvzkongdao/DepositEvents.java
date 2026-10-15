// Copyright (c) 2026 Apu-Hart. All rights reserved.
package apuhart.pvzkongdao;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class DepositEvents {
    private static final double PICKUP_RANGE = 2.0;

    public static void autoDeposit(MinecraftServer server) {
        Item sun = PvzPools.sunItem();
        if (sun == null) {
            return;
        }
        ModData data = ModData.get(server.overworld());
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            int amount = drainSun(player.getInventory(), sun) + absorbGround(player, sun);
            if (amount > 0) {
                data.addSun(player.getUUID(), amount);
                PvzKongdao.syncSun(player);
            }
        }
    }

    private static int absorbGround(ServerPlayer player, Item sun) {
        if (!(player.level() instanceof ServerLevel level)) {
            return 0;
        }
        int total = 0;
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(PICKUP_RANGE))) {
            ItemStack stack = item.getItem();
            if (!stack.is(sun)) {
                continue;
            }
            total += stack.getCount();
            item.discard();
        }
        return total;
    }

    private static int drainSun(Inventory inventory, Item sun) {
        int total = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(sun)) {
                total += stack.getCount();
                inventory.setItem(i, ItemStack.EMPTY);
            }
        }
        if (total > 0) {
            inventory.setChanged();
        }
        return total;
    }
}