// Copyright (c) 2026 Apu-Hart. All rights reserved.
package apuhart.pvzkongdao;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PvzKongdao implements ModInitializer {
    public static final String MODID = "pvzkongdao";
    public static final String PZ = "plantz";
    public static final Logger LOGGER = LoggerFactory.getLogger("pvzkongdao");

    @Override
    public void onInitialize() {
        if (!net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded(PZ)) {
            LOGGER.error("缺少前置 Plants & Zombies，本模组无法工作！");
            return;
        }

        PayloadTypeRegistry.serverboundPlay().register(Payloads.SpinRequest.ID, Payloads.SpinRequest.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(Payloads.SpinDone.ID, Payloads.SpinDone.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(Payloads.LayoutRequest.ID, Payloads.LayoutRequest.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(Payloads.WheelResult.ID, Payloads.WheelResult.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(Payloads.WheelLayout.ID, Payloads.WheelLayout.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(Payloads.SunSync.ID, Payloads.SunSync.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(Payloads.WaveSync.ID, Payloads.WaveSync.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(Payloads.WheelReward.ID, Payloads.WheelReward.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(Payloads.SpinRequest.ID, WheelManager::onSpinRequest);
        ServerPlayNetworking.registerGlobalReceiver(Payloads.SpinDone.ID, WheelManager::onSpinDone);
        ServerPlayNetworking.registerGlobalReceiver(Payloads.LayoutRequest.ID, WheelManager::onLayoutRequest);

        PvzPools.init();
        ModBlocks.initialize();
        TimeLock.register();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ServerLevel overworld = server.overworld();
            ModData data = ModData.get(overworld);
            if (!data.islandPlaced) {
                IslandPlacer.placeIsland(overworld);
            }
        });

        ServerPlayerEvents.JOIN.register(player -> {
            MinecraftServer server = player.level().getServer();
            ServerLevel overworld = server.overworld();
            ModData data = ModData.get(overworld);
            if (!data.islandPlaced) {
                IslandPlacer.placeIsland(overworld);
            }
            if (!data.giftGiven.contains(player.getUUID())) {
                IslandPlacer.giveGift(player);
                data.giftGiven.add(player.getUUID());
                data.setDirty();
            }
            syncSun(player);
            syncWave(player);
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            WaveManager.tick(server);
            WheelManager.tick(server);
            DepositEvents.autoDeposit(server);
        });
    }

    public static void syncSun(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        int sun = ModData.get(server.overworld()).getSun(player.getUUID());
        ServerPlayNetworking.send(player, new Payloads.SunSync(sun));
    }

    public static void syncWave(ServerPlayer player) {
        ModData data = ModData.get(player.level().getServer().overworld());
        ServerPlayNetworking.send(player, new Payloads.WaveSync(data.wave, data.waveActive, data.nextWaveDay));
    }
}
