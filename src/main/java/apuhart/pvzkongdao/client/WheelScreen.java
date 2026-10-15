// Copyright (c) 2026 Apu-Hart. All rights reserved.
package apuhart.pvzkongdao.client;

import apuhart.pvzkongdao.Payloads;
import apuhart.pvzkongdao.PvzPools;
import apuhart.pvzkongdao.WheelManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class WheelScreen extends Screen {
    public static final int TEXTURE_SIZE = 1254;
    private static final int BADGE_RADIUS = 400;
    private static final int BADGE_HIT = 52;
    private static final int DISC_HIT = 56;
    private static final int OUTER_RADIUS = 565;
    private static final long SPIN_MS = 4600;
    private static final float TURNS = 4.0f;

    private static Integer pendingSector;
    private static String layoutData = "";
    private static Component rewardText;

    private boolean awaitingResult = false;
    private boolean spinning = false;
    private boolean finished = false;
    private long spinStart;
    private float rotationBeforeSpin;
    private float totalDelta;
    private float lastRotation = 0.0f;

    public WheelScreen() {
        super(Component.translatable("gui.pvzkongdao.wheel"));
    }

    public static void onResult(int sector) {
        pendingSector = sector;
    }

    public static void onLayout(String data) {
        layoutData = data;
    }

    public static void onReward(Component text) {
        rewardText = text;
    }

    @Override
    protected void init() {
        rewardText = null;
        ClientPlayNetworking.send(Payloads.LayoutRequest.INSTANCE);
    }

    @Override
    public void tick() {
        if (spinning && System.currentTimeMillis() - spinStart > SPIN_MS) {
            spinning = false;
            finished = true;
            lastRotation = ((rotationBeforeSpin + totalDelta) % 360.0f + 360.0f) % 360.0f;
            ClientPlayNetworking.send(Payloads.SpinDone.INSTANCE);
        }
        if (!spinning && awaitingResult && pendingSector != null) {
            startSpin(pendingSector);
            pendingSector = null;
            awaitingResult = false;
        }
    }

    private void startSpin(int sector) {
        spinning = true;
        finished = false;
        rewardText = null;
        spinStart = System.currentTimeMillis();
        rotationBeforeSpin = lastRotation;
        float target = ((-(sector * 18.0f + 9.0f)) % 360.0f + 360.0f) % 360.0f;
        float delta = (target - rotationBeforeSpin % 360.0f) % 360.0f;
        if (delta < 0) {
            delta += 360.0f;
        }
        totalDelta = delta + 360.0f * TURNS;
    }

    private float currentRotation() {
        if (!spinning) {
            return lastRotation;
        }
        float elapsed = System.currentTimeMillis() - spinStart;
        float u = Math.min(elapsed / SPIN_MS, 1.0f);
        lastRotation = rotationBeforeSpin + totalDelta * easeInOutCubic(u);
        return lastRotation;
    }

    private static float easeInOutCubic(float u) {
        if (u < 0.5f) {
            return 4.0f * u * u * u;
        }
        float v = 2.0f * u - 2.0f;
        return 1.0f + v * v * v / 2.0f;
    }

    private static Identifier[] entries() {
        String[] parts = layoutData.split(",", -1);
        Identifier[] ids = new Identifier[WheelManager.SECTORS];
        for (int i = 0; i < WheelManager.SECTORS && i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                ids[i] = Identifier.tryParse(parts[i]);
            }
        }
        return ids;
    }

    private static EntityType<?> typeOf(Identifier id) {
        if (id == null) {
            return null;
        }
        return BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
    }

    private static ItemStack preview(int sector, Identifier id) {
        EntityType<?> type = typeOf(id);
        if (type == null) {
            return ItemStack.EMPTY;
        }
        if (WheelManager.SECTOR_COLORS[sector] == WheelManager.GREEN) {
            ItemStack stack = PvzPools.seedStack(type, 1);
            return stack == null ? ItemStack.EMPTY : stack;
        }
        if (WheelManager.SECTOR_COLORS[sector] == WheelManager.RED) {
            Item egg = PvzPools.spawnEgg(type);
            return egg == null ? ItemStack.EMPTY : new ItemStack(egg);
        }
        return ItemStack.EMPTY;
    }

    private static Component tip(int sector, Identifier id) {
        int color = WheelManager.SECTOR_COLORS[sector];
        if (color == WheelManager.YELLOW) {
            return Component.translatable("gui.pvzkongdao.tip_chest");
        }
        if (color == WheelManager.WHITE) {
            return Component.translatable("gui.pvzkongdao.tip_vanilla");
        }
        if (color != WheelManager.GREEN && color != WheelManager.RED) {
            return null;
        }
        EntityType<?> type = typeOf(id);
        return type == null ? null : type.getDescription();
    }

    private static float badgeAngle(int sector) {
        return (float) Math.toRadians(sector * 18.0 + 9.0);
    }

    private static float[] badgeOnScreen(int sector, float rotation, float scale, float cx, float cy) {
        float a = badgeAngle(sector);
        float lx = BADGE_RADIUS * (float) Math.sin(a);
        float ly = -BADGE_RADIUS * (float) Math.cos(a);
        float cos = (float) Math.cos((float) Math.toRadians(rotation));
        float sin = (float) Math.sin((float) Math.toRadians(rotation));
        return new float[]{cx + scale * (lx * cos - ly * sin), cy + scale * (lx * sin + ly * cos)};
    }

    private float scale() {
        int size = Math.min(this.width, this.height) - 70;
        return size / (float) TEXTURE_SIZE;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        int size = Math.min(graphics.guiWidth(), graphics.guiHeight()) - 70;
        float scale = size / (float) TEXTURE_SIZE;
        int cx = graphics.guiWidth() / 2;
        int cy = graphics.guiHeight() / 2;
        float rotation = currentRotation();
        Identifier[] ids = entries();

        var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(cx, cy);
        pose.rotate((float) Math.toRadians(rotation));
        pose.scale(scale, scale);
        graphics.blit(RenderPipelines.GUI_TEXTURED, PvzKongdaoClient.WHEEL, -TEXTURE_SIZE / 2, -TEXTURE_SIZE / 2,
                0.0f, 0.0f, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE);
        for (int i = 0; i < WheelManager.SECTORS; i++) {
            ItemStack stack = preview(i, ids[i]);
            boolean ghostEgg = stack.isEmpty() && WheelManager.SECTOR_COLORS[i] == WheelManager.RED;
            if (stack.isEmpty() && !ghostEgg) {
                continue;
            }
            float a = badgeAngle(i);
            pose.pushMatrix();
            pose.translate(BADGE_RADIUS * (float) Math.sin(a), -BADGE_RADIUS * (float) Math.cos(a));
            pose.rotate(a);
            pose.scale(4.0f, 4.0f);
            if (ghostEgg) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, PvzKongdaoClient.GHOST_EGG, -8, -8, 0.0f, 0.0f, 16, 16, 16, 16);
            } else {
                graphics.item(stack, -8, -8);
            }
            pose.popMatrix();
        }
        pose.popMatrix();

        pose.pushMatrix();
        pose.translate(cx, cy);
        pose.scale(scale, scale);
        graphics.blit(RenderPipelines.GUI_TEXTURED, PvzKongdaoClient.POINTER, -32, -151,
                0.0f, 0.0f, 64, 64, 64, 64);
        pose.popMatrix();

        Component sun = Component.translatable("gui.pvzkongdao.sun",
                Component.literal(String.valueOf(PvzKongdaoClient.sunBalance)).withStyle(ChatFormatting.YELLOW));
        graphics.text(this.font, sun, cx - this.font.width(sun) / 2, cy + size / 2 + 8, 0xFFFFFFFF, true);
        boolean warn = System.currentTimeMillis() < PvzKongdaoClient.sunWarnUntil;
        Component hint;
        if (warn) {
            hint = Component.translatable("message.pvzkongdao.sun_not_enough");
        } else if (spinning) {
            hint = Component.translatable("gui.pvzkongdao.spinning");
        } else if (awaitingResult) {
            hint = Component.translatable("gui.pvzkongdao.waiting");
        } else if (finished && rewardText != null) {
            hint = rewardText;
        } else {
            hint = Component.translatable("gui.pvzkongdao.click");
        }
        int hintColor = warn ? 0xFFFF5555 : spinning ? 0xFFFFFF55 : 0xFFFFFFFF;
        graphics.text(this.font, hint, cx - this.font.width(hint) / 2, cy + size / 2 + 22, hintColor, true);

        if (!spinning && !awaitingResult) {
            for (int i = 0; i < WheelManager.SECTORS; i++) {
                Component text = tip(i, ids[i]);
                if (text == null) {
                    continue;
                }
                float[] p = badgeOnScreen(i, rotation, scale, cx, cy);
                double dx = mouseX - p[0];
                double dy = mouseY - p[1];
                if (dx * dx + dy * dy <= (BADGE_HIT * scale) * (BADGE_HIT * scale)) {
                    graphics.setTooltipForNextFrame(this.font, text, mouseX, mouseY);
                    break;
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        double dx = event.x() - this.width / 2.0;
        double dy = event.y() - this.height / 2.0;
        double dist = Math.sqrt(dx * dx + dy * dy);
        float scale = scale();
        if (dist > OUTER_RADIUS * scale) {
            onClose();
            return true;
        }
        if (dist <= DISC_HIT * scale && !spinning && !awaitingResult) {
            if (PvzKongdaoClient.sunBalance < WheelManager.COST) {
                PvzKongdaoClient.sunWarnUntil = System.currentTimeMillis() + 2000L;
                return true;
            }
            awaitingResult = true;
            ClientPlayNetworking.send(Payloads.SpinRequest.INSTANCE);
        }
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (PvzKongdaoClient.openKeyMatches(event)) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        if (spinning && !finished) {
            finished = true;
            ClientPlayNetworking.send(Payloads.SpinDone.INSTANCE);
        }
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
