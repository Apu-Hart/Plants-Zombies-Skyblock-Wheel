// Copyright (c) 2026 Apu-Hart. All rights reserved.
package apuhart.pvzkongdao;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class WaveManager {
    public static final int MAX_WAVE = 10;
    private static final int[] WAVE_COUNTS = {3, 7, 10, 15, 24, 30, 38, 48, 55, 64};
    public static final String ZOMBIE_TAG = "pvzkongdao_zombie";
    private static final double FALL_GAP = 8.0;
    private static final double VOID_DROP = 24.0;
    private static final RandomSource RANDOM = RandomSource.create();
    private static final SoundEvent WAVE_SOUND = BuiltInRegistries.SOUND_EVENT
            .getOptional(Identifier.fromNamespaceAndPath(PvzKongdao.PZ, "entity.browncoat.ambient"))
            .orElse(SoundEvents.ZOMBIE_AMBIENT);

    public static void tick(MinecraftServer server) {
        ServerLevel level = server.overworld();
        ModData data = ModData.get(level);
        if (level.getGameTime() % 40 == 0) {
            fixStuckZombies(level);
        }
        if (data.waveActive) {
            checkWaveEnd(level, data);
            return;
        }
        if (data.wave >= MAX_WAVE) {
            return;
        }
        long triggerTime = (data.nextWaveDay - 1) * 24000L;
        if (!level.players().isEmpty() && level.getOverworldClockTime() >= triggerTime) {
            startWave(level, data, data.wave + 1);
        }
    }

    private static void startWave(ServerLevel level, ModData data, int wave) {
        data.waveActive = true;
        data.setDirty();

        Component title = Component.translatable(
                wave >= MAX_WAVE ? "message.pvzkongdao.wave_final" : "message.pvzkongdao.wave_n", waveName(wave));
        for (ServerPlayer player : level.players()) {
            if (player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
                continue;
            }
            player.connection.send(new ClientboundSetTitleTextPacket(title.copy().withStyle(ChatFormatting.RED)));
            level.playSound(null, player.getX(), player.getY(), player.getZ(), WAVE_SOUND, SoundSource.HOSTILE, 1.0f, 1.0f);
        }

        int count = WAVE_COUNTS[Math.min(wave, WAVE_COUNTS.length) - 1];
        int spawned = 0;
        for (int i = 0; i < count; i++) {
            EntityType<?> type = PvzPools.randomZombie(RANDOM);
            BlockPos pos = SpawnFinder.fixedSpot(level, RANDOM);
            if (pos == null) {
                continue;
            }
            var entity = type.create(level, EntitySpawnReason.EVENT);
            if (!(entity instanceof Mob mob)) {
                continue;
            }
            mob.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, RANDOM.nextFloat() * 360.0f, 0.0f);
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.EVENT, null);
            mob.setPersistenceRequired();
            PvzPools.seekAnywhere(mob);
            mob.addTag(ZOMBIE_TAG);
            applyWaveEffects(mob, wave);
            level.addFreshEntity(mob);
            data.waveZombies.add(mob.getUUID());
            spawned++;
        }
        PvzKongdao.LOGGER.info("第 {} 波尸潮生成 {} 只 PvZ 怪物", wave, spawned);
        syncAll(level);
        data.setDirty();
    }

    private static void syncAll(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            PvzKongdao.syncWave(player);
        }
    }

    private static void applyWaveEffects(Mob mob, int wave) {
        if (wave >= 4) {
            mob.addEffect(new MobEffectInstance(MobEffects.SPEED, MobEffectInstance.INFINITE_DURATION, 0,
                    true, false));
        }
        if (wave >= 6) {
            mob.addEffect(new MobEffectInstance(MobEffects.STRENGTH, MobEffectInstance.INFINITE_DURATION, 0,
                    true, false));
        }
        if (wave >= 8) {
            mob.addEffect(new MobEffectInstance(MobEffects.SPEED, MobEffectInstance.INFINITE_DURATION, 1,
                    true, false));
            mob.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, MobEffectInstance.INFINITE_DURATION, 0,
                    true, false));
        }
        if (wave >= MAX_WAVE) {
            mob.addEffect(new MobEffectInstance(MobEffects.STRENGTH, MobEffectInstance.INFINITE_DURATION, 1,
                    true, false));
        }
    }

    private static void checkWaveEnd(ServerLevel level, ModData data) {
        if (level.getGameTime() % 40 != 0) {
            return;
        }
        Iterator<UUID> it = data.waveZombies.iterator();
        boolean changed = false;
        while (it.hasNext()) {
            var entity = level.getEntityInAnyDimension(it.next());
            if (entity == null || !entity.isAlive() || entity.isRemoved() || !entity.entityTags().contains(ZOMBIE_TAG)) {
                it.remove();
                changed = true;
            }
        }
        if (changed) {
            data.setDirty();
        }
        if (data.waveZombies.isEmpty()) {
            data.waveActive = false;
            data.wave++;
            if (data.wave >= MAX_WAVE) {
                for (ServerPlayer player : level.players()) {
                    Rewards.give(player, new ItemStack(Items.END_PORTAL_FRAME, 12));
                    Rewards.give(player, new ItemStack(Items.ENDER_EYE, 12));
                }
            }
            data.nextWaveDay = (data.wave + 1) * 5L;
            data.setDirty();
            PvzKongdao.LOGGER.info("第 {} 波尸潮结束，下一波在第 {} 天", data.wave, data.nextWaveDay);
            syncAll(level);
        }
    }

    private static void fixStuckZombies(ServerLevel level) {
        List<Mob> zombies = new ArrayList<>();
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof Mob mob && mob.entityTags().contains(ZOMBIE_TAG)) {
                zombies.add(mob);
            }
        }
        for (Mob mob : zombies) {
            if (mob.isRemoved()) {
                continue;
            }
            LivingEntity target = mob.getTarget();
            if (target != null && Math.abs(target.getY() - mob.getY()) > FALL_GAP) {
                mob.setTarget(null);
                mob.getNavigation().stop();
            }
            if (level.getBlockState(mob.blockPosition().below()).is(BlockTags.LEAVES)) {
                BlockPos spot = SpawnFinder.findSafe(level, mob.blockPosition(), 1, RANDOM);
                if (spot != null) {
                    mob.teleportTo(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5);
                    mob.getNavigation().stop();
                }
            }
            Player ref = level.getNearestPlayer(mob, 256.0);
            if (ref == null) {
                continue;
            }
            if (!mob.isNoGravity()) {
                if (mob.getY() < ref.getY() - VOID_DROP) {
                    mob.discard();
                }
                continue;
            }
            if (mob.getY() < ref.getY() - 12.0 || mob.getY() > ref.getY() + 20.0) {
                BlockPos spot = SpawnFinder.findSafe(level, BlockPos.containing(mob.getX(), ref.getY(), mob.getZ()), 1, RANDOM);
                if (spot == null) {
                    spot = SpawnFinder.findSafe(level, ref.blockPosition(), 4, RANDOM);
                }
                if (spot != null) {
                    mob.teleportTo(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5);
                    mob.setTarget(null);
                    mob.getNavigation().stop();
                }
            }
        }
    }

    private static String waveName(int wave) {
        return switch (wave) {
            case 1 -> "一";
            case 2 -> "二";
            case 3 -> "三";
            case 4 -> "四";
            case 5 -> "五";
            case 6 -> "六";
            case 7 -> "七";
            case 8 -> "八";
            case 9 -> "九";
            default -> "十";
        };
    }
}
