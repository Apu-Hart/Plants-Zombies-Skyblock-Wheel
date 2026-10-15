// Copyright (c) 2026 Apu-Hart. All rights reserved.
package apuhart.pvzkongdao;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PvzPools {
    private static final List<EntityType<?>> zombies = new ArrayList<>();
    private static final List<EntityType<?>> plants = new ArrayList<>();
    private static Item sunItem;
    private static Item seedPacket;

    public static void init() {
        if (sunItem != null) {
            return;
        }
        for (var entry : BuiltInRegistries.ENTITY_TYPE.entrySet()) {
            Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entry.getValue());
            if (!PvzKongdao.PZ.equals(id.getNamespace())) {
                continue;
            }
            MobCategory category = entry.getValue().getCategory();
            if (category == MobCategory.MONSTER) {
                zombies.add(entry.getValue());
            } else if (category == MobCategory.CREATURE) {
                plants.add(entry.getValue());
            }
        }
        for (var entry : BuiltInRegistries.ITEM.entrySet()) {
            Identifier id = BuiltInRegistries.ITEM.getKey(entry.getValue());
            if (!PvzKongdao.PZ.equals(id.getNamespace())) {
                continue;
            }
            switch (id.getPath()) {
                case "sun" -> sunItem = entry.getValue();
                case "seed_packet" -> seedPacket = entry.getValue();
            }
        }
        PvzKongdao.LOGGER.info("PvZ 联动加载完成：{} 种僵尸，{} 种植物", zombies.size(), plants.size());
    }

    public static EntityType<?> randomZombie(RandomSource random) {
        return zombies.get(random.nextInt(zombies.size()));
    }

    public static EntityType<?> randomPlant(RandomSource random) {
        return plants.get(random.nextInt(plants.size()));
    }

    public static boolean isPlant(Entity entity) {
        EntityType<?> type = entity.getType();
        Identifier id = idOf(type);
        return id != null && PvzKongdao.PZ.equals(id.getNamespace()) && type.getCategory() == MobCategory.CREATURE;
    }

    public static void seekAnywhere(Mob mob) {
        AttributeInstance follow = mob.getAttribute(Attributes.FOLLOW_RANGE);
        if (follow != null) {
            follow.setBaseValue(256.0);
        }
    }

    public static Item sunItem() {
        return sunItem;
    }

    public static Item seedPacket() {
        return seedPacket;
    }

    public static ItemStack seedStack(EntityType<?> type, int count) {
        if (seedPacket == null) {
            return null;
        }
        ItemStack stack = new ItemStack(seedPacket, count);
        if (type != null) {
            stack.set(DataComponents.ENTITY_DATA, TypedEntityData.of(type, new CompoundTag()));
        }
        return stack;
    }

    public static Item spawnEgg(EntityType<?> type) {
        Identifier id = idOf(type);
        return BuiltInRegistries.ITEM.getOptional(Identifier.fromNamespaceAndPath(id.getNamespace(), id.getPath() + "_spawn_egg")).orElse(null);
    }

    public static EntityType<?> typeByPath(String path) {
        for (var entry : BuiltInRegistries.ENTITY_TYPE.entrySet()) {
            if (path.equals(idOf(entry.getValue()).getPath())) {
                return entry.getValue();
            }
        }
        return null;
    }

    public static Identifier idOf(EntityType<?> type) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(type);
    }
}
