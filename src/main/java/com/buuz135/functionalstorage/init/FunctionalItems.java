package com.buuz135.functionalstorage.init;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.item.ConfigurationToolItem;
import com.buuz135.functionalstorage.item.LinkingToolItem;
import com.buuz135.functionalstorage.item.StorageUpgradeItem;
import com.buuz135.functionalstorage.item.UpgradeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class FunctionalItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, FunctionalStorage.MOD_ID);

    public static final RegistryObject<Item> FLINT_UPGRADE = ITEMS.register("flint_upgrade", () -> new StorageUpgradeItem(StorageUpgradeItem.StorageTier.FLINT));
    public static final RegistryObject<Item> OBSIDIAN_UPGRADE = ITEMS.register("obsidian_upgrade", () -> new StorageUpgradeItem(StorageUpgradeItem.StorageTier.OBSIDIAN));
    public static final RegistryObject<Item> IRON_UPGRADE = ITEMS.register("iron_upgrade", () -> new StorageUpgradeItem(StorageUpgradeItem.StorageTier.IRON));
    public static final RegistryObject<Item> GOLD_UPGRADE = ITEMS.register("gold_upgrade", () -> new StorageUpgradeItem(StorageUpgradeItem.StorageTier.GOLD));
    public static final RegistryObject<Item> DIAMOND_UPGRADE = ITEMS.register("diamond_upgrade", () -> new StorageUpgradeItem(StorageUpgradeItem.StorageTier.DIAMOND));
    public static final RegistryObject<Item> NETHERITE_UPGRADE = ITEMS.register("netherite_upgrade", () -> new StorageUpgradeItem(StorageUpgradeItem.StorageTier.NETHERITE));

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

    public static final RegistryObject<Item> CONFIGURATION_TOOL = ITEMS.register("configuration_tool", ConfigurationToolItem::new);
    public static final RegistryObject<Item> LINKING_TOOL = ITEMS.register("linking_tool", LinkingToolItem::new);

    public static void initItems(IEventBus e) {
        ITEMS.register(e);
    }

    public static List<RegistryObject<Item>> STORAGE_UPGRADE = Arrays.asList(
            FLINT_UPGRADE, OBSIDIAN_UPGRADE, IRON_UPGRADE, GOLD_UPGRADE, DIAMOND_UPGRADE, NETHERITE_UPGRADE
    );
}
