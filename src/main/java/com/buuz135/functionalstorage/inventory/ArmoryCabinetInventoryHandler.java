package com.buuz135.functionalstorage.inventory;

import com.buuz135.functionalstorage.block.config.FunctionalStorageConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class ArmoryCabinetInventoryHandler implements IItemHandler, INBTSerializable<CompoundNBT> {

    public List<ItemStack> stackList;

    public ArmoryCabinetInventoryHandler() {
        this.stackList = create();
    }

    @Override
    public int getSlots() {
        return FunctionalStorageConfig.ARMORY_CABINET_SIZE;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot < this.stackList.size()) {
            return this.stackList.get(slot);
        }
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (isValid(slot, stack)) {
            if (!simulate) {
                this.stackList.set(slot, stack);
                onChange();
            }
            return ItemStack.EMPTY;
        }
        return stack;
    }

    public abstract void onChange();

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!simulate) {
            ItemStack stack = this.stackList.set(slot, ItemStack.EMPTY);
            onChange();
            return stack;
        }
        return this.stackList.get(slot);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return isCertifiedStack(stack);
    }

    private boolean isValid(int slot, @Nonnull ItemStack stack) {
        return !stack.isEmpty() && this.stackList.get(slot).isEmpty() && isCertifiedStack(stack);
    }

    private boolean isCertifiedStack(ItemStack stack) {
        if (stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).isPresent()) return false;
        if (stack.getMaxStackSize() > 1) return false;
        return stack.hasTag() || stack.isDamageableItem() || stack.isEnchantable();
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT compoundTag = new CompoundNBT();
        for (int i = 0; i < this.stackList.size(); i++) {
            ItemStack stack = this.stackList.get(i);
            if (!stack.isEmpty()) {
                compoundTag.put(i + "", stack.serializeNBT());
            }
        }
        return compoundTag;
    }

    private List<ItemStack> create() {
        List<ItemStack> stackList = new ArrayList<>();
        for (int i = 0; i < FunctionalStorageConfig.ARMORY_CABINET_SIZE; i++) {
            stackList.add(ItemStack.EMPTY);
        }
        return stackList;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        this.stackList = create();
        for (String allKey : nbt.getAllKeys()) {
            int pos = Integer.parseInt(allKey);
            if (pos < this.stackList.size()) {
                this.stackList.set(pos, ItemStack.of(nbt.getCompound(allKey)));
            }
        }
    }
}
