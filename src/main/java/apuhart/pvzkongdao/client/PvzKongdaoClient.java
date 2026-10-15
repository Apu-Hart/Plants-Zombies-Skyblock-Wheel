// Copyright (c) 2026 Apu-Hart. All rights reserved.
package apuhart.pvzkongdao.client;

import com.mojang.blaze3d.platform.InputConstants;
import apuhart.pvzkongdao.Payloads;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class PvzKongdaoClient implements ClientModInitializer {
    public static final Identifier SUN_ICON = Identifier.fromNamespaceAndPath("plantz", "textures/item/sun.png");
    public static final Identifier WHEEL = Identifier.fromNamespaceAndPath("pvzkongdao", "textures/gui/wheel.png");
    public static final Identifier POINTER = Identifier.fromNamespaceAndPath("pvzkongdao", "textures/gui/pointer.png");
    public static final Identifier GHOST_EGG = Identifier.fromNamespaceAndPath("pvzkongdao", "textures/gui/ghost_egg.png");
    private static final String GIFT_KEY = "message.pvzkongdao.gift";
    private static final ChatFormatting[] GIFT_COLORS = {
            ChatFormatting.RED, ChatFormatting.GOLD, ChatFormatting.YELLOW, ChatFormatting.GREEN,
            ChatFormatting.AQUA, ChatFormatting.BLUE, ChatFormatting.LIGHT_PURPLE,
    };

    public static int sunBalance = 0;
    public static long sunWarnUntil = 0L;
    public static int wave = 0;
    public static boolean waveActive = false;
    public static long nextWaveDay = 5L;
    private static KeyMapping openWheel;

    @Override
    public void onInitializeClient() {
        openWheel = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.pvzkongdao.wheel",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                KeyMapping.Category.register(Identifier.fromNamespaceAndPath("pvzkongdao", "main"))));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openWheel.consumeClick()) {
                if (client.player != null && client.screen == null) {
                    client.setScreen(new WheelScreen());
                }
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(Payloads.SunSync.ID, (payload, context) ->
                context.client().execute(() -> sunBalance = payload.sun()));

        ClientPlayNetworking.registerGlobalReceiver(Payloads.WaveSync.ID, (payload, context) ->
                context.client().execute(() -> {
                    wave = payload.wave();
                    waveActive = payload.active();
                    nextWaveDay = payload.nextWaveDay();
                }));

        ClientPlayNetworking.registerGlobalReceiver(Payloads.WheelResult.ID, (payload, context) ->
                context.client().execute(() -> WheelScreen.onResult(payload.sector())));

        ClientPlayNetworking.registerGlobalReceiver(Payloads.WheelReward.ID, (payload, context) ->
                context.client().execute(() -> WheelScreen.onReward(payload.text())));

        ClientPlayNetworking.registerGlobalReceiver(Payloads.WheelLayout.ID, (payload, context) ->
                context.client().execute(() -> WheelScreen.onLayout(payload.data())));

        ClientReceiveMessageEvents.MODIFY_GAME.register((message, overlay) -> giftRainbow(message));

        HudElementRegistry.attachElementAfter(VanillaHudElements.BOSS_BAR, Identifier.fromNamespaceAndPath("pvzkongdao", "sun_hud"),
                SunHud::render);
    }

    private static Component giftRainbow(Component message) {
        if (!(message.getContents() instanceof TranslatableContents contents) || !GIFT_KEY.equals(contents.getKey())) {
            return message;
        }
        String text = message.getString();
        MutableComponent result = Component.empty();
        for (int i = 0; i < text.length(); i += 3) {
            result.append(Component.literal(text.substring(i, Math.min(text.length(), i + 3)))
                    .withStyle(GIFT_COLORS[(i / 3) % GIFT_COLORS.length]));
        }
        return result;
    }

    public static boolean openKeyMatches(KeyEvent event) {
        return openWheel != null && openWheel.matches(event);
    }

    public static Component openKeyName() {
        return openWheel == null ? Component.literal("N") : openWheel.getTranslatedKeyMessage();
    }
}
