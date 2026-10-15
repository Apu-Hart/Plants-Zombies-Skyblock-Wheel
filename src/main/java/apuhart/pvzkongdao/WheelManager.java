// Copyright (c) 2026 Apu-Hart. All rights reserved.
package apuhart.pvzkongdao;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class WheelManager {
    public static final int COST = 10;
    public static final int SECTORS = 20;
    public static final int WHITE = 0;
    public static final int RED = 1;
    public static final int GREEN = 2;
    public static final int YELLOW = 3;

    public static final int[] SECTOR_COLORS = {
            GREEN, RED, YELLOW, GREEN, RED, WHITE, GREEN, RED, RED, GREEN,
            WHITE, WHITE, RED, GREEN, WHITE, RED, GREEN, WHITE, RED, GREEN
    };

    private static final long SPIN_TIMEOUT_MS = 30_000;
    private static final float RED_CHANCE = 0.10f;
    private static final float YELLOW_CHANCE = 0.03f;
    private static final float GREEN_RATIO = 50.0f / 90.0f;
    private static final RandomSource RANDOM = RandomSource.create();
    private static final Map<UUID, PendingSpin> pending = new HashMap<>();
    private static final Map<UUID, EntityType<?>[]> layouts = new HashMap<>();
    private static final Set<UUID> needsRefresh = new HashSet<>();

    private record PendingSpin(int sector, long created) {
    }

    public static void onLayoutRequest(Payloads.LayoutRequest payload, net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context context) {
        ServerPlayer player = context.player();
        UUID id = player.getUUID();
        EntityType<?>[] layout = layouts.get(id);
        if (layout == null || needsRefresh.remove(id)) {
            layout = rollLayout();
            layouts.put(id, layout);
        }
        ServerPlayNetworking.send(player, new Payloads.WheelLayout(encode(layout)));
    }

    private static EntityType<?>[] rollLayout() {
        EntityType<?>[] layout = new EntityType<?>[SECTORS];
        for (int i = 0; i < SECTORS; i++) {
            if (SECTOR_COLORS[i] == RED) {
                layout[i] = PvzPools.randomZombie(RANDOM);
            } else if (SECTOR_COLORS[i] == GREEN) {
                layout[i] = PvzPools.randomPlant(RANDOM);
            }
        }
        return layout;
    }

    private static String encode(EntityType<?>[] layout) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < layout.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            if (layout[i] != null) {
                Identifier id = PvzPools.idOf(layout[i]);
                sb.append(id.getNamespace()).append(':').append(id.getPath());
            }
        }
        return sb.toString();
    }

    public static void onSpinRequest(Payloads.SpinRequest payload, net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context context) {
        ServerPlayer player = context.player();
        UUID id = player.getUUID();
        if (pending.containsKey(id)) {
            return;
        }
        ModData data = ModData.get(player.level().getServer().overworld());
        int balance = data.getSun(id);
        if (balance < COST) {
            player.sendSystemMessage(Component.translatable("message.pvzkongdao.sun_not_enough").withStyle(ChatFormatting.RED));
            return;
        }
        data.addSun(id, -COST);
        PvzKongdao.syncSun(player);

        int sector = rollSector();
        pending.put(id, new PendingSpin(sector, System.currentTimeMillis()));
        needsRefresh.add(id);
        ServerPlayNetworking.send(player, new Payloads.WheelResult(sector));
    }

    public static void onSpinDone(Payloads.SpinDone payload, net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context context) {
        grant(context.player());
    }

    public static void tick(MinecraftServer server) {
        if (pending.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        List<UUID> expired = new ArrayList<>();
        for (Map.Entry<UUID, PendingSpin> entry : pending.entrySet()) {
            if (now - entry.getValue().created() > SPIN_TIMEOUT_MS) {
                expired.add(entry.getKey());
            }
        }
        for (UUID id : expired) {
            PendingSpin spin = pending.remove(id);
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player != null && spin != null) {
                grant(player, spin.sector());
            }
        }
    }

    private static void grant(ServerPlayer player) {
        PendingSpin spin = pending.remove(player.getUUID());
        if (spin != null) {
            grant(player, spin.sector());
        }
    }

    private static int rollSector() {
        float roll = RANDOM.nextFloat();
        if (roll < RED_CHANCE) {
            return pickSector(RED);
        }
        if (roll < RED_CHANCE + YELLOW_CHANCE) {
            return pickSector(YELLOW);
        }
        return pickSector(RANDOM.nextFloat() < GREEN_RATIO ? GREEN : WHITE);
    }

    private static int pickSector(int color) {
        int sector = RANDOM.nextInt(SECTORS);
        while (SECTOR_COLORS[sector] != color) {
            sector = RANDOM.nextInt(SECTORS);
        }
        return sector;
    }

    private static void grant(ServerPlayer player, int sector) {
        EntityType<?>[] layout = layouts.get(player.getUUID());
        EntityType<?> type = layout == null ? null : layout[sector];
        switch (SECTOR_COLORS[sector]) {
            case RED -> grantZombies(player, type);
            case GREEN -> grantPlants(player, type);
            case YELLOW -> grantChest(player);
            default -> grantItem(player);
        }
    }

    private static void grantZombies(ServerPlayer player, EntityType<?> type) {
        if (!(player.level() instanceof net.minecraft.server.level.ServerLevel level)) {
            return;
        }
        EntityType<?> picked = type != null ? type : PvzPools.randomZombie(RANDOM);
        int count = 1 + RANDOM.nextInt(64);
        Identifier id = PvzPools.idOf(picked);
        int spawned = 0;
        for (int i = 0; i < count; i++) {
            BlockPos pos = SpawnFinder.fixedSpot(level, RANDOM);
            if (pos == null) {
                continue;
            }
            var entity = picked.create(level, EntitySpawnReason.EVENT);
            if (!(entity instanceof Mob mob)) {
                continue;
            }
            mob.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, RANDOM.nextFloat() * 360.0f, 0.0f);
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.EVENT, null);
            mob.setPersistenceRequired();
            PvzPools.seekAnywhere(mob);
            mob.addTag(WaveManager.ZOMBIE_TAG);
            level.addFreshEntity(mob);
            spawned++;
        }
        if (spawned > 0) {
            sendReward(player, Component.translatable("message.pvzkongdao.wheel_zombie",
                    spawned, Component.translatable("entity." + id.getNamespace() + "." + id.getPath())).withStyle(ChatFormatting.RED));
        } else {
            sendReward(player, Component.translatable("message.pvzkongdao.wheel_nothing").withStyle(ChatFormatting.GRAY));
        }
    }

    private static void grantPlants(ServerPlayer player, EntityType<?> type) {
        EntityType<?> picked = type != null ? type : PvzPools.randomPlant(RANDOM);
        Identifier id = PvzPools.idOf(picked);
        int count = 1 + RANDOM.nextInt(3);
        ItemStack stack = PvzPools.seedStack(picked, count);
        if (stack == null) {
            return;
        }
        Rewards.give(player, stack);
        sendReward(player, Component.translatable("message.pvzkongdao.wheel_plant",
                count, Component.translatable("entity." + id.getNamespace() + "." + id.getPath())).withStyle(ChatFormatting.GREEN));
    }

    private static void grantChest(ServerPlayer player) {
        Rewards.give(player, new ItemStack(ModBlocks.MYSTERY_CHEST.asItem()));
        sendReward(player, Component.translatable("message.pvzkongdao.wheel_chest").withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    private static void grantItem(ServerPlayer player) {
        Item item = VanillaPool.randomItem(RANDOM);
        ItemStack stack = new ItemStack(item, 1 + RANDOM.nextInt(64));
        if (stack.is(Items.ENCHANTED_BOOK)) {
            enchantBook(player, stack);
        }
        int count = stack.getCount();
        Component name = stack.getHoverName();
        Rewards.give(player, stack);
        sendReward(player, Component.translatable("message.pvzkongdao.wheel_item", count, name).withStyle(ChatFormatting.AQUA));
    }

    private static void enchantBook(ServerPlayer player, ItemStack stack) {
        List<Holder<Enchantment>> legal = new ArrayList<>();
        player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).listElements()
                .filter(holder -> holder.value().canEnchant(stack))
                .filter(holder -> !holder.is(Enchantments.VANISHING_CURSE) && !holder.is(Enchantments.BINDING_CURSE))
                .forEach(legal::add);
        if (legal.isEmpty()) {
            return;
        }
        Holder<Enchantment> picked = legal.get(RANDOM.nextInt(legal.size()));
        stack.enchant(picked, 1 + RANDOM.nextInt(picked.value().getMaxLevel()));
    }

    private static void sendReward(ServerPlayer player, Component message) {
        ServerPlayNetworking.send(player, new Payloads.WheelReward(message));
    }
}
