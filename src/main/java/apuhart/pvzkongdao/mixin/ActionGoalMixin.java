// Copyright (c) 2026 Apu-Hart. All rights reserved.
package apuhart.pvzkongdao.mixin;

import joshxviii.plantz.ai.goal.ActionGoal;
import joshxviii.plantz.entity.plant.SunShroom;
import net.minecraft.world.entity.PathfinderMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ActionGoal.class)
public class ActionGoalMixin {

    @Inject(method = "tick", at = @At("RETURN"))
    private void pvzkongdao$sunShroomCooldown(CallbackInfo ci) {
        PathfinderMob mob = ((ActionGoal) (Object) this).getUsingEntity();
        if (!(mob instanceof SunShroom shroom)) {
            return;
        }
        if (shroom.getCooldown() > 400) {
            shroom.setCooldown(shroom.isBaby() ? 200 : 400);
        }
    }
}