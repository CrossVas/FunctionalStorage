package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.block.config.FunctionalStorageConfig;
import com.buuz135.functionalstorage.init.FunctionalItems;
import com.buuz135.functionalstorage.item.StorageUpgradeItem;
import com.buuz135.functionalstorage.item.UpgradeItem;
import com.hrznstudio.titanium.block.BasicTileBlock;
import com.hrznstudio.titanium.component.inventory.InventoryComponent;
import com.hrznstudio.titanium.util.RayTraceUtils;
import com.hrznstudio.titanium.util.TileUtil;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.UUID;

public abstract class ItemControllableDrawerTile<T extends ItemControllableDrawerTile<T>> extends ControllableDrawerTile<T> {

    private static HashMap<UUID, Long> INTERACTION_LOGGER = new HashMap<>();

    public ItemControllableDrawerTile(BasicTileBlock<T> base) {
        super(base);
    }

    @Override
    public void tickServer() {
        super.tickServer();
        if (level.getGameTime() % FunctionalStorageConfig.UPGRADE_TICK == 0) {
            for (int i = 0; i < this.getUtilityUpgrades().getSlots(); i++) {
                ItemStack stack = this.getUtilityUpgrades().getStackInSlot(i);
                if (!stack.isEmpty()) {
                    Item item = stack.getItem();
                    if (item.equals(FunctionalItems.PULLING_UPGRADE.get())) {
                        Direction direction = UpgradeItem.getDirection(stack);
                        TileUtil.getTileEntity(level, getBlockPos().relative(direction)).ifPresent(blockEntity1 -> {
                            blockEntity1.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite()).ifPresent(iItemHandler -> {
                                for (int otherSlot = 0; otherSlot < iItemHandler.getSlots(); otherSlot++) {
                                    ItemStack pulledStack = iItemHandler.extractItem(otherSlot, FunctionalStorageConfig.UPGRADE_PULL_ITEMS, true);
                                    if (pulledStack.isEmpty()) continue;
                                    boolean hasWorked = false;
                                    for (int ourSlot = 0; ourSlot < this.getStorage().getSlots(); ourSlot++) {
                                        ItemStack simulated = getStorage().insertItem(ourSlot, pulledStack, true);
                                        if (!simulated.equals(pulledStack)) {
                                            ItemStack extracted = iItemHandler.extractItem(otherSlot, pulledStack.getCount() - simulated.getCount(), false);
                                            getStorage().insertItem(ourSlot, extracted, false);
                                            hasWorked = true;
                                            break;
                                        }
                                    }
                                    if (hasWorked) break;
                                }
                            });
                        });
                    }
                    if (item.equals(FunctionalItems.PUSHING_UPGRADE.get())) {
                        Direction direction = UpgradeItem.getDirection(stack);
                        TileUtil.getTileEntity(level, getBlockPos().relative(direction)).ifPresent(blockEntity1 -> {
                            blockEntity1.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite()).ifPresent(otherHandler -> {
                                for (int sourceSlot = 0; sourceSlot < getStorage().getSlots(); sourceSlot++) {
                                    ItemStack toTransferSim = getStorage().extractItem(sourceSlot, FunctionalStorageConfig.UPGRADE_PUSH_ITEMS, true);
                                    if (toTransferSim.isEmpty()) continue;
                                    for (int targetSlot = 0; targetSlot < otherHandler.getSlots(); targetSlot++) {
                                        ItemStack remainder = otherHandler.insertItem(targetSlot, toTransferSim, true);
                                        int inserted = toTransferSim.getCount() - remainder.getCount();
                                        if (inserted > 0) {
                                            // extract the amount we were actually able to insert
                                            ItemStack extracted = getStorage().extractItem(sourceSlot, inserted, false);
                                            otherHandler.insertItem(targetSlot, extracted, false);
                                            // check if there is still anything left to try with this stack
                                            toTransferSim = remainder;
                                            if (toTransferSim.isEmpty()) {
                                                break; // done
                                            }
                                        }
                                    }
                                    // if we made no progress at all for this source slot, assume inventory is full
                                    if (toTransferSim.getCount() == getStorage().extractItem(sourceSlot, FunctionalStorageConfig.UPGRADE_PUSH_ITEMS, true).getCount()) {
                                        break;
                                    }
                                }
                            });
                        });
                    }
                    if (item.equals(FunctionalItems.COLLECTOR_UPGRADE.get())) {
                        Direction direction = UpgradeItem.getDirection(stack);
                        AxisAlignedBB box = new AxisAlignedBB(getBlockPos().relative(direction));
                        for (ItemEntity entitiesOfClass : level.getEntitiesOfClass(ItemEntity.class, box)) {
                            ItemStack pulledStack = ItemHandlerHelper.copyStackWithSize(entitiesOfClass.getItem(), Math.min(entitiesOfClass.getItem().getCount(), FunctionalStorageConfig.UPGRADE_COLLECTOR_ITEMS));
                            if (pulledStack.isEmpty()) continue;
                            boolean hasWorked = false;
                            for (int ourSlot = 0; ourSlot < this.getStorage().getSlots(); ourSlot++) {
                                ItemStack simulated = getStorage().insertItem(ourSlot, pulledStack, true);
                                if (simulated.getCount() != pulledStack.getCount()) {
                                    getStorage().insertItem(ourSlot, ItemHandlerHelper.copyStackWithSize(entitiesOfClass.getItem(), pulledStack.getCount() - simulated.getCount()), false);
                                    entitiesOfClass.getItem().shrink(pulledStack.getCount() - simulated.getCount());
                                    hasWorked = true;
                                    break;
                                }
                            }
                            if (hasWorked) break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public ActionResultType onSlotActivated(PlayerEntity playerIn, Hand hand, Direction facing, double hitX, double hitY, double hitZ, int slot) {
        ItemStack stack = playerIn.getItemInHand(hand);
        if (super.onActivated(playerIn, hand, facing, hitX, hitY, hitZ) == ActionResultType.SUCCESS) {
            return ActionResultType.SUCCESS;
        }
        if (slot != -1 && isServer()) {
            if (!stack.isEmpty() && getStorage().insertItem(slot, stack, true).getCount() != stack.getCount()) {
                playerIn.setItemInHand(hand, getStorage().insertItem(slot, stack, false));
                return ActionResultType.SUCCESS;
            } else if (System.currentTimeMillis() - INTERACTION_LOGGER.getOrDefault(playerIn.getUUID(), System.currentTimeMillis()) < 300) {
                for (ItemStack itemStack : playerIn.inventory.items) {
                    if (!itemStack.isEmpty() && getStorage().insertItem(slot, itemStack, true).getCount() != itemStack.getCount()) {
                        itemStack.setCount(getStorage().insertItem(slot, itemStack.copy(), false).getCount());
                    }
                }
            }
            INTERACTION_LOGGER.put(playerIn.getUUID(), System.currentTimeMillis());
        }
        if (super.onSlotActivated(playerIn, hand, facing, hitX, hitY, hitZ, slot) == ActionResultType.SUCCESS) {
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.SUCCESS;
    }

    public abstract int getStorageSlotAmount();

    public void onClicked(PlayerEntity playerIn, int slot) {
        // TODO: re-think this
        if (isServer() && slot != -1) {
            RayTraceResult rayTraceResult = RayTraceUtils.rayTraceSimple(this.level, playerIn, 16, 0);
            if (rayTraceResult.getType() == RayTraceResult.Type.BLOCK) {
                BlockRayTraceResult blockResult = (BlockRayTraceResult) rayTraceResult;
                Direction facing = blockResult.getDirection();
                if (facing.equals(this.getFacingDirection())) {
                    ItemHandlerHelper.giveItemToPlayer(playerIn, getStorage().extractItem(slot, playerIn.isShiftKeyDown() ? getStorage().getStackInSlot(slot).getMaxStackSize() : 1, false));
                }
            }
        }
    }

    public abstract IItemHandler getStorage();

    public abstract LazyOptional<IItemHandler> getOptional();

    public abstract int getBaseSize(int lost);

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        getOptional().invalidate();
    }

    @Override
    public InventoryComponent<ControllableDrawerTile<T>> getStorageUpgradesConstructor() {
        return new InventoryComponent<ControllableDrawerTile<T>>("storage_upgrades", 10, 70, getStorageSlotAmount()) {
            @Nonnull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                ItemStack stack = this.getStackInSlot(slot);
                if (stack.getItem() instanceof StorageUpgradeItem) {
                    int mult = 1;
                    for (int i = 0; i < getStorageUpgrades().getSlots(); i++) {
                        if (getStorageUpgrades().getStackInSlot(i).getItem() instanceof StorageUpgradeItem) {
                            if (i == slot) continue;
                            if (mult == 1)
                                mult = ((StorageUpgradeItem) getStorageUpgrades().getStackInSlot(i).getItem()).getStorageMultiplier();
                            else
                                mult *= ((StorageUpgradeItem) getStorageUpgrades().getStackInSlot(i).getItem()).getStorageMultiplier();
                        }
                    }
                    for (int i = 0; i < getStorage().getSlots(); i++) {
                        if (getStorage().getStackInSlot(i).isEmpty()) continue;
                        double stackSize = getStorage().getStackInSlot(i).getMaxStackSize() / 64D;
                        if ((int) Math.floor(Math.min(Integer.MAX_VALUE, getBaseSize(i) * (long) mult) * stackSize) < getStorage().getStackInSlot(i).getCount()) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
                return super.extractItem(slot, amount, simulate);
            }
        }
                .setInputFilter((stack, integer) -> {
                    if (stack.getItem().equals(FunctionalItems.FLINT_UPGRADE.get())) {
                        for (int i = 0; i < getStorage().getSlots(); i++) {
                            if (getStorage().getStackInSlot(i).getCount() > 64) {
                                return false;
                            }
                        }
                    }
                    return stack.getItem() instanceof UpgradeItem && ((UpgradeItem) stack.getItem()).getType() == UpgradeItem.Type.STORAGE;
                })
                .setOnSlotChanged((stack, integer) -> {
                    setNeedsUpgradeCache(true);
                })
                .setSlotLimit(1);
    }

    public boolean isEverythingEmpty() {
        for (int i = 0; i < getStorage().getSlots(); i++) {
            if (!getStorage().getStackInSlot(i).isEmpty()) {
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

}
