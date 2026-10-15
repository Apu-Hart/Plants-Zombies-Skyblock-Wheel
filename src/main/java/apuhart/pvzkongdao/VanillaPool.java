// Copyright (c) 2026 Apu-Hart. All rights reserved.
package apuhart.pvzkongdao;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class VanillaPool {
    private static final Set<Item> BLOCKED = Set.of(
            Items.AIR, Items.BEDROCK, Items.BARRIER, Items.LIGHT, Items.STRUCTURE_VOID,
            Items.STRUCTURE_BLOCK, Items.JIGSAW, Items.COMMAND_BLOCK, Items.CHAIN_COMMAND_BLOCK,
            Items.REPEATING_COMMAND_BLOCK, Items.COMMAND_BLOCK_MINECART, Items.DEBUG_STICK,
            Items.KNOWLEDGE_BOOK, Items.BUDDING_AMETHYST, Items.REINFORCED_DEEPSLATE,
            Items.PETRIFIED_OAK_SLAB
    );

    private static final List<Item> GOOD = List.of(
            Items.DIAMOND, Items.DIAMOND_BLOCK, Items.EMERALD, Items.EMERALD_BLOCK,
            Items.NETHERITE_INGOT, Items.NETHERITE_SCRAP, Items.ANCIENT_DEBRIS, Items.NETHERITE_BLOCK,
            Items.GOLD_BLOCK, Items.IRON_BLOCK, Items.NETHER_STAR, Items.ELYTRA,
            Items.TOTEM_OF_UNDYING, Items.ENCHANTED_GOLDEN_APPLE, Items.GOLDEN_APPLE,
            Items.EXPERIENCE_BOTTLE, Items.ENDER_PEARL, Items.SHULKER_SHELL, Items.SHULKER_BOX,
            Items.DRAGON_BREATH, Items.ECHO_SHARD, Items.HEART_OF_THE_SEA, Items.NAUTILUS_SHELL,
            Items.TRIDENT, Items.OBSIDIAN, Items.CRYING_OBSIDIAN, Items.BLAZE_ROD,
            Items.GLOWSTONE, Items.AMETHYST_SHARD, Items.LAPIS_BLOCK, Items.REDSTONE_BLOCK,
            Items.DIAMOND_SWORD, Items.DIAMOND_PICKAXE, Items.DIAMOND_AXE, Items.DIAMOND_SHOVEL,
            Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS,
            Items.NETHERITE_SWORD, Items.NETHERITE_PICKAXE, Items.NETHERITE_AXE,
            Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS,
            Items.NETHERITE_BOOTS, Items.BOW, Items.CROSSBOW, Items.SHIELD,
            Items.BEACON, Items.END_CRYSTAL
    );

    private static List<Item> every;

    private VanillaPool() {
    }

    public static Item randomItem(RandomSource random) {
        if (every == null) {
            List<Item> list = new ArrayList<>();
            for (Item item : BuiltInRegistries.ITEM) {
                Identifier id = BuiltInRegistries.ITEM.getKey(item);
                if (!"minecraft".equals(id.getNamespace()) || BLOCKED.contains(item)) {
                    continue;
                }
                list.add(item);
                if (item instanceof BlockItem && !GOOD.contains(item)) {
                    list.add(item);
                    list.add(item);
                }
                if (item.getDefaultInstance().has(DataComponents.FOOD)) {
                    list.add(item);
                    list.add(item);
                }
                if (isEquipment(item)) {
                    list.add(item);
                    list.add(item);
                }
            }
            every = list;
        }
        return every.get(random.nextInt(every.size()));
    }

    private static boolean isEquipment(Item item) {
        ItemStack stack = item.getDefaultInstance();
        return stack.has(DataComponents.EQUIPPABLE) || stack.has(DataComponents.TOOL)
                || stack.has(DataComponents.WEAPON) || stack.has(DataComponents.PIERCING_WEAPON)
                || stack.has(DataComponents.KINETIC_WEAPON);
    }
}