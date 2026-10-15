// Copyright (c) 2026 Apu-Hart. All rights reserved.
package apuhart.pvzkongdao.mixin;

import apuhart.pvzkongdao.ModData;
import apuhart.pvzkongdao.PvzKongdao;
import joshxviii.plantz.UtilsKt;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(UtilsKt.class)
public class SunPaymentMixin {

    @Inject(method = "getTotalSun", at = @At("RETURN"), cancellable = true)
    private static void pvzkongdao$includeBank(Player player, CallbackInfoReturnable<Integer> cir) {
        if (!(player instanceof ServerPlayer serverPlayer) || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        cir.setReturnValue(cir.getReturnValueI() + ModData.get(level.getServer().overworld()).getSun(serverPlayer.getUUID()));
    }

    @Inject(method = "removeSunFromStorageAndInventory", at = @At("RETURN"), cancellable = true)
    private static void pvzkongdao$payFromBank(Player player, int amount, CallbackInfoReturnable<Integer> cir) {
        int rest = cir.getReturnValueI();
        if (rest <= 0 || !(player instanceof ServerPlayer serverPlayer) || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        ModData data = ModData.get(level.getServer().overworld());
        int take = Math.min(rest, data.getSun(serverPlayer.getUUID()));
        if (take <= 0) {
            return;
        }
        data.addSun(serverPlayer.getUUID(), -take);
        PvzKongdao.syncSun(serverPlayer);
        cir.setReturnValue(rest - take);
    }
}