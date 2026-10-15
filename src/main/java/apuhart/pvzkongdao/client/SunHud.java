// Copyright (c) 2026 Apu-Hart. All rights reserved.
package apuhart.pvzkongdao.client;

import apuhart.pvzkongdao.WaveManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

public class SunHud {
    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.options.hideGui) {
            return;
        }
        int x = 4;
        int y = graphics.guiHeight() / 2 - 8;
        graphics.blit(RenderPipelines.GUI_TEXTURED, PvzKongdaoClient.SUN_ICON, x, y, 0.0f, 0.0f, 16, 16, 16, 16);
        Component text = Component.translatable("gui.pvzkongdao.sun",
                Component.literal(String.valueOf(PvzKongdaoClient.sunBalance)).withStyle(ChatFormatting.YELLOW));
        graphics.text(client.font, text, x + 19, y + 4, 0xFFFFFFFF, true);

        int wheelY = y + 22;
        var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(x, wheelY);
        pose.scale(16.0f / WheelScreen.TEXTURE_SIZE, 16.0f / WheelScreen.TEXTURE_SIZE);
        graphics.blit(RenderPipelines.GUI_TEXTURED, PvzKongdaoClient.WHEEL, 0, 0, 0.0f, 0.0f,
                WheelScreen.TEXTURE_SIZE, WheelScreen.TEXTURE_SIZE, WheelScreen.TEXTURE_SIZE, WheelScreen.TEXTURE_SIZE);
        pose.popMatrix();
        Component key = Component.translatable("gui.pvzkongdao.wheel_key",
                PvzKongdaoClient.openKeyName().copy().withStyle(ChatFormatting.GREEN));
        graphics.text(client.font, key, x + 19, wheelY + 4, 0xFFFFFFFF, true);

        Component tide = waveText();
        Component day = dayText();
        int tideX = graphics.guiWidth() - 4 - client.font.width(tide);
        int tideY = graphics.guiHeight() - 10;
        graphics.text(client.font, day, tideX - client.font.width(day) - 4, tideY, 0xFFFFD700, true);
        graphics.text(client.font, tide, tideX, tideY, waveColor(), true);
    }

    private static int waveColor() {
        if (PvzKongdaoClient.waveActive) {
            return 0xFFFF5555;
        }
        return PvzKongdaoClient.wave >= WaveManager.MAX_WAVE ? 0xFFA0A0A0 : 0xFFFFFFFF;
    }

    private static Component dayText() {
        return Component.translatable("gui.pvzkongdao.day", currentDay());
    }

    private static long currentDay() {
        Minecraft client = Minecraft.getInstance();
        long time = client.level == null ? 0L : client.level.getOverworldClockTime();
        return time / 24000L + 1L;
    }

    private static Component waveText() {
        if (PvzKongdaoClient.wave >= WaveManager.MAX_WAVE && !PvzKongdaoClient.waveActive) {
            return Component.translatable("gui.pvzkongdao.wave_done");
        }
        if (PvzKongdaoClient.waveActive) {
            return Component.translatable("gui.pvzkongdao.wave_active",
                    Math.min(PvzKongdaoClient.wave + 1, WaveManager.MAX_WAVE));
        }
        return soonText();
    }

    private static Component soonText() {
        return Component.translatable("gui.pvzkongdao.wave_soon").withStyle(ChatFormatting.GREEN)
                .append(" ")
                .append(Component.literal(String.valueOf(daysLeft())).withStyle(ChatFormatting.RED))
                .append(" ")
                .append(Component.translatable("gui.pvzkongdao.wave_soon_day").withStyle(ChatFormatting.BLUE));
    }

    private static long daysLeft() {
        return Math.max(0L, PvzKongdaoClient.nextWaveDay - currentDay());
    }
}