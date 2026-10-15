// Copyright (c) 2026 Apu-Hart. All rights reserved.
package apuhart.pvzkongdao.mixin;

import joshxviii.plantz.entity.plant.Plant;
import net.minecraft.advancements.criterion.TameAnimalTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TameAnimalTrigger.class)
public class TameAnimalTriggerMixin {

    @Inject(method = "trigger", at = @At("HEAD"), cancellable = true)
    private void pvzkongdao$skipPlants(ServerPlayer player, Animal animal, CallbackInfo ci) {
        if (animal instanceof Plant) {
            ci.cancel();
        }
    }
}
