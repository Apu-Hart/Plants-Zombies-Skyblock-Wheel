// Copyright (c) 2026 Apu-Hart. All rights reserved.
package apuhart.pvzkongdao;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.LevelData;

import java.util.Optional;

public class IslandPlacer {
    private static final BlockPos ORIGIN = new BlockPos(-4, 64, -4);
    private static final int SIZE_X = 8;
    private static final int SIZE_Z = 9;

    public static void placeIsland(ServerLevel level) {
        ModData data = ModData.get(level);
        if (data.islandPlaced) {
            return;
        }
        StructureTemplateManager manager = level.getStructureManager();
        Optional<StructureTemplate> found = manager.get(
                Identifier.fromNamespaceAndPath(PvzKongdao.MODID, "kongdao"));
        if (found.isEmpty()) {
            PvzKongdao.LOGGER.error("读不到内置空岛模板，无法生成空岛");
            return;
        }
        StructureTemplate template = found.get();
        StructurePlaceSettings settings = new StructurePlaceSettings().setIgnoreEntities(true);
        template.placeInWorld(level, ORIGIN, ORIGIN, settings, level.getRandom(), 18);

        BlockPos spawn = findSpawn(level);
        level.getServer().setRespawnData(LevelData.RespawnData.of(Level.OVERWORLD, spawn, 0.0f, 0.0f));
        data.islandPlaced = true;
        data.setDirty();
        PvzKongdao.LOGGER.info("空岛已生成，出生点 {}", spawn.toShortString());
    }

    static BlockPos findSpawn(ServerLevel level) {
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (int x = 0; x < SIZE_X; x++) {
            for (int z = 0; z < SIZE_Z; z++) {
                BlockPos ground = ORIGIN.offset(x, 3, z);
                if (!level.getBlockState(ground.below()).is(Blocks.GRASS_BLOCK)) {
                    continue;
                }
                boolean clear = true;
                for (int y = 0; y <= 8; y++) {
                    if (!level.getBlockState(ground.above(y)).isAir()) {
                        clear = false;
                        break;
                    }
                }
                if (!clear) {
                    continue;
                }
                double dist = (x - 4.0) * (x - 4.0) + (z - 4.0) * (z - 4.0);
                if (dist < bestDist) {
                    bestDist = dist;
                    best = ground;
                }
            }
        }
        return best != null ? best : ORIGIN.offset(5, 3, 2);
    }

    public static void giveGift(ServerPlayer player) {
        ItemStack packets = PvzPools.seedStack(PvzPools.typeByPath("sunflower"), 2);
        Item sun = PvzPools.sunItem();
        if (packets == null || sun == null) {
            PvzKongdao.LOGGER.error("找不到 PvZ 的种子包或阳光物品，开局礼包发不了");
            return;
        }
        give(player, packets);
        give(player, new ItemStack(sun, 10));
        player.sendSystemMessage(Component.translatable("message.pvzkongdao.gift"));
    }

    private static void give(ServerPlayer player, ItemStack stack) {
        player.getInventory().add(stack);
        if (!stack.isEmpty()) {
            player.drop(stack, false);
        }
    }
}
