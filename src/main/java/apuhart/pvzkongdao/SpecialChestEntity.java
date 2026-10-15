// Copyright (c) 2026 Apu-Hart. All rights reserved.
package apuhart.pvzkongdao;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class SpecialChestEntity extends BaseContainerBlockEntity {
    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    private boolean filled = false;

    public SpecialChestEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.MYSTERY_CHEST_ENTITY, pos, state);
    }

    private void fillLoot() {
        filled = true;
        ItemStack helmet = new ItemStack(Items.NETHERITE_HELMET);
        enchant(helmet, Enchantments.PROTECTION, 4);
        enchant(helmet, Enchantments.RESPIRATION, 3);
        enchant(helmet, Enchantments.AQUA_AFFINITY, 1);
        enchant(helmet, Enchantments.UNBREAKING, 3);
        items.set(0, helmet);

        ItemStack chestplate = new ItemStack(Items.NETHERITE_CHESTPLATE);
        enchant(chestplate, Enchantments.PROTECTION, 4);
        enchant(chestplate, Enchantments.THORNS, 3);
        enchant(chestplate, Enchantments.UNBREAKING, 3);
        items.set(1, chestplate);

        ItemStack leggings = new ItemStack(Items.NETHERITE_LEGGINGS);
        enchant(leggings, Enchantments.PROTECTION, 4);
        enchant(leggings, Enchantments.UNBREAKING, 3);
        items.set(2, leggings);

        ItemStack boots = new ItemStack(Items.NETHERITE_BOOTS);
        enchant(boots, Enchantments.PROTECTION, 4);
        enchant(boots, Enchantments.FEATHER_FALLING, 4);
        enchant(boots, Enchantments.DEPTH_STRIDER, 3);
        enchant(boots, Enchantments.UNBREAKING, 3);
        items.set(3, boots);

        ItemStack bow = new ItemStack(Items.BOW);
        enchant(bow, Enchantments.POWER, 5);
        enchant(bow, Enchantments.PUNCH, 2);
        enchant(bow, Enchantments.FLAME, 1);
        enchant(bow, Enchantments.INFINITY, 1);
        enchant(bow, Enchantments.UNBREAKING, 3);
        items.set(4, bow);

        ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);
        enchant(sword, Enchantments.SHARPNESS, 5);
        enchant(sword, Enchantments.SWEEPING_EDGE, 3);
        enchant(sword, Enchantments.LOOTING, 3);
        enchant(sword, Enchantments.FIRE_ASPECT, 2);
        enchant(sword, Enchantments.UNBREAKING, 3);
        items.set(5, sword);

        items.set(6, new ItemStack(Items.ARROW, 64));

        ItemStack elytra = new ItemStack(Items.ELYTRA);
        enchant(elytra, Enchantments.UNBREAKING, 3);
        enchant(elytra, Enchantments.MENDING, 1);
        items.set(7, elytra);

        setChanged();
    }

    private void enchant(ItemStack stack, ResourceKey<Enchantment> key, int power) {
        stack.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key), power);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (filled && level != null && !level.isClientSide() && looted()) {
            level.removeBlock(getBlockPos(), false);
        }
    }

    private boolean looted() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int getContainerSize() {
        return 27;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    public void startOpen(ContainerUser user) {
        super.startOpen(user);
        if (!filled && level != null && !level.isClientSide()) {
            fillLoot();
        }
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return ChestMenu.threeRows(containerId, inventory, this);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.pvzkongdao.mystery_chest");
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("Filled", filled);
        ContainerHelper.saveAllItems(output, items);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        filled = input.getBooleanOr("Filled", false);
        ContainerHelper.loadAllItems(input, items);
    }
}
