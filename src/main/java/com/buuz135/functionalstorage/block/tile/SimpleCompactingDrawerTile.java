package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.client.gui.DrawerInfoGuiAddon;
import com.buuz135.functionalstorage.init.FunctionalItems;
import com.buuz135.functionalstorage.inventory.CompactingInventoryHandler;
import com.buuz135.functionalstorage.util.CompactingUtil;
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
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class SimpleCompactingDrawerTile extends ItemControllableDrawerTile<SimpleCompactingDrawerTile> {

    private final LazyOptional<IItemHandler> lazyStorage;
    @Save
    public CompactingInventoryHandler handler;
    private boolean hasCheckedRecipes;

    public SimpleCompactingDrawerTile(BasicTileBlock<SimpleCompactingDrawerTile> base) {
        super(base);
        this.handler = new CompactingInventoryHandler(2) {
            @Override
            public void onChange() {
                SimpleCompactingDrawerTile.this.markForUpdate();
            }

            @Override
            public int getMultiplier() {
                return getStorageMultiplier();
            }

            @Override
            public boolean isVoid() {
                return SimpleCompactingDrawerTile.this.isVoid();
            }

            @Override
            public boolean hasDowngrade() {
                return SimpleCompactingDrawerTile.this.hasDowngrade();
            }

            @Override
            public boolean isCreative() {
                return SimpleCompactingDrawerTile.this.isCreative();
            }

            @Override
            public boolean isLocked() {
                return SimpleCompactingDrawerTile.this.isLocked();
            }

        };
        lazyStorage = LazyOptional.of(() -> this.handler);
        this.hasCheckedRecipes = false;
    }

    @Override
    public List<IFactory<? extends IScreenAddon>> getScreenAddons() {
        List<IFactory<? extends IScreenAddon>> screenAddons = super.getScreenAddons();
        screenAddons.add(() -> new DrawerInfoGuiAddon(64, 16,
                new ResourceLocation(FunctionalStorage.MOD_ID, "textures/blocks/simple_compacting_drawer_front.png"),
                2,
                integer -> {
                    if (integer == 0) return Pair.of(16, 28);
                    if (integer == 1) return Pair.of(16, 4);
                    return Pair.of(16, 4);
                },
                integer -> getStorage().getStackInSlot(integer),
                integer -> getStorage().getSlotLimit(integer)
        ));
        return screenAddons;
    }

    @Override
    public void tickServer() {
        super.tickServer();
        if (!hasCheckedRecipes) {
            if (!handler.getParent().isEmpty()) {
                CompactingUtil compactingUtil = new CompactingUtil(this.level, 2);
                compactingUtil.setup(handler.getParent());
                handler.setup(compactingUtil);
            }
            hasCheckedRecipes = true;
        }
    }

    @Override
    public ActionResultType onSlotActivated(PlayerEntity playerIn, Hand hand, Direction facing, double hitX, double hitY, double hitZ, int slot) {
        ItemStack stack = playerIn.getItemInHand(hand);
        if (stack.getItem().equals(FunctionalItems.CONFIGURATION_TOOL.get()) || stack.getItem().equals(FunctionalItems.LINKING_TOOL.get()))
            return ActionResultType.PASS;
        if (!handler.isSetup() && slot != -1) {
            stack = playerIn.getItemInHand(hand).copy();
            stack.setCount(1);
            CompactingUtil compactingUtil = new CompactingUtil(this.level, 2);
            compactingUtil.setup(stack);
            handler.setup(compactingUtil);
            for (int i = 0; i < handler.getResultList().size(); i++) {
                if (ItemStack.isSame(handler.getResultList().get(i).getResult(), stack)) {
                    slot = i;
                    break;
                }
            }
        }
        return super.onSlotActivated(playerIn, hand, facing, hitX, hitY, hitZ, slot);
    }

    @Override
    public int getStorageSlotAmount() {
        return 3;
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
    public int getBaseSize(int slot) {
        return handler.getSlotLimitBase(slot);
    }

    @Nonnull
    @Override
    public <U> LazyOptional<U> getCapability(@Nonnull Capability<U> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return lazyStorage.cast();
        }
        return super.getCapability(cap, side);
    }

    @Nonnull
    @Override
    public SimpleCompactingDrawerTile getSelf() {
        return this;
    }

    public CompactingInventoryHandler getHandler() {
        return handler;
    }

}
