package com.buuz135.functionalstorage.block;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.CompactingDrawerTile;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.DrawerControllerTile;
import com.buuz135.functionalstorage.block.tile.ItemControllableDrawerTile;
import com.buuz135.functionalstorage.init.FunctionalItems;
import com.buuz135.functionalstorage.item.LinkingToolItem;
import com.buuz135.functionalstorage.util.StorageTags;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.hrznstudio.titanium.api.IFactory;
import com.hrznstudio.titanium.block.RotatableBlock;
import com.hrznstudio.titanium.datagenerator.loot.block.BasicBlockLootTables;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import com.hrznstudio.titanium.util.RayTraceUtils;
import com.hrznstudio.titanium.util.TileUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CompactingDrawerBlock extends RotatableBlock<CompactingDrawerTile> {

    public static Multimap<Direction, VoxelShape> CACHED_SHAPES = MultimapBuilder.hashKeys().arrayListValues().build();

    static {
        CACHED_SHAPES.put(Direction.NORTH, VoxelShapes.box(1 / 16D, 1 / 16D, 0, 7 / 16D, 7 / 16D, 1 / 16D));
        CACHED_SHAPES.put(Direction.NORTH, VoxelShapes.box(9 / 16D, 1 / 16D, 0, 15 / 16D, 7 / 16D, 1 / 16D));
        CACHED_SHAPES.put(Direction.NORTH, VoxelShapes.box(1 / 16D, 9 / 16D, 0, 15 / 16D, 15 / 16D, 1 / 16D));
        CACHED_SHAPES.put(Direction.SOUTH, VoxelShapes.box(9 / 16D, 1 / 16D, 15 / 16D, 15 / 16D, 7 / 16D, 1));
        CACHED_SHAPES.put(Direction.SOUTH, VoxelShapes.box(1 / 16D, 1 / 16D, 15 / 16D, 7 / 16D, 7 / 16D, 1));
        CACHED_SHAPES.put(Direction.SOUTH, VoxelShapes.box(1 / 16D, 9 / 16D, 15 / 16D, 15 / 16D, 15 / 16D, 1));
        CACHED_SHAPES.put(Direction.EAST, VoxelShapes.box(15 / 16D, 1 / 16D, 1 / 16D, 1, 7 / 16D, 7 / 16D));
        CACHED_SHAPES.put(Direction.EAST, VoxelShapes.box(15 / 16D, 1 / 16D, 9 / 16D, 1, 7 / 16D, 15 / 16D));
        CACHED_SHAPES.put(Direction.EAST, VoxelShapes.box(15 / 16D, 9 / 16D, 1 / 16D, 1, 15 / 16D, 15 / 16D));
        CACHED_SHAPES.put(Direction.WEST, VoxelShapes.box(0, 1 / 16D, 9 / 16D, 1 / 16D, 7 / 16D, 15 / 16D));
        CACHED_SHAPES.put(Direction.WEST, VoxelShapes.box(0, 1 / 16D, 1 / 16D, 1 / 16D, 7 / 16D, 7 / 16D));
        CACHED_SHAPES.put(Direction.WEST, VoxelShapes.box(0, 9 / 16D, 1 / 16D, 1 / 16D, 15 / 16D, 15 / 16D));
    }


    public CompactingDrawerBlock(String name, Properties properties) {
        super(properties, CompactingDrawerTile.class);
        this.setRegistryName(name);
        setItemGroup(FunctionalStorage.TAB);
        registerDefaultState(defaultBlockState().setValue(RotatableBlock.FACING_HORIZONTAL, Direction.NORTH).setValue(DrawerBlock.LOCKED, false));
    }

    @Nonnull
    @Override
    public RotationType getRotationType() {
        return RotationType.FOUR_WAY;
    }

    @Override
    public IFactory<CompactingDrawerTile> getTileEntityFactory() {
        return () -> new CompactingDrawerTile(this);
    }

    @Override
    public List<VoxelShape> getBoundingBoxes(BlockState state, IBlockReader source, BlockPos pos) {
        return getShapes(state, source, pos);
    }

    private static List<VoxelShape> getShapes(BlockState state, IBlockReader source, BlockPos pos) {
        List<VoxelShape> boxes = new ArrayList<>();
        CACHED_SHAPES.get(state.getValue(RotatableBlock.FACING_HORIZONTAL)).forEach(boxes::add);
        VoxelShape total = VoxelShapes.block();
        boxes.add(total);
        return boxes;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(DrawerBlock.LOCKED);
    }

    @Nonnull
    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext selectionContext) {
        return VoxelShapes.box(0, 0, 0, 1, 1, 1);
    }

    @Override
    public boolean hasCustomBoxes(BlockState state, IBlockReader source, BlockPos pos) {
        return true;
    }

    @Override
    public boolean hasIndividualRenderVoxelShape() {
        return true;
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult ray) {
        return TileUtil.getTileEntity(worldIn, pos, CompactingDrawerTile.class).map(drawerTile -> drawerTile.onSlotActivated(player, hand, ray.getDirection(), ray.getLocation().x, ray.getLocation().y, ray.getLocation().z, getHit(state, worldIn, pos, player))).orElse(ActionResultType.PASS);
    }

    @Override
    public void attack(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
        TileUtil.getTileEntity(worldIn, pos, CompactingDrawerTile.class).ifPresent(drawerTile -> drawerTile.onClicked(player, getHit(state, worldIn, pos, player)));
    }

    public int getHit(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
        RayTraceResult result = RayTraceUtils.rayTraceSimple(worldIn, player, 32, 0);
        if (result instanceof BlockRayTraceResult) {
            VoxelShape hit = RayTraceUtils.rayTraceVoxelShape((BlockRayTraceResult) result, worldIn, player, 32, 0);
            if (hit != null) {
                if (hit.equals(VoxelShapes.block())) return -1;
                List<VoxelShape> shapes = new ArrayList<>(CACHED_SHAPES.get(state.getValue(RotatableBlock.FACING_HORIZONTAL)));
                for (int i = 0; i < shapes.size(); i++) {
                    if (VoxelShapes.joinIsNotEmpty(shapes.get(i), hit, IBooleanFunction.AND)) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    @Override
    public LootTable.Builder getLootTable(@Nonnull BasicBlockLootTables blockLootTables) {
        //CopyNbtFunction.Builder nbtBuilder = CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY);
        //nbtBuilder.copy("handler",  "BlockEntityTag.handler");
        //nbtBuilder.copy("storageUpgrades",  "BlockEntityTag.storageUpgrades");
        //nbtBuilder.copy("utilityUpgrades",  "BlockEntityTag.utilityUpgrades");
        //return blockLootTables.droppingSelfWithNbt(this, nbtBuilder);
        return blockLootTables.droppingNothing();
    }


    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        NonNullList<ItemStack> stacks = NonNullList.create();
        ItemStack stack = new ItemStack(this);
        TileEntity drawerTile = builder.getOptionalParameter(LootParameters.BLOCK_ENTITY);
        if (drawerTile instanceof ControllableDrawerTile) {
            ControllableDrawerTile<?> tile = (ControllableDrawerTile<?>) drawerTile;
            if (!tile.isEverythingEmpty()) {
                stack.getOrCreateTag().put("Tile", drawerTile.save(new CompoundNBT()));
            }
            if (tile.isLocked()) {
                stack.getOrCreateTag().putBoolean("Locked", tile.isLocked());
            }
        }
        stacks.add(stack);
        return stacks;
    }

    @Override
    public NonNullList<ItemStack> getDynamicDrops(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        return NonNullList.create();
    }

    @Override
    public void setPlacedBy(World level, BlockPos pos, BlockState state, @Nullable LivingEntity livingEntity, ItemStack stack) {
        super.setPlacedBy(level, pos, state, livingEntity, stack);
        if (stack.hasTag()) {
            if (stack.getTag().contains("Tile")) {
                TileEntity entity = level.getBlockEntity(pos);
                if (entity instanceof ControllableDrawerTile) {
                    ControllableDrawerTile<?> tile = (ControllableDrawerTile<?>) entity;
                    entity.load(state, stack.getTag().getCompound("Tile"));
                    tile.markForUpdate();
                }
            }
            if (stack.getTag().contains("Locked")) {
                TileEntity entity = level.getBlockEntity(pos);
                if (entity instanceof ControllableDrawerTile) {
                    ControllableDrawerTile<?> tile = (ControllableDrawerTile<?>) entity;
                    tile.setLocked(stack.getTag().getBoolean("Locked"));
                }
            }
        }
    }

    @Override
    public void registerRecipe(Consumer<IFinishedRecipe> consumer) {
        TitaniumShapedRecipeBuilder.shapedRecipe(this)
                .pattern("SSS").pattern("PDP").pattern("SIS")
                .define('S', Blocks.STONE)
                .define('P', Blocks.PISTON)
                .define('D', StorageTags.DRAWER)
                .define('I', Tags.Items.INGOTS_IRON)
                .save(consumer);
    }

    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            TileUtil.getTileEntity(worldIn, pos, CompactingDrawerTile.class).ifPresent(tile -> {
                if (tile.getControllerPos() != null) {
                    TileUtil.getTileEntity(worldIn, tile.getControllerPos(), DrawerControllerTile.class).ifPresent(drawerControllerTile -> {
                        drawerControllerTile.addConnectedDrawers(LinkingToolItem.ActionMode.REMOVE, pos);
                    });
                }
            });
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }


    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader level, BlockPos pos, @Nullable Direction direction) {
        return true;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState p_60483_, IBlockReader blockGetter, BlockPos blockPos, Direction p_60486_) {
        ItemControllableDrawerTile tile = TileUtil.getTileEntity(blockGetter, blockPos, ItemControllableDrawerTile.class).orElse(null);
        if (tile != null) {
            for (int i = 0; i < tile.getUtilityUpgrades().getSlots(); i++) {
                ItemStack stack = tile.getUtilityUpgrades().getStackInSlot(i);
                if (stack.getItem().equals(FunctionalItems.REDSTONE_UPGRADE.get())) {
                    int redstoneSlot = stack.getOrCreateTag().getInt("Slot");
                    if (redstoneSlot < tile.getStorage().getSlots()) {
                        int amount = tile.getStorage().getStackInSlot(redstoneSlot).getCount() * 14 / tile.getStorage().getSlotLimit(redstoneSlot);
                        return amount + (amount > 0 ? 1 : 0);
                    }
                }
            }
        }
        return 0;
    }
}
