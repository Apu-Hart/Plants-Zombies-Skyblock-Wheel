// Copyright (c) 2026 Apu-Hart. All rights reserved.
package apuhart.pvzkongdao;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public class SpawnFinder {
    private static final int ATTEMPTS = 32;
    private static final int RANGE = 8;
    private static final int UP = 3;
    private static final int DOWN = 16;
    private static final int PLANT_CLEARANCE = 8;
    private static final double SPREAD = 1.0;
    private static final BlockPos FIXED_SPAWN = new BlockPos(1, 67, 1);
    private static final int FIXED_REACH = 8;

    private SpawnFinder() {
    }

    public static BlockPos fixedSpot(Level level, RandomSource random) {
        List<BlockPos> ring = new ArrayList<>();
        for (int r = 0; r <= FIXED_REACH; r++) {
            ring.clear();
            for (int x = FIXED_SPAWN.getX() - r; x <= FIXED_SPAWN.getX() + r; x++) {
                for (int z = FIXED_SPAWN.getZ() - r; z <= FIXED_SPAWN.getZ() + r; z++) {
                    if (Math.max(Math.abs(x - FIXED_SPAWN.getX()), Math.abs(z - FIXED_SPAWN.getZ())) != r) {
                        continue;
                    }
                    if (r == 0) {
                        if (standable(level, FIXED_SPAWN)) {
                            ring.add(FIXED_SPAWN);
                        }
                    } else {
                        BlockPos pos = floorIn(level, x, FIXED_SPAWN.getY(), z);
                        if (pos != null) {
                            ring.add(pos);
                        }
                    }
                }
            }
            if (!ring.isEmpty()) {
                return ring.get(random.nextInt(ring.size()));
            }
        }
        return null;
    }

    public static boolean hasFloor(Level level, BlockPos pos, int depth) {
        for (int i = 1; i <= depth; i++) {
            BlockPos below = pos.below(i);
            if (!level.getBlockState(below).getCollisionShape(level, below).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasGround(Level level, BlockPos pos) {
        for (int y = 0; y <= 3; y++) {
            if (standable(level, pos.below(y))) {
                return true;
            }
        }
        return false;
    }

    private static boolean standable(Level level, BlockPos pos) {
        if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir()) {
            return false;
        }
        BlockPos floor = pos.below();
        BlockState state = level.getBlockState(floor);
        return !state.isAir() && !state.getCollisionShape(level, floor).isEmpty() && !state.is(BlockTags.LEAVES);
    }

    public static BlockPos findSafe(Level level, BlockPos center, int minRadius, RandomSource random) {
        int reach = 2 * RANGE + PLANT_CLEARANCE;
        List<BlockPos> plantSpots = plantSpots(level, center, reach);
        BlockPos found = search(level, center, minRadius, RANGE, random, plantSpots, PLANT_CLEARANCE, RANGE);
        return found != null ? found : scan(level, center, random, plantSpots, 0, RANGE);
    }

    private static BlockPos search(Level level, BlockPos center, int minRadius, int maxRadius, RandomSource random,
                                   List<BlockPos> plantSpots, int clearance, int range) {
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double dist = minRadius + random.nextInt(maxRadius - minRadius + 1);
            int x = center.getX() + (int) Math.round(Math.cos(angle) * dist);
            int z = center.getZ() + (int) Math.round(Math.sin(angle) * dist);
            BlockPos spot = floorIn(level, x, center.getY(), z);
            if (spot != null && isOpen(level, spot, plantSpots, clearance)) {
                return spot;
            }
        }
        return scan(level, center, random, plantSpots, clearance, range);
    }

    private static BlockPos scan(Level level, BlockPos center, RandomSource random,
                                 List<BlockPos> plantSpots, int clearance, int range) {
        List<BlockPos> spots = new ArrayList<>();
        List<Double> gaps = new ArrayList<>();
        double best = 0.0;
        for (int x = center.getX() - range; x <= center.getX() + range; x++) {
            for (int z = center.getZ() - range; z <= center.getZ() + range; z++) {
                BlockPos spot = floorIn(level, x, center.getY(), z);
                if (spot == null || !isOpen(level, spot, plantSpots, clearance)) {
                    continue;
                }
                double gap = gap(plantSpots, spot);
                spots.add(spot);
                gaps.add(gap);
                if (gap > best) {
                    best = gap;
                }
            }
        }
        if (spots.isEmpty()) {
            return null;
        }
        if (plantSpots.isEmpty()) {
            return spots.get(random.nextInt(spots.size()));
        }
        double floor = Math.max(0.0, Math.sqrt(best) - SPREAD);
        floor *= floor;
        List<BlockPos> picked = new ArrayList<>();
        for (int i = 0; i < spots.size(); i++) {
            if (gaps.get(i) >= floor) {
                picked.add(spots.get(i));
            }
        }
        return picked.get(random.nextInt(picked.size()));
    }

    private static BlockPos floorIn(Level level, int x, int centerY, int z) {
        for (int y = centerY + UP; y >= centerY - DOWN; y--) {
            if (y < level.getMinY() + 1 || y > level.getMaxY() - 2) {
                continue;
            }
            BlockPos pos = new BlockPos(x, y, z);
            if (standable(level, pos)) {
                return pos;
            }
        }
        return null;
    }

    private static boolean isOpen(Level level, BlockPos pos, List<BlockPos> plantSpots, int clearance) {
        if (level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4.0, false) != null) {
            return false;
        }
        return clearance <= 0 || gap(plantSpots, pos) >= (double) clearance * clearance;
    }

    private static double gap(List<BlockPos> plantSpots, BlockPos pos) {
        double gap = Double.MAX_VALUE;
        for (BlockPos plant : plantSpots) {
            double d = pos.distSqr(plant);
            if (d < gap) {
                gap = d;
            }
        }
        return gap;
    }

    private static List<BlockPos> plantSpots(Level level, BlockPos center, int reach) {
        List<BlockPos> spots = new ArrayList<>();
        int pad = UP + DOWN;
        AABB box = new AABB(center.getX() - reach, center.getY() - pad, center.getZ() - reach,
                center.getX() + reach + 1, center.getY() + pad + 1, center.getZ() + reach + 1);
        for (Entity entity : level.getEntitiesOfClass(Entity.class, box, PvzPools::isPlant)) {
            spots.add(entity.blockPosition());
        }
        return spots;
    }
}