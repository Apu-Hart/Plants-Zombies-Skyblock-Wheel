// Copyright (c) 2026 Apu-Hart. All rights reserved.
package apuhart.pvzkongdao.mixin;

import joshxviii.plantz.entity.plant.Plant;
import joshxviii.plantz.entity.zombie.PazZombie;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PazZombie.class)
public abstract class PazZombieMixin extends Mob {

    protected PazZombieMixin(EntityType<? extends Mob> type, Level level) {
        super(type, level);
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void pvzkongdao$targetPlants(CallbackInfo ci) {
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Plant.class, true));
    }
}