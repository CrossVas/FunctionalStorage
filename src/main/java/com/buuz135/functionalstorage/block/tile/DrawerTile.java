package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.client.gui.DrawerInfoGuiAddon;
import com.buuz135.functionalstorage.inventory.BigInventoryHandler;
import com.buuz135.functionalstorage.util.IWoodType;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.api.IFactory;
import com.hrznstudio.titanium.api.client.IScreenAddon;
import com.hrznstudio.titanium.block.BasicTileBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class DrawerTile extends ItemControllableDrawerTile<DrawerTile> {

    @Save
    public BigInventoryHandler handler;
    private final LazyOptional<IItemHandler> lazyStorage;
    private FunctionalStorage.DrawerType type;
    private IWoodType woodType;

    public DrawerTile(BasicTileBlock<DrawerTile> base, FunctionalStorage.DrawerType type, IWoodType woodType) {
        super(base);
        this.type = type;
        this.woodType = woodType;
        this.handler = new BigInventoryHandler(type) {
            @Override
            public void onChange() {
                DrawerTile.this.markForUpdate();
            }

            @Override
            public int getMultiplier() {
                return getStorageMultiplier();
            }

            @Override
            public boolean isVoid() {
                return DrawerTile.this.isVoid();
            }

            @Override
            public boolean hasDowngrade() {
                return DrawerTile.this.hasDowngrade();
            }

            @Override
            public boolean isLocked() {
                return DrawerTile.this.isLocked();
            }

            @Override
            public boolean isCreative() {
                return DrawerTile.this.isCreative();
            }


        };
        lazyStorage = LazyOptional.of(() -> this.handler);
    }

    @Override
    public List<IFactory<? extends IScreenAddon>> getScreenAddons() {
        List<IFactory<? extends IScreenAddon>> screenAddons = super.getScreenAddons();
        screenAddons.add(() -> new DrawerInfoGuiAddon(64, 16,
                new ResourceLocation(FunctionalStorage.MOD_ID, "textures/blocks/" + woodType.getName() + "_front_" + type.getSlots() + ".png"),
                type.getSlots(),
                type.getSlotPosition(),
                integer -> getHandler().getStackInSlot(integer),
                integer -> getHandler().getSlotLimit(integer)
        ));
        return screenAddons;
    }

    @Nonnull
    @Override
    public <U> LazyOptional<U> getCapability(@Nonnull Capability<U> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return lazyStorage.cast();
        }
        return super.getCapability(cap, side);
    }

    public ActionResultType onSlotActivated(PlayerEntity playerIn, Hand hand, Direction facing, double hitX, double hitY, double hitZ, int slot) {
        ItemStack stack = playerIn.getItemInHand(hand);
        if (stack.getItem().equals(FunctionalStorage.CONFIGURATION_TOOL.get()) || stack.getItem().equals(FunctionalStorage.LINKING_TOOL.get())) return ActionResultType.PASS;
        if (slot != -1 && !playerIn.getItemInHand(hand).isEmpty()){
            BigInventoryHandler.BigStack bigStack = getHandler().getStoredStacks().get(slot);
            if (bigStack.getStack().isEmpty()){
                bigStack.setStack(playerIn.getItemInHand(hand));
            }
        }
        return super.onSlotActivated(playerIn, hand, facing, hitX, hitY, hitZ, slot);
    }

    @Nonnull
    @Override
    public DrawerTile getSelf() {
        return this;
    }

    public FunctionalStorage.DrawerType getDrawerType() {
        return type;
    }

    @Override
    public int getStorageSlotAmount() {
        return 4;
    }

    @Override
    public IItemHandler getStorage() {
        return handler;
    }

    @Override
    public LazyOptional<IItemHandler> getOptional() {
        return lazyStorage;
    }

    @Override
    public int getBaseSize(int lost) {
        return type.getSlotAmount();
    }

    public BigInventoryHandler getHandler() {
        return handler;
    }

}
