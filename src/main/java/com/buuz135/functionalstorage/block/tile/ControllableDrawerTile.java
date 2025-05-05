package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.block.DrawerBlock;
import com.buuz135.functionalstorage.init.FunctionalItems;
import com.buuz135.functionalstorage.item.ConfigurationToolItem;
import com.buuz135.functionalstorage.item.LinkingToolItem;
import com.buuz135.functionalstorage.item.StorageUpgradeItem;
import com.buuz135.functionalstorage.item.UpgradeItem;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.api.IFactory;
import com.hrznstudio.titanium.api.client.IScreenAddon;
import com.hrznstudio.titanium.block.BasicTileBlock;
import com.hrznstudio.titanium.block.tile.ActiveTile;
import com.hrznstudio.titanium.client.screen.addon.TextScreenAddon;
import com.hrznstudio.titanium.component.inventory.InventoryComponent;
import com.hrznstudio.titanium.nbthandler.NBTManager;
import com.hrznstudio.titanium.util.TileUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;

public abstract class ControllableDrawerTile<T extends ControllableDrawerTile<T>> extends ActiveTile<T> {

    private boolean needsUpgradeCache = true;

    @Save
    private BlockPos controllerPos;
    @Save
    private InventoryComponent<ControllableDrawerTile<T>> storageUpgrades;
    @Save
    private InventoryComponent<ControllableDrawerTile<T>> utilityUpgrades;
    @Save
    private DrawerOptions drawerOptions;
    @Save
    private boolean hasDowngrade = false;
    @Save
    private boolean isCreative = false;
    @Save
    private boolean isVoid = false;
    @Save
    private int mult = 1;

    @SuppressWarnings("unchecked")
    public ControllableDrawerTile(BasicTileBlock<T> base) {
        super(base);
        this.drawerOptions = new DrawerOptions();
        this.storageUpgrades = getStorageUpgradesConstructor();
        if (getStorageSlotAmount() > 0) {
            this.addInventory((InventoryComponent<T>) this.storageUpgrades);
        }
        this.addInventory((InventoryComponent<T>) (this.utilityUpgrades = new InventoryComponent<ControllableDrawerTile<T>>("utility_upgrades", 114, 70, 3)
                        .setInputFilter((stack, integer) -> stack.getItem() instanceof UpgradeItem && ((UpgradeItem) stack.getItem()).getType() == UpgradeItem.Type.UTILITY)
                        .setSlotLimit(1)
                        .setOnSlotChanged((itemStack, integer) -> {
                            needsUpgradeCache = true;
                            if (controllerPos != null && this.level.getBlockEntity(controllerPos) instanceof DrawerControllerTile) {
                                DrawerControllerTile controllerTile = (DrawerControllerTile) this.level.getBlockEntity(controllerPos);
                                controllerTile.getConnectedDrawers().rebuild();
                            }
                        })
                )
        );
    }

    @Override
    public List<IFactory<? extends IScreenAddon>> getScreenAddons() {
        List<IFactory<? extends IScreenAddon>> screenAddons = super.getScreenAddons();
        if (getStorageSlotAmount() > 0) {
            screenAddons.add(() -> new TextScreenAddon("Storage", 10, 59, false, TextFormatting.DARK_GRAY.getColor()) {
                @Override
                public String getText() {
                    return new TranslationTextComponent("key.categories.storage").getString();
                }
            });
        }
        screenAddons.add(() -> new TextScreenAddon("Utility", 114, 59, false, TextFormatting.DARK_GRAY.getColor()) {
            @Override
            public String getText() {
                return new TranslationTextComponent("key.categories.utility").getString();
            }
        });
        screenAddons.add(() -> new TextScreenAddon("key.categories.inventory", 8, 92, false, TextFormatting.DARK_GRAY.getColor()) {
            @Override
            public String getText() {
                return new TranslationTextComponent("key.categories.inventory").getString();
            }
        });
        return screenAddons;
    }

    @Override
    public void tick() {
        super.tick();
        if (!isClient()) {
            tickServer();
        }
    }

