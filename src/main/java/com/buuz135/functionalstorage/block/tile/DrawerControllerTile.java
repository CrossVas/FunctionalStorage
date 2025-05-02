package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.config.FunctionalStorageConfig;
import com.buuz135.functionalstorage.fluid.ControllerFluidHandler;
import com.buuz135.functionalstorage.inventory.ControllerInventoryHandler;
import com.buuz135.functionalstorage.inventory.ILockable;
import com.buuz135.functionalstorage.item.ConfigurationToolItem;
import com.buuz135.functionalstorage.item.LinkingToolItem;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.block.BasicTileBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DrawerControllerTile extends ItemControllableDrawerTile<DrawerControllerTile> {

    private static HashMap<UUID, Long> INTERACTION_LOGGER = new HashMap<>();

    @Save
    private ConnectedDrawers connectedDrawers;
    public ControllerInventoryHandler inventoryHandler;
    public ControllerFluidHandler fluidHandler;
    private LazyOptional<IItemHandler> itemHandlerLazyOptional;
    private LazyOptional<IFluidHandler> fluidHandlerLazyOptional;

    public DrawerControllerTile(BasicTileBlock<DrawerControllerTile> base, TileEntityType<DrawerControllerTile> blockEntityType, BlockPos pos, BlockState state) {
        super(base, blockEntityType, pos, state);
        this.connectedDrawers = new ConnectedDrawers(null);
        this.inventoryHandler = new ControllerInventoryHandler() {
            @Override
            public ConnectedDrawers getDrawers() {
                return connectedDrawers;
            }
        };
        this.itemHandlerLazyOptional = LazyOptional.of(() -> this.inventoryHandler);
        this.fluidHandler = new ControllerFluidHandler() {
            @Override
            public ConnectedDrawers getDrawers() {
                return connectedDrawers;
            }
        };
        this.fluidHandlerLazyOptional = LazyOptional.of(() -> this.fluidHandler);
    }

    @Override
    public int getStorageSlotAmount() {
        return 1;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.connectedDrawers.getConnectedDrawers().size() != (this.connectedDrawers.getItemHandlers().size() + this.connectedDrawers.getFluidHandlers().size() + this.connectedDrawers.getExtensions())) {
            this.connectedDrawers.getConnectedDrawers().removeIf(aLong -> !(this.getLevel().getBlockEntity(BlockPos.of(aLong)) instanceof ControllableDrawerTile<?>));
            this.connectedDrawers.setLevel(getLevel());
            this.connectedDrawers.rebuild();
            markForUpdate();
            updateNeigh();
        }
    }

    public ActionResultType onSlotActivated(PlayerEntity playerIn, Hand hand, Direction facing, double hitX, double hitY, double hitZ) {
        ItemStack stack = playerIn.getItemInHand(hand);
        if (stack.getItem().equals(FunctionalStorage.CONFIGURATION_TOOL.get()) || stack.getItem().equals(FunctionalStorage.LINKING_TOOL.get()))
            return ActionResultType.PASS;
        if (isServer()) {
            for (IItemHandler iItemHandler : this.getConnectedDrawers().itemHandlers) {
                if (iItemHandler instanceof ILockable && ((ILockable) iItemHandler).isLocked()) {
                    for (int slot = 0; slot < iItemHandler.getSlots(); slot++) {
                        if (!stack.isEmpty() && iItemHandler.insertItem(slot, stack, true).getCount() != stack.getCount()) {
                            playerIn.setItemInHand(hand, iItemHandler.insertItem(slot, stack, false));
                            return ActionResultType.SUCCESS;
                        } else if (System.currentTimeMillis() - INTERACTION_LOGGER.getOrDefault(playerIn.getUUID(), System.currentTimeMillis()) < 300) {
                            for (ItemStack itemStack : playerIn.inventory.items) {
                                if (!itemStack.isEmpty() && iItemHandler.insertItem(slot, itemStack, true).getCount() != itemStack.getCount()) {
                                    itemStack.setCount(iItemHandler.insertItem(slot, itemStack.copy(), false).getCount());
                                }
                            }
                        }
                    }
                }
            }
            for (IItemHandler iItemHandler : this.getConnectedDrawers().itemHandlers) {
                if (iItemHandler instanceof ILockable && !((ILockable) iItemHandler).isLocked()) {
                    for (int slot = 0; slot < iItemHandler.getSlots(); slot++) {
                        if (!stack.isEmpty() && !iItemHandler.getStackInSlot(slot).isEmpty() && iItemHandler.insertItem(slot, stack, true).getCount() != stack.getCount()) {
                            playerIn.setItemInHand(hand, iItemHandler.insertItem(slot, stack, false));
                            return ActionResultType.SUCCESS;
                        } else if (System.currentTimeMillis() - INTERACTION_LOGGER.getOrDefault(playerIn.getUUID(), System.currentTimeMillis()) < 300) {
                            for (ItemStack itemStack : playerIn.inventory.items) {
                                if (!itemStack.isEmpty() && !iItemHandler.getStackInSlot(slot).isEmpty() && iItemHandler.insertItem(slot, itemStack, true).getCount() != itemStack.getCount()) {
                                    itemStack.setCount(iItemHandler.insertItem(slot, itemStack.copy(), false).getCount());
                                }
                            }
                        }
                    }
                }
            }
            INTERACTION_LOGGER.put(playerIn.getUUID(), System.currentTimeMillis());
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public IItemHandler getStorage() {
        return inventoryHandler;
    }

    @Override
    public LazyOptional<IItemHandler> getOptional() {
        return itemHandlerLazyOptional;
    }

    @Override
    public int getBaseSize(int lost) {
        return 1;
    }

    @Override
    public void toggleLocking() {
        super.toggleLocking();
        if (isServer()) {
            for (Long connectedDrawer : new ArrayList<>(this.connectedDrawers.getConnectedDrawers())) {
                TileEntity blockEntity = this.level.getBlockEntity(BlockPos.of(connectedDrawer));
                if (blockEntity instanceof DrawerControllerTile) continue;
                if (blockEntity instanceof ControllableDrawerTile) {
                    ((ControllableDrawerTile<?>) blockEntity).setLocked(this.isLocked());
                }
            }
        }
    }

    @Override
    public void toggleOption(ConfigurationToolItem.ConfigurationAction action) {
        super.toggleOption(action);
        if (isServer()) {
            for (Long connectedDrawer : new ArrayList<>(this.connectedDrawers.getConnectedDrawers())) {
                TileEntity blockEntity = this.level.getBlockEntity(BlockPos.of(connectedDrawer));
                if (blockEntity instanceof DrawerControllerTile) continue;
                if (blockEntity instanceof ControllableDrawerTile) {
                    ((ControllableDrawerTile<?>) blockEntity).getDrawerOptions().setActive(action, this.getDrawerOptions().isActive(action));
                    ((ControllableDrawerTile<?>) blockEntity).markForUpdate();
                }
            }
        }
    }

    @Nonnull
    @Override
    public DrawerControllerTile getSelf() {
        return this;
    }

    public ConnectedDrawers getConnectedDrawers() {
        return connectedDrawers;
    }

    public void addConnectedDrawers(LinkingToolItem.ActionMode action, BlockPos... positions) {
        AxisAlignedBB area = new AxisAlignedBB(this.getBlockPos()).inflate(FunctionalStorageConfig.DRAWER_CONTROLLER_LINKING_RANGE);
        for (BlockPos position : positions) {
            if (level.getBlockState(position).is(FunctionalStorage.DRAWER_CONTROLLER.getLeft().get())) continue;
            if (area.contains(Vector3d.atCenterOf(position)) && this.getLevel().getBlockEntity(position) instanceof ControllableDrawerTile<?>) {
                ControllableDrawerTile<?> controllableDrawerTile = (ControllableDrawerTile<?>) this.getLevel().getBlockEntity(position);
                if (action == LinkingToolItem.ActionMode.ADD) {
                    controllableDrawerTile.setControllerPos(this.getBlockPos());
                    if (!connectedDrawers.getConnectedDrawers().contains(position.asLong())){
                        this.connectedDrawers.getConnectedDrawers().add(position.asLong());
                    }
                }
            }
            if (action == LinkingToolItem.ActionMode.REMOVE) {
                this.connectedDrawers.getConnectedDrawers().removeIf(aLong -> aLong == position.asLong());
            }
        }
        this.connectedDrawers.rebuild();
        markForUpdate();
    }

    @Nonnull
    @Override
    public <U> LazyOptional<U> getCapability(@Nonnull Capability<U> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return itemHandlerLazyOptional.cast();
        }
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return fluidHandlerLazyOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.fluidHandlerLazyOptional.invalidate();
        this.itemHandlerLazyOptional.invalidate();
    }

    public class ConnectedDrawers implements INBTSerializable<CompoundNBT> {

        private List<Long> connectedDrawers;
        private List<IItemHandler> itemHandlers;
        private List<IFluidHandler> fluidHandlers;
        private World level;
        private int extensions;
        private VoxelShape cachedVoxelShape;

        public ConnectedDrawers(World level) {
            this.connectedDrawers = new ArrayList<>();
            this.itemHandlers = new ArrayList<>();
            this.fluidHandlers = new ArrayList<>();
            this.level = level;
            this.extensions = 0;
            this.cachedVoxelShape = null;
        }

        public void setLevel(World level) {
            this.level = level;
        }

        public void rebuildShapes() {
            this.cachedVoxelShape = VoxelShapes.create(new AxisAlignedBB(DrawerControllerTile.this.getBlockPos()));
            for (Long connectedDrawer : this.connectedDrawers) {
                this.cachedVoxelShape = VoxelShapes.join(this.cachedVoxelShape, VoxelShapes.create(new AxisAlignedBB(BlockPos.of(connectedDrawer))), IBooleanFunction.OR);
            }
        }

        public void rebuild() {
            this.itemHandlers = new ArrayList<>();
            this.fluidHandlers = new ArrayList<>();
            if (level != null && !level.isClientSide()) {
                for (Long connectedDrawer : this.connectedDrawers) {
                    BlockPos pos = BlockPos.of(connectedDrawer);
                    TileEntity entity = level.getBlockEntity(pos);
                    if (entity instanceof DrawerControllerTile) continue;
                    if (entity instanceof ControllerExtensionTile) {
                        ++extensions;
                    }
                    if (entity instanceof ItemControllableDrawerTile<?>) {
                        this.itemHandlers.add(((ItemControllableDrawerTile<?>) entity).getStorage());
                    }
                    if (entity instanceof FluidDrawerTile) {
                        this.fluidHandlers.add(((FluidDrawerTile) entity).getFluidHandler());
                    }
                }
            }
            DrawerControllerTile.this.inventoryHandler.invalidateSlots();
            DrawerControllerTile.this.fluidHandler.invalidateSlots();
        }

        @Override
        public CompoundNBT serializeNBT() {
            CompoundNBT compoundTag = new CompoundNBT();
            for (int i = 0; i < this.connectedDrawers.size(); i++) {
                compoundTag.putLong(i + "", this.connectedDrawers.get(i));
            }
            return compoundTag;
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            this.connectedDrawers = new ArrayList<>();
            for (String allKey : nbt.getAllKeys()) {
                connectedDrawers.add(nbt.getLong(allKey));
            }
            rebuild();
            if (DrawerControllerTile.this.level != null && DrawerControllerTile.this.level.isClientSide()) {
                rebuildShapes();
            }
        }

        public List<Long> getConnectedDrawers() {
            return connectedDrawers;
        }

        public List<IItemHandler> getItemHandlers() {
            return itemHandlers;
        }

        public List<IFluidHandler> getFluidHandlers() {
            return fluidHandlers;
        }

        public int getExtensions() {
            return extensions;
        }

        public VoxelShape getCachedVoxelShape() {
            return cachedVoxelShape;
        }
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return super.getRenderBoundingBox().inflate(50);
    }
}
