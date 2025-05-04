package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.client.gui.DrawerInfoGuiAddon;
import com.buuz135.functionalstorage.init.FunctionalItems;
import com.buuz135.functionalstorage.inventory.EnderInventoryHandler;
import com.buuz135.functionalstorage.network.EnderDrawerSyncMessage;
import com.buuz135.functionalstorage.util.DrawerType;
import com.buuz135.functionalstorage.world.EnderSavedData;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.block.BasicTileBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class EnderDrawerTile extends ItemControllableDrawerTile<EnderDrawerTile> {

    @Save
    private String frequency;
    private LazyOptional<IItemHandler> lazyStorage;

    public EnderDrawerTile(BasicTileBlock<EnderDrawerTile> base) {
        super(base);
        this.frequency = UUID.randomUUID().toString();
        this.lazyStorage = LazyOptional.empty();
    }

    @Override
    public IItemHandler getItemHandler() {
        return lazyStorage.orElse(null);
    }

    @Override
    public void setLevelAndPosition(World world, BlockPos pos) {
        super.setLevelAndPosition(world, pos);
        this.lazyStorage.invalidate();
        this.lazyStorage = LazyOptional.of(() -> EnderSavedData.getInstance(this.level).getFrequency(this.frequency));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void initClient() {
        super.initClient();
        addGuiAddonFactory(() -> new DrawerInfoGuiAddon(64, 16,
                new ResourceLocation(FunctionalStorage.MOD_ID, "textures/blocks/ender_front.png"),
                1,
                DrawerType.X_1.getSlotPosition(),
                integer -> getStorage().getStackInSlot(integer),
                integer -> getStorage().getSlotLimit(integer)
        ));
    }

    @Nonnull
    @Override
    public <U> LazyOptional<U> getCapability(@Nonnull Capability<U> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return lazyStorage.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void tick() {
        super.tick();
        if (level.getGameTime() % 20 == 0) {
            FunctionalStorage.NETWORK.sendToNearby(level, getBlockPos(), 32, new EnderDrawerSyncMessage(frequency, ((EnderInventoryHandler) getStorage())));
        }
        if (level.getGameTime() % 10 == 0) {
            EnderInventoryHandler handler = EnderSavedData.getInstance(this.level).getFrequency(this.frequency);
            if (handler.isLocked() != isLocked()) {
                super.setLocked(handler.isLocked());
            }
            if (!handler.isVoid()) {
                for (int i = 0; i < getUtilityUpgrades().getSlots(); i++) {
                    ItemStack stack = getUtilityUpgrades().getStackInSlot(i);
                    if (!stack.isEmpty() && stack.sameItem(new ItemStack(FunctionalItems.VOID_UPGRADE.get()))) {
                        handler.setVoidItems(true);
                        stack.shrink(1);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public ActionResultType onSlotActivated(PlayerEntity playerIn, Hand hand, Direction facing, double hitX, double hitY, double hitZ, int slot) {
        ActionResultType result = super.onSlotActivated(playerIn, hand, facing, hitX, hitY, hitZ, slot);
        if (slot != -1) {
            FunctionalStorage.NETWORK.sendToNearby(level, this.getBlockPos(), 32, new EnderDrawerSyncMessage(frequency, ((EnderInventoryHandler) getStorage())));
        }
        return result;
    }

    @Override
    public void onClicked(PlayerEntity playerIn, int slot) {
        super.onClicked(playerIn, slot);
        if (slot != -1) {
            FunctionalStorage.NETWORK.sendToNearby(level, this.getBlockPos(), 32, new EnderDrawerSyncMessage(frequency, ((EnderInventoryHandler) getStorage())));
        }
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        String oldFreq = this.frequency;
        super.load(state, compound);
        if (!this.frequency.equalsIgnoreCase(oldFreq) && level instanceof ServerWorld) {
            setFrequency(this.frequency);
        }
    }

    @Override
    public void setLocked(boolean locked) {
        super.setLocked(locked);
        EnderSavedData.getInstance(this.level).getFrequency(this.frequency).setLocked(locked);
    }

    @Override
    public boolean isVoid() {
        return EnderSavedData.getInstance(this.level).getFrequency(this.frequency).isVoid();
    }

    @Nonnull
    @Override
    public EnderDrawerTile getSelf() {
        return this;
    }

    @Override
    public int getStorageSlotAmount() {
        return 0;
    }

    @Override
    public IItemHandler getStorage() {
        return this.lazyStorage.resolve().get();
    }

    @Override
    public LazyOptional<IItemHandler> getOptional() {
        return this.lazyStorage;
    }

    @Override
    public boolean isEverythingEmpty() {
        EnderInventoryHandler inventoryHandler = EnderSavedData.getInstance(this.level).getFrequency(this.frequency);
        for (int i = 0; i < inventoryHandler.getSlots(); i++) {
            if (!inventoryHandler.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
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

    @Override
    public int getBaseSize(int lost) {
        return 1;
    }

    public void setFrequency(String frequency) {
        if (frequency == null) return;
        this.frequency = frequency;
        this.lazyStorage.invalidate();
        this.lazyStorage = LazyOptional.of(() -> EnderSavedData.getInstance(this.level).getFrequency(this.frequency));
        this.markForUpdate();
    }

    public String getFrequency() {
        return frequency;
    }
}