    public void tickServer() {
        if (level.getGameTime() % 20 == 0) {
            for (int i = 0; i < this.utilityUpgrades.getSlots(); i++) {
                ItemStack stack = this.utilityUpgrades.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    Item item = stack.getItem();
                    if (item.equals(FunctionalItems.REDSTONE_UPGRADE.get())) {
                        level.updateNeighborsAt(this.getBlockPos(), this.getBasicTileBlock());
                        break;
                    }
                }
            }
        }
    }

    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT tag = new CompoundNBT();
        save(tag);
        return tag;
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        load(pkt.getTag());
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        CompoundNBT tag = new CompoundNBT();
        saveAdditional(tag);
        return new SUpdateTileEntityPacket(getBlockPos(), 1, tag);
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        load(compound);
        // super implementation
        this.worldPosition = new BlockPos(compound.getInt("x"), compound.getInt("y"), compound.getInt("z"));
        if (compound.contains("ForgeData")) ObfuscationReflectionHelper.setPrivateValue(TileEntity.class, this, compound.getCompound("ForgeData"), "customTileData");
        if (getCapabilities() != null && compound.contains("ForgeCaps")) deserializeCaps(compound.getCompound("ForgeCaps"));
    }

    public void load(CompoundNBT compoundNBT) {
        NBTManager.getInstance().readTileEntity(this, compoundNBT);
    }

    @Nonnull
    @Override
    public CompoundNBT save(CompoundNBT compound) {
        // Inject the "metadata" that saveMetadata would add
        // we basically mimic the newer system
        ResourceLocation id = TileEntityType.getKey(this.getType());
        if (id == null) {
            throw new RuntimeException(this.getClass() + " is missing a mapping! This is a bug!");
        }
        compound.putString("id", id.toString());
        BlockPos pos = this.getBlockPos();
        compound.putInt("x", pos.getX());
        compound.putInt("y", pos.getY());
        compound.putInt("z", pos.getZ());
        CompoundNBT customTileData = ObfuscationReflectionHelper.getPrivateValue(TileEntity.class, this, "customTileData");
        if (customTileData != null) {
            compound.put("ForgeData", customTileData);
        }
        if (getCapabilities() != null) {
            compound.put("ForgeCaps", serializeCaps());
        }

        // add our data
        this.saveAdditional(compound);
        return compound;
    }

    public CompoundNBT saveAdditional(CompoundNBT compoundNBT) {
        return NBTManager.getInstance().writeTileEntity(this, compoundNBT);
    }

    public final CompoundNBT saveWithoutMetadata() {
        CompoundNBT tag = new CompoundNBT();
        this.saveAdditional(tag);
        return tag;
    }

    public BlockPos getControllerPos() {
        return controllerPos;
    }

    public void setControllerPos(BlockPos controllerPos) {
        if (this.controllerPos != null) {
            TileUtil.getTileEntity(getLevel(), this.controllerPos, DrawerControllerTile.class).ifPresent(drawerControllerTile -> {
                drawerControllerTile.addConnectedDrawers(LinkingToolItem.ActionMode.REMOVE, getBlockPos());
            });
        }
        this.controllerPos = controllerPos;
    }

    public int getStorageMultiplier() {
        maybeCacheUpgrades();
        return mult;
    }

    public boolean isVoid() {
        maybeCacheUpgrades();
        return isVoid;
    }

    public boolean isCreative() {
        maybeCacheUpgrades();
        return isCreative;
    }

    public double getStorageDiv() {
        return 1;
    }

    public void setNeedsUpgradeCache(boolean needsUpgradeCache) {
        this.needsUpgradeCache = needsUpgradeCache;
    }

    public ActionResultType onSlotActivated(PlayerEntity playerIn, Hand hand, Direction facing, double hitX, double hitY, double hitZ, int slot) {
        ItemStack stack = playerIn.getItemInHand(hand);
        if (stack.getItem().equals(FunctionalItems.CONFIGURATION_TOOL.get()) || stack.getItem().equals(FunctionalItems.LINKING_TOOL.get()))
            return ActionResultType.PASS;
        if (!stack.isEmpty() && stack.getItem() instanceof UpgradeItem) {
            UpgradeItem upgradeItem = (UpgradeItem) stack.getItem();
            if (upgradeItem instanceof StorageUpgradeItem) {
                StorageUpgradeItem storageUpgradeItem = (StorageUpgradeItem) upgradeItem;
                InventoryComponent component = storageUpgrades;
                for (int i = 0; i < component.getSlots(); i++) {
                    if (component.getStackInSlot(i).isEmpty()) {
                        playerIn.setItemInHand(hand, component.insertItem(i, stack, false));
                        return ActionResultType.SUCCESS;
                    }
                }
                for (int i = 0; i < component.getSlots(); i++) {
                    if (!component.getStackInSlot(i).isEmpty() && component.getStackInSlot(i).getItem() instanceof StorageUpgradeItem && ((StorageUpgradeItem) component.getStackInSlot(i).getItem()).getStorageMultiplier() < storageUpgradeItem.getStorageMultiplier()) {
                        ItemHandlerHelper.giveItemToPlayer(playerIn, component.getStackInSlot(i).copy());
                        ItemStack upgradeStack = stack.copy();
                        upgradeStack.setCount(1);
                        component.setStackInSlot(i, upgradeStack);
                        stack.shrink(1);
                        return ActionResultType.SUCCESS;
                    }
                }
            } else {
                InventoryComponent component = utilityUpgrades;
                for (int i = 0; i < component.getSlots(); i++) {
                    if (component.getStackInSlot(i).isEmpty()) {
                        playerIn.setItemInHand(hand, component.insertItem(i, stack, false));
                        return ActionResultType.SUCCESS;
                    }
                }
            }
        }
        if (super.onActivated(playerIn, hand, facing, hitX, hitY, hitZ) == ActionResultType.SUCCESS) {
            return ActionResultType.SUCCESS;
        }
        if (slot == -1) {
            openGui(playerIn);
        }
        return ActionResultType.SUCCESS;
    }

    public abstract int getStorageSlotAmount();

    public void onClicked(PlayerEntity playerIn, int slot) {}

    public abstract int getBaseSize(int lost);

    private void maybeCacheUpgrades() {
        if (needsUpgradeCache) {
            isCreative = false;
            hasDowngrade = false;
            mult = 1;
            for (int i = 0; i < storageUpgrades.getSlots(); i++) {
                Item upgrade = storageUpgrades.getStackInSlot(i).getItem();
                if (upgrade.equals(FunctionalItems.FLINT_UPGRADE.get())) {
                    hasDowngrade = true;
                }
                if (upgrade.equals(FunctionalItems.CREATIVE_UPGRADE.get())) {
                    isCreative = true;
                }
                if (upgrade instanceof StorageUpgradeItem) {
                    double calculated = ((StorageUpgradeItem) upgrade).getStorageMultiplier() / getStorageDiv();
                    mult *= calculated;
                }
            }
            isVoid = false;
            for (int i = 0; i < utilityUpgrades.getSlots(); i++) {
                if (utilityUpgrades.getStackInSlot(i).getItem().equals(FunctionalItems.VOID_UPGRADE.get())) {
                    isVoid = true;
                }
            }
            needsUpgradeCache = false;
        }
    }

    public boolean hasDowngrade() {
        maybeCacheUpgrades();
        return hasDowngrade;
    }

    public void toggleLocking() {
        setLocked(!this.isLocked());
    }

    public boolean isLocked() {
        return this.getBlockState().hasProperty(DrawerBlock.LOCKED) && this.getBlockState().getValue(DrawerBlock.LOCKED);
    }

    public void setLocked(boolean locked) {
        if (this.getBlockState().hasProperty(DrawerBlock.LOCKED)) {
            this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(DrawerBlock.LOCKED, locked), 3);
        }
    }

    public void toggleOption(ConfigurationToolItem.ConfigurationAction action) {
        this.drawerOptions.setActive(action, !this.drawerOptions.isActive(action));
        markForUpdate();
    }

    public DrawerOptions getDrawerOptions() {
        return drawerOptions;
    }


    public InventoryComponent<ControllableDrawerTile<T>> getUtilityUpgrades() {
        return utilityUpgrades;
    }

    public InventoryComponent<ControllableDrawerTile<T>> getStorageUpgrades() {
        return storageUpgrades;
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
    }

    public boolean isEverythingEmpty() {
        for (int i = 0; i < getStorageUpgrades().getSlots(); i++) {
            if (!getStorageUpgrades().getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        for (int i = 0; i < getUtilityUpgrades().getSlots(); i++) {
            if (!getUtilityUpgrades().getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public abstract InventoryComponent<ControllableDrawerTile<T>> getStorageUpgradesConstructor();

    public static class DrawerOptions implements INBTSerializable<CompoundNBT> {

        public HashMap<ConfigurationToolItem.ConfigurationAction, Boolean> options;

        public DrawerOptions() {
            this.options = new HashMap<>();
            this.options.put(ConfigurationToolItem.ConfigurationAction.TOGGLE_NUMBERS, true);
            this.options.put(ConfigurationToolItem.ConfigurationAction.TOGGLE_RENDER, true);
            this.options.put(ConfigurationToolItem.ConfigurationAction.TOGGLE_UPGRADES, true);
        }

        public boolean isActive(ConfigurationToolItem.ConfigurationAction configurationAction) {
            return options.getOrDefault(configurationAction, true);
        }

        public void setActive(ConfigurationToolItem.ConfigurationAction configurationAction, boolean active) {
            this.options.put(configurationAction, active);
        }

        @Override
        public CompoundNBT serializeNBT() {
            CompoundNBT compoundTag = new CompoundNBT();
            for (ConfigurationToolItem.ConfigurationAction action : this.options.keySet()) {
                compoundTag.putBoolean(action.name(), this.options.get(action));
            }
            return compoundTag;
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            for (String allKey : nbt.getAllKeys()) {
                this.options.put(ConfigurationToolItem.ConfigurationAction.valueOf(allKey), nbt.getBoolean(allKey));
            }
        }
    }
}
