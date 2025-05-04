package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.config.FunctionalStorageConfig;
import com.buuz135.functionalstorage.client.gui.FluidDrawerInfoGuiAddon;
import com.buuz135.functionalstorage.fluid.BigFluidHandler;
import com.buuz135.functionalstorage.init.FunctionalItems;
import com.buuz135.functionalstorage.item.StorageUpgradeItem;
import com.buuz135.functionalstorage.item.UpgradeItem;
import com.buuz135.functionalstorage.util.DrawerType;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.block.BasicTileBlock;
import com.hrznstudio.titanium.component.inventory.InventoryComponent;
import com.hrznstudio.titanium.util.TileUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IBucketPickupHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.wrappers.BucketPickupHandlerWrapper;
import net.minecraftforge.fluids.capability.wrappers.FluidBlockWrapper;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FluidDrawerTile extends ControllableDrawerTile<FluidDrawerTile> {

    public LazyOptional<IFluidHandler> fluidHandlerLazyOptional;
    @Save
    private BigFluidHandler fluidHandler;
    private DrawerType type;

    public FluidDrawerTile(BasicTileBlock<FluidDrawerTile> base, DrawerType type) {
        super(base);
        this.type = type;
        this.fluidHandler = new BigFluidHandler(type.getSlots(), getTankCapacity(getStorageMultiplier())) {
            @Override
            public void onChange() {
                syncObject(fluidHandler);
            }

            @Override
            public boolean isDrawerLocked() {
                return isLocked();
            }

            @Override
            public boolean isDrawerVoid() {
                return isVoid();
            }

            @Override
            public boolean isDrawerCreative() {
                return isCreative();
            }
        };
        this.fluidHandlerLazyOptional = LazyOptional.of(() -> fluidHandler);
    }

    private int getTankCapacity(int storageMultiplier) {
        long maxCap = ((type.getSlotAmount() / 64)) * 1000L * storageMultiplier;
        return (int) Math.min(Integer.MAX_VALUE, maxCap);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void initClient() {
        super.initClient();
        String slotName = "";
        if (type.getSlots() == 2) {
            slotName = "_2";
        }
        if (type.getSlots() == 4) {
            slotName = "_4";
        }
        String finalSlotName = slotName;
        addGuiAddonFactory(() -> new FluidDrawerInfoGuiAddon(64, 16,
                new ResourceLocation(FunctionalStorage.MOD_ID, "textures/blocks/fluid_front" + finalSlotName + ".png"),
                type.getSlots(),
                type.getSlotPosition(),
                this::getFluidHandler,
                integer -> getFluidHandler().getTankCapacity(integer)
        ));
    }

    @Nonnull
    @Override
    public <U> LazyOptional<U> getCapability(@Nonnull Capability<U> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return LazyOptional.empty();
        }
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return fluidHandlerLazyOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public double getStorageDiv() {
        return 2;
    }

    @Override
    public void tickServer() {
        super.tickServer();
        if (level.getGameTime() % FunctionalStorageConfig.UPGRADE_TICK == 0) {
            for (int i = 0; i < this.getUtilityUpgrades().getSlots(); i++) {
                net.minecraft.item.ItemStack stack = this.getUtilityUpgrades().getStackInSlot(i);
                if (!stack.isEmpty()) {
                    Item item = stack.getItem();
                    if (item.equals(FunctionalItems.PUSHING_UPGRADE.get())) {
                        Direction direction = UpgradeItem.getDirection(stack);
                        TileUtil.getTileEntity(level, getBlockPos().relative(direction)).ifPresent(blockEntity1 -> {
                            blockEntity1.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction.getOpposite()).ifPresent(otherFluidHandler -> {
                                for (int tankId = 0; tankId < this.getFluidHandler().getTanks(); tankId++) {
                                    BigFluidHandler.CustomFluidTank fluidTank = this.fluidHandler.getTankList()[tankId];
                                    if (fluidTank.getFluid().isEmpty()) continue;
                                    FluidStack extracted = fluidTank.drain(FunctionalStorageConfig.UPGRADE_PUSH_FLUID, IFluidHandler.FluidAction.SIMULATE);
                                    if (extracted.isEmpty()) continue;
                                    int insertedAmount = otherFluidHandler.fill(extracted, IFluidHandler.FluidAction.EXECUTE);
                                    if (insertedAmount > 0) {
                                        fluidTank.drain(insertedAmount, IFluidHandler.FluidAction.EXECUTE);
                                        this.fluidHandler.onChange();
                                        break;
                                    }
                                }
                            });
                        });
                    }
                    if (item.equals(FunctionalItems.PULLING_UPGRADE.get())) {
                        Direction direction = UpgradeItem.getDirection(stack);
                        TileUtil.getTileEntity(level, getBlockPos().relative(direction)).ifPresent(blockEntity1 -> {
                            blockEntity1.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction.getOpposite()).ifPresent(otherFluidHandler -> {
                                for (int tankId = 0; tankId < this.getFluidHandler().getTanks(); tankId++) {
                                    BigFluidHandler.CustomFluidTank fluidTank = this.fluidHandler.getTankList()[tankId];
                                    FluidStack extracted = otherFluidHandler.drain(FunctionalStorageConfig.UPGRADE_PULL_FLUID, IFluidHandler.FluidAction.SIMULATE);
                                    if (extracted.isEmpty()) continue;
                                    int insertedAmount = fluidTank.fill(extracted, IFluidHandler.FluidAction.EXECUTE);
                                    if (insertedAmount > 0) {
                                        otherFluidHandler.drain(insertedAmount, IFluidHandler.FluidAction.EXECUTE);
                                        this.fluidHandler.onChange();
                                        break;
                                    }
                                }
                            });
                        });
                    }
                    if (item.equals(FunctionalItems.COLLECTOR_UPGRADE.get()) && level.getGameTime() % (FunctionalStorageConfig.UPGRADE_TICK * 3) == 0) {
                        Direction direction = UpgradeItem.getDirection(stack);
                        FluidState fluidstate = this.level.getFluidState(this.getBlockPos().relative(direction));
                        if (!fluidstate.isEmpty() && fluidstate.isSource()) {
                            BlockState state = level.getBlockState(getBlockPos().relative(direction));
                            Block block = state.getBlock();
                            IFluidHandler targetFluidHandler = null;
                            if (block instanceof IFluidBlock) {
                                targetFluidHandler = new FluidBlockWrapper((IFluidBlock) block, level, getBlockPos().relative(direction));
                            } else if (block instanceof IBucketPickupHandler) {
                                targetFluidHandler = new BucketPickupHandlerWrapper((IBucketPickupHandler) block, level, getBlockPos().relative(direction));
                            }
                            if (targetFluidHandler != null) {
                                FluidStack drained = targetFluidHandler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
                                if (!drained.isEmpty()) {
                                    for (int tankId = 0; tankId < this.getFluidHandler().getTanks(); tankId++) {
                                        BigFluidHandler.CustomFluidTank fluidTank = this.fluidHandler.getTankList()[tankId];
                                        int insertedAmount = fluidTank.fill(drained, IFluidHandler.FluidAction.SIMULATE);
                                        if (insertedAmount == drained.getAmount()) {
                                            fluidTank.fill(drained, IFluidHandler.FluidAction.EXECUTE);
                                            targetFluidHandler.drain(insertedAmount, IFluidHandler.FluidAction.EXECUTE);
                                            this.fluidHandler.onChange();
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public ActionResultType onSlotActivated(PlayerEntity playerIn, Hand hand, Direction facing, double hitX, double hitY, double hitZ, int slot) {
        if (hand == Hand.MAIN_HAND) {
            ItemStack stack = playerIn.getItemInHand(Hand.MAIN_HAND);
            if (stack.getItem().equals(FunctionalItems.CONFIGURATION_TOOL.get()) || stack.getItem().equals(FunctionalItems.LINKING_TOOL.get()))
                return ActionResultType.PASS;
            if (slot != -1 && !playerIn.getItemInHand(hand).isEmpty()) {
                ActionResultType interactionResult = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).map(iFluidHandlerItem -> {
                    return playerIn.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).map(iItemHandler -> {
                        FluidActionResult result = FluidUtil.tryEmptyContainerAndStow(stack, this.fluidHandler.getTankList()[slot], iItemHandler, Integer.MAX_VALUE, playerIn, true);
                        if (result.isSuccess()) {
                            playerIn.setItemInHand(hand, result.getResult());
                            return ActionResultType.SUCCESS;
                        } else return ActionResultType.PASS;
                    }).orElse(ActionResultType.PASS);
                }).orElse(ActionResultType.PASS);
                if (interactionResult == ActionResultType.SUCCESS) {
                    return interactionResult;
                }
            }
        }
        return super.onSlotActivated(playerIn, hand, facing, hitX, hitY, hitZ, slot);
    }

    @Override
    public void onClicked(PlayerEntity playerIn, int slot) {
        ItemStack stack = playerIn.getItemInHand(Hand.MAIN_HAND);
        if (slot != -1 && !stack.isEmpty()) {
            stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).ifPresent(iFluidHandlerItem -> {
                playerIn.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(iItemHandler -> {
                    FluidActionResult result = FluidUtil.tryFillContainerAndStow(stack, this.fluidHandler.getTankList()[slot], iItemHandler, Integer.MAX_VALUE, playerIn, true);
                    if (result.isSuccess()) {
                        playerIn.setItemInHand(Hand.MAIN_HAND, result.getResult());
                    }
                });
            });
        }
    }

    @Nonnull
    @Override
    public FluidDrawerTile getSelf() {
        return this;
    }

    public DrawerType getDrawerType() {
        return type;
    }

    @Override
    public int getStorageSlotAmount() {
        return 4;
    }

    @Override
    public int getBaseSize(int lost) {
        return type.getSlotAmount();
    }

    public BigFluidHandler getFluidHandler() {
        return fluidHandler;
    }

    @Override
    public void setLocked(boolean locked) {
        super.setLocked(locked);
        this.fluidHandler.lockHandler();
        syncObject(this.fluidHandler);
    }

    public boolean isEverythingEmpty() {
        for (int i = 0; i < getFluidHandler().getTanks(); i++) {
            if (!getFluidHandler().getFluidInTank(i).isEmpty()) {
                return false;
            }
        }
        if (isLocked()) return false;
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
    public InventoryComponent<ControllableDrawerTile<FluidDrawerTile>> getStorageUpgradesConstructor() {
        return new InventoryComponent<ControllableDrawerTile<FluidDrawerTile>>("storage_upgrades", 10, 70, getStorageSlotAmount()) {
            @Nonnull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                ItemStack stack = this.getStackInSlot(slot);
                if (stack.getItem() instanceof StorageUpgradeItem) {
                    int mult = 1;
                    for (int i = 0; i < getStorageUpgrades().getSlots(); i++) {
                        if (getStorageUpgrades().getStackInSlot(i).getItem() instanceof StorageUpgradeItem) {
                            if (i == slot) continue;
                            double calculated = ((StorageUpgradeItem) getStorageUpgrades().getStackInSlot(i).getItem()).getStorageMultiplier() / getStorageDiv();
                            if (mult == 1)
                                mult = (int) calculated;
                            else
                                mult *= calculated;
                        }
                    }
                    for (int i = 0; i < getFluidHandler().getTanks(); i++) {
                        if (getFluidHandler().getFluidInTank(i).isEmpty()) continue;
                        if (getFluidHandler().getFluidInTank(i).getAmount() > getTankCapacity(mult)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
                return super.extractItem(slot, amount, simulate);
            }
        }
                .setInputFilter((stack, integer) -> {
                    if (stack.getItem().equals(FunctionalItems.FLINT_UPGRADE.get())) {
                        return false;
                    }
                    return stack.getItem() instanceof UpgradeItem && ((UpgradeItem) stack.getItem()).getType() == UpgradeItem.Type.STORAGE;
                })
                .setOnSlotChanged((stack, integer) -> {
                    setNeedsUpgradeCache(true);
                    this.fluidHandler.setCapacity(getTankCapacity(getStorageMultiplier()));
                    syncObject(this.fluidHandler);
                })
                .setSlotLimit(1);
    }
}
