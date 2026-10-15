// Copyright (c) 2026 Apu-Hart. All rights reserved.
package apuhart.pvzkongdao;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ModData extends SavedData {
    public boolean islandPlaced = false;
    public final Set<UUID> giftGiven = new HashSet<>();
    public int wave = 0;
    public boolean waveActive = false;
    public long nextWaveDay = 5;
    public final Set<UUID> waveZombies = new HashSet<>();

    private final Map<UUID, Integer> sunBalance = new HashMap<>();
    private final Map<UUID, BlockPos> portalReturn = new HashMap<>();

    public static final Codec<ModData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("island_placed").forGetter(d -> d.islandPlaced),
            Codec.BOOL.fieldOf("wave_active").forGetter(d -> d.waveActive),
            Codec.INT.fieldOf("wave").forGetter(d -> d.wave),
            Codec.LONG.fieldOf("next_wave_day").forGetter(d -> d.nextWaveDay),
            UUIDUtil.CODEC_SET.fieldOf("gift_given").forGetter(d -> d.giftGiven),
            UUIDUtil.CODEC_SET.fieldOf("wave_zombies").forGetter(d -> d.waveZombies),
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.INT).fieldOf("sun_balance").forGetter(d -> d.sunBalance),
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, BlockPos.CODEC).fieldOf("portal_return").forGetter(d -> d.portalReturn)
    ).apply(instance, ModData::new));

    public static final SavedDataType<ModData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(PvzKongdao.MODID, "data"),
            ModData::new,
            CODEC,
            net.minecraft.util.datafix.DataFixTypes.SAVED_DATA_RAIDS);

    public ModData() {
    }

    private ModData(boolean islandPlaced, boolean waveActive, int wave, long nextWaveDay,
                    Set<UUID> giftGiven, Set<UUID> waveZombies,
                    Map<UUID, Integer> sunBalance, Map<UUID, BlockPos> portalReturn) {
        this.islandPlaced = islandPlaced;
        this.waveActive = waveActive;
        this.wave = wave;
        this.nextWaveDay = nextWaveDay;
        this.giftGiven.addAll(giftGiven);
        this.waveZombies.addAll(waveZombies);
        this.sunBalance.putAll(sunBalance);
        this.portalReturn.putAll(portalReturn);
    }

    public static ModData get(ServerLevel overworld) {
        return overworld.getDataStorage().computeIfAbsent(TYPE);
    }

    public int getSun(UUID id) {
        return sunBalance.getOrDefault(id, 0);
    }

    public void setSun(UUID id, int value) {
        sunBalance.put(id, Math.max(0, value));
        setDirty();
    }

    public void addSun(UUID id, int delta) {
        setSun(id, getSun(id) + delta);
    }

    public BlockPos getPortal(UUID id) {
        return portalReturn.get(id);
    }

    public void setPortal(UUID id, BlockPos pos) {
        portalReturn.put(id, pos);
        setDirty();
    }
}
