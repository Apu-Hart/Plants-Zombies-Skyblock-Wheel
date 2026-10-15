// Copyright (c) 2026 Apu-Hart. All rights reserved.
package apuhart.pvzkongdao.mixin;

import apuhart.pvzkongdao.ModData;
import apuhart.pvzkongdao.PvzKongdao;
import joshxviii.plantz.ai.goal.GenerateSunGoal;
import joshxviii.plantz.entity.plant.SunShroom;
import joshxviii.plantz.entity.plant.Sunflower;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GenerateSunGoal.class)
public class GenerateSunGoalMixin {

    @ModifyArg(method = "<init>(Ljoshxviii/plantz/entity/plant/Plant;IILkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function0;Ljava/util/function/Predicate;ILkotlin/jvm/functions/Function0;IZ)V",
            at = @At(value = "INVOKE", target = "Lkotlin/ranges/IntRange;<init>(II)V"),
            index = 0)
    private static int pvzkongdao$minVariation(int min) {
        return 0;
    }

    @ModifyArg(method = "<init>(Ljoshxviii/plantz/entity/plant/Plant;IILkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function0;Ljava/util/function/Predicate;ILkotlin/jvm/functions/Function0;IZ)V",
            at = @At(value = "INVOKE", target = "Lkotlin/ranges/IntRange;<init>(II)V"),
            index = 1)
    private static int pvzkongdao$maxVariation(int max) {
        return 0;
    }

    @Inject(method = "doAction", at = @At("HEAD"), cancellable = true)
    private void pvzkongdao$directSun(CallbackInfoReturnable<Boolean> cir) {
        PathfinderMob mob = ((GenerateSunGoal) (Object) this).getUsingEntity();
        if (!(mob instanceof Sunflower) && !(mob instanceof SunShroom)) {
            return;
        }
        if (!(mob.level() instanceof ServerLevel level)) {
            return;
        }
        LivingEntity owner = ((TamableAnimal) mob).getOwner();
        if (!(owner instanceof ServerPlayer player)) {
            return;
        }
        int amount = mob instanceof SunShroom shroom && !shroom.isBaby() ? 2 : 1;
        ModData data = ModData.get(level.getServer().overworld());
        data.addSun(player.getUUID(), amount);
        PvzKongdao.syncSun(player);
        mob.playSound(SoundEvents.CHICKEN_EGG, 1.0f, 0.5f);
        cir.setReturnValue(true);
    }
}