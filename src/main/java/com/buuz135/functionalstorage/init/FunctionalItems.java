package com.buuz135.functionalstorage.init;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.DrawerBlock;
import com.buuz135.functionalstorage.item.ConfigurationToolItem;
import com.buuz135.functionalstorage.item.LinkingToolItem;
import com.buuz135.functionalstorage.item.StorageUpgradeItem;
import com.buuz135.functionalstorage.item.UpgradeItem;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

public class FunctionalItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, FunctionalStorage.MOD_ID);

    public static final RegistryObject<Item> COPPER_UPGRADE = ITEMS.register("copper_upgrade", () -> new StorageUpgradeItem(StorageUpgradeItem.StorageTier.COPPER));
    public static final RegistryObject<Item> GOLD_UPGRADE = ITEMS.register("gold_upgrade", () -> new StorageUpgradeItem(StorageUpgradeItem.StorageTier.GOLD));
    public static final RegistryObject<Item> DIAMOND_UPGRADE = ITEMS.register("diamond_upgrade", () -> new StorageUpgradeItem(StorageUpgradeItem.StorageTier.DIAMOND));
    public static final RegistryObject<Item> NETHERITE_UPGRADE = ITEMS.register("netherite_upgrade", () -> new StorageUpgradeItem(StorageUpgradeItem.StorageTier.NETHERITE));
    public static final RegistryObject<Item> IRON_UPGRADE = ITEMS.register("iron_downgrade", () -> new StorageUpgradeItem(StorageUpgradeItem.StorageTier.IRON));
    public static final RegistryObject<Item> CREATIVE_UPGRADE = ITEMS.register("creative_vending_upgrade", () -> new UpgradeItem(new Item.Properties(), UpgradeItem.Type.STORAGE) {
        @Override
        public boolean isFoil(@Nonnull ItemStack stack) {
            return true;
        }
    });

    public static final RegistryObject<Item> COLLECTOR_UPGRADE = ITEMS.register("collector_upgrade", () -> new UpgradeItem(new Item.Properties(), UpgradeItem.Type.UTILITY));
    public static final RegistryObject<Item> PULLING_UPGRADE = ITEMS.register("puller_upgrade", () -> new UpgradeItem(new Item.Properties(), UpgradeItem.Type.UTILITY));
    public static final RegistryObject<Item> PUSHING_UPGRADE = ITEMS.register("pusher_upgrade", () -> new UpgradeItem(new Item.Properties(), UpgradeItem.Type.UTILITY));
    public static final RegistryObject<Item> VOID_UPGRADE = ITEMS.register("void_upgrade", () -> new UpgradeItem(new Item.Properties(), UpgradeItem.Type.UTILITY));
    public static final RegistryObject<Item> REDSTONE_UPGRADE = ITEMS.register("redstone_upgrade", () -> new UpgradeItem(new Item.Properties(), UpgradeItem.Type.UTILITY));

    public static final RegistryObject<Item> CONFIGURATION_TOOL = ITEMS.register("linking_tool", LinkingToolItem::new);
    public static final RegistryObject<Item> LINKING_TOOL = ITEMS.register("configuration_tool", ConfigurationToolItem::new);

    public static void initItems(IEventBus e) {
//        TYPED_DRAWER_BLOCKS.forEach(FunctionalItems::registerCustomBlockItem);
//        Stream.of(
//                COMPACTING, FRAMED_COMPACTING, SIMPLE_COMPACTING,
//                FLUID_1, FLUID_2, FLUID_4,
//                CONTROLLER, CONTROLLER_EXTENSION,
//                ARMORY_CABINET, ENDER
//        ).forEach(FunctionalItems::registerDefaultBlockItem);
        ITEMS.register(e);
    }

    private static void registerCustomBlockItem(RegistryObject<Block> block) {
        ITEMS.register(block.getId().getPath(), () -> new DrawerBlock.DrawerItem((DrawerBlock) block.get(), new Item.Properties()/*.tab(FunctionalStorage.TAB)*/));
    }

    private static void registerDefaultBlockItem(RegistryObject<Block> block) {
        ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()/*.tab(FunctionalStorage.TAB)*/));
    }

    public static Stream<RegistryObject<Item>> STORAGE_UPGRADE = Stream.of(
            COPPER_UPGRADE, GOLD_UPGRADE, DIAMOND_UPGRADE, NETHERITE_UPGRADE, IRON_UPGRADE
    );

    public static Stream<RegistryObject<Item>> UPGRADES = Stream.of(
            COPPER_UPGRADE, GOLD_UPGRADE, DIAMOND_UPGRADE, NETHERITE_UPGRADE, IRON_UPGRADE, CREATIVE_UPGRADE, COLLECTOR_UPGRADE, PULLING_UPGRADE, PUSHING_UPGRADE, VOID_UPGRADE, REDSTONE_UPGRADE
    );
}
