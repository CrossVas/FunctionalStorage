package com.buuz135.functionalstorage.inventory.item;

import com.buuz135.functionalstorage.FunctionalStorage;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DrawerCapabilityProvider implements ICapabilityProvider {

    private final ItemStack stack;
    private final DrawerStackItemHandler drawerStackItemHandler;
    private final LazyOptional<IItemHandler> itemHandler;

    public DrawerCapabilityProvider(ItemStack stack, FunctionalStorage.DrawerType type) {
        this.stack = stack;
        this.drawerStackItemHandler = new DrawerStackItemHandler(stack, type);
        this.itemHandler = LazyOptional.of(() -> this.drawerStackItemHandler);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap.equals(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)) return this.itemHandler.cast();
        return LazyOptional.empty();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
        if (cap.equals(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)) return this.itemHandler.cast();
        return LazyOptional.empty();
    }
}
