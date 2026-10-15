// Copyright (c) 2026 Apu-Hart. All rights reserved.
package apuhart.pvzkongdao.mixin;

import apuhart.pvzkongdao.Rewards;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {

    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void pvzkongdao$keepReward(Player player, CallbackInfo ci) {
        ItemEntity item = (ItemEntity) (Object) this;
        if (!item.entityTags().contains(Rewards.REWARD_TAG) || !player.hasInfiniteMaterials()) {
            return;
        }
        if (Rewards.canHold(player.getInventory(), item.getItem())) {
            return;
        }
        ci.cancel();
    }
}