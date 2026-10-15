// Copyright (c) 2026 Apu-Hart. All rights reserved.
package apuhart.pvzkongdao;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public class Payloads {
    public record WheelReward(Component text) implements CustomPacketPayload {
        public static final Type<WheelReward> ID = new Type<>(Identifier.fromNamespaceAndPath(PvzKongdao.MODID, "wheel_reward"));
        public static final StreamCodec<RegistryFriendlyByteBuf, WheelReward> CODEC = StreamCodec.composite(
                ComponentSerialization.STREAM_CODEC, WheelReward::text, WheelReward::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record SpinRequest() implements CustomPacketPayload {
        public static final Type<SpinRequest> ID = new Type<>(Identifier.fromNamespaceAndPath(PvzKongdao.MODID, "spin_request"));
        public static final SpinRequest INSTANCE = new SpinRequest();
        public static final StreamCodec<RegistryFriendlyByteBuf, SpinRequest> CODEC = StreamCodec.unit(INSTANCE);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record SpinDone() implements CustomPacketPayload {
        public static final Type<SpinDone> ID = new Type<>(Identifier.fromNamespaceAndPath(PvzKongdao.MODID, "spin_done"));
        public static final SpinDone INSTANCE = new SpinDone();
        public static final StreamCodec<RegistryFriendlyByteBuf, SpinDone> CODEC = StreamCodec.unit(INSTANCE);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record LayoutRequest() implements CustomPacketPayload {
        public static final Type<LayoutRequest> ID = new Type<>(Identifier.fromNamespaceAndPath(PvzKongdao.MODID, "layout_request"));
        public static final LayoutRequest INSTANCE = new LayoutRequest();
        public static final StreamCodec<RegistryFriendlyByteBuf, LayoutRequest> CODEC = StreamCodec.unit(INSTANCE);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record WheelLayout(String data) implements CustomPacketPayload {
        public static final Type<WheelLayout> ID = new Type<>(Identifier.fromNamespaceAndPath(PvzKongdao.MODID, "wheel_layout"));
        public static final StreamCodec<RegistryFriendlyByteBuf, WheelLayout> CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, WheelLayout::data, WheelLayout::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record WheelResult(int sector) implements CustomPacketPayload {
        public static final Type<WheelResult> ID = new Type<>(Identifier.fromNamespaceAndPath(PvzKongdao.MODID, "wheel_result"));
        public static final StreamCodec<RegistryFriendlyByteBuf, WheelResult> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, WheelResult::sector, WheelResult::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record SunSync(int sun) implements CustomPacketPayload {
        public static final Type<SunSync> ID = new Type<>(Identifier.fromNamespaceAndPath(PvzKongdao.MODID, "sun_sync"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SunSync> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, SunSync::sun, SunSync::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record WaveSync(int wave, boolean active, long nextWaveDay) implements CustomPacketPayload {
        public static final Type<WaveSync> ID = new Type<>(Identifier.fromNamespaceAndPath(PvzKongdao.MODID, "wave_sync"));
        public static final StreamCodec<RegistryFriendlyByteBuf, WaveSync> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, WaveSync::wave,
                ByteBufCodecs.BOOL, WaveSync::active,
                ByteBufCodecs.VAR_LONG, WaveSync::nextWaveDay,
                WaveSync::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }
}
