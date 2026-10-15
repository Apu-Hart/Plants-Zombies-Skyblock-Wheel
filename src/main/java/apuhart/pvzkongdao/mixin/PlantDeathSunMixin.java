// Copyright (c) 2026 Apu-Hart. All rights reserved.
package apuhart.pvzkongdao.mixin;

import apuhart.pvzkongdao.ModData;
import apuhart.pvzkongdao.PvzKongdao;
import joshxviii.plantz.entity.Sun;
import joshxviii.plantz.entity.plant.Plant;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Plant.class)
public class PlantDeathSunMixin {

    @Inject(method = "die(Lnet/minecraft/world/damagesource/DamageSource;)V", at = @At("RETURN"))
    private void pvzkongdao$collectSun(DamageSource source, CallbackInfo ci) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        Plant plant = (Plant) (Object) this;
        if (!(plant.level() instanceof ServerLevel level)) {
            return;
        }
        int total = 0;
        for (Sun sun : level.getEntitiesOfClass(Sun.class, plant.getBoundingBox().inflate(2.0))) {
            total += sun.getValue();
            sun.discard();
        }
        if (total > 0) {
            ModData data = ModData.get(level.getServer().overworld());
            data.addSun(player.getUUID(), total);
            PvzKongdao.syncSun(player);
        }
    }
}