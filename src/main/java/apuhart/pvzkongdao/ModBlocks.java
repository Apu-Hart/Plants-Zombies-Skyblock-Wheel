// Copyright (c) 2026 Apu-Hart. All rights reserved.
package apuhart.pvzkongdao;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class ModBlocks {
    public static final Block MYSTERY_CHEST = register("mystery_chest",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .strength(0.4f)
                    .sound(SoundType.WOOD)
                    .noLootTable(),
            SpecialChestBlock::new,
            new Item.Properties());
    public static BlockEntityType<SpecialChestEntity> MYSTERY_CHEST_ENTITY;

    public static void initialize() {
        MYSTERY_CHEST_ENTITY = FabricBlockEntityTypeBuilder.create(SpecialChestEntity::new, MYSTERY_CHEST).build();
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, id("mystery_chest")), MYSTERY_CHEST_ENTITY);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(PvzKongdao.MODID, path);
    }

    private static Block register(String name, BlockBehaviour.Properties properties,
                                  java.util.function.Function<BlockBehaviour.Properties, Block> factory,
                                  Item.Properties itemProperties) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, id(name));
        Block block = factory.apply(properties.setId(key));
        Registry.register(BuiltInRegistries.BLOCK, key, block);

        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id(name));
        Registry.register(BuiltInRegistries.ITEM, itemKey, new BlockItem(block, itemProperties.setId(itemKey)));
        return block;
    }
}
