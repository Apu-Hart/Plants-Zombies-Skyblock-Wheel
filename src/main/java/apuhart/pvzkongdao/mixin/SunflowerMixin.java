// Copyright (c) 2026 Apu-Hart. All rights reserved.
package apuhart.pvzkongdao.mixin;

import joshxviii.plantz.entity.plant.Sunflower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Sunflower.class)
public class SunflowerMixin {

    @ModifyArg(method = "registerGoals",
            at = @At(value = "INVOKE", target = "Ljoshxviii/plantz/ai/goal/GenerateSunGoal;<init>(Ljoshxviii/plantz/entity/plant/Plant;IILkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function0;Ljava/util/function/Predicate;ILkotlin/jvm/functions/Function0;IZILkotlin/jvm/internal/DefaultConstructorMarker;)V"),
            index = 1)
    private static int pvzkongdao$cooldownTime(int cooldownTime) {
        return 200;
    }

    @ModifyArg(method = "registerGoals",
            at = @At(value = "INVOKE", target = "Ljoshxviii/plantz/ai/goal/GenerateSunGoal;<init>(Ljoshxviii/plantz/entity/plant/Plant;IILkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function0;Ljava/util/function/Predicate;ILkotlin/jvm/functions/Function0;IZILkotlin/jvm/internal/DefaultConstructorMarker;)V"),
            index = 11)
    private static int pvzkongdao$keepCooldownArg(int bitmask) {
        return bitmask & ~2;
    }
}