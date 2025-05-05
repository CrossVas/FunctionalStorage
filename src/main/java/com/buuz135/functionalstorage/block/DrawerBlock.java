package com.buuz135.functionalstorage.block;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.DrawerControllerTile;
import com.buuz135.functionalstorage.block.tile.DrawerTile;
import com.buuz135.functionalstorage.block.tile.ItemControllableDrawerTile;
import com.buuz135.functionalstorage.init.FunctionalItems;
import com.buuz135.functionalstorage.inventory.item.DrawerCapabilityProvider;
import com.buuz135.functionalstorage.item.LinkingToolItem;
import com.buuz135.functionalstorage.util.DrawerType;
import com.buuz135.functionalstorage.util.IWoodType;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.hrznstudio.titanium.api.IFactory;
import com.hrznstudio.titanium.block.RotatableBlock;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import com.hrznstudio.titanium.util.RayTraceUtils;
import com.hrznstudio.titanium.util.TileUtil;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.*;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class DrawerBlock extends RotatableBlock<DrawerTile> {

    public static HashMap<DrawerType, Multimap<Direction, VoxelShape>> CACHED_SHAPES = new HashMap<>();

    public static BooleanProperty LOCKED = BooleanProperty.create("locked");

    static {
        CACHED_SHAPES.computeIfAbsent(DrawerType.X_1, type1 -> MultimapBuilder.hashKeys().arrayListValues().build())
                .put(Direction.NORTH, VoxelShapes.box(1 / 16D, 1 / 16D, 0, 15 / 16D, 15 / 16D, 1 / 16D));
        CACHED_SHAPES.computeIfAbsent(DrawerType.X_1, type1 -> MultimapBuilder.hashKeys().arrayListValues().build())
                .put(Direction.SOUTH, VoxelShapes.box(1 / 16D, 1 / 16D, 15 / 16D, 15 / 16D, 15 / 16D, 1));
        CACHED_SHAPES.computeIfAbsent(DrawerType.X_1, type1 -> MultimapBuilder.hashKeys().arrayListValues().build())
                .put(Direction.WEST, VoxelShapes.box(0, 1 / 16D, 1 / 16D, 1 / 16D, 15 / 16D, 15 / 16D));
        CACHED_SHAPES.computeIfAbsent(DrawerType.X_1, type1 -> MultimapBuilder.hashKeys().arrayListValues().build())
                .put(Direction.EAST, VoxelShapes.box(15 / 16D, 1 / 16D, 1 / 16D, 1, 15 / 16D, 15 / 16D));
        for (Direction direction : CACHED_SHAPES.get(DrawerType.X_1).keySet()) {
            for (VoxelShape voxelShape : CACHED_SHAPES.get(DrawerType.X_1).get(direction)) {
                AxisAlignedBB bounding = voxelShape.toAabbs().get(0);
                CACHED_SHAPES.computeIfAbsent(DrawerType.X_2, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                        put(direction, VoxelShapes.box(bounding.minX, bounding.minY, bounding.minZ, bounding.maxX, 7 / 16D, bounding.maxZ));
                CACHED_SHAPES.computeIfAbsent(DrawerType.X_2, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                        put(direction, VoxelShapes.box(bounding.minX, 9 / 16D, bounding.minZ, bounding.maxX, bounding.maxY, bounding.maxZ));
            }
        }
        for (Direction direction : CACHED_SHAPES.get(DrawerType.X_2).keySet()) {
            for (VoxelShape voxelShape : CACHED_SHAPES.get(DrawerType.X_2).get(direction)) {
                AxisAlignedBB bounding = voxelShape.toAabbs().get(0);
                if (direction == Direction.SOUTH) {
                    CACHED_SHAPES.computeIfAbsent(DrawerType.X_4, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, VoxelShapes.box(9 / 16D, bounding.minY, bounding.minZ, bounding.maxX, bounding.maxY, bounding.maxZ));
                    CACHED_SHAPES.computeIfAbsent(DrawerType.X_4, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, VoxelShapes.box(bounding.minX, bounding.minY, bounding.minZ, 7 / 16D, bounding.maxY, bounding.maxZ));
                } else if (direction == Direction.NORTH) {
                    CACHED_SHAPES.computeIfAbsent(DrawerType.X_4, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, VoxelShapes.box(bounding.minX, bounding.minY, bounding.minZ, 7 / 16D, bounding.maxY, bounding.maxZ));
                    CACHED_SHAPES.computeIfAbsent(DrawerType.X_4, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, VoxelShapes.box(9 / 16D, bounding.minY, bounding.minZ, bounding.maxX, bounding.maxY, bounding.maxZ));
                } else if (direction == Direction.EAST) {
                    CACHED_SHAPES.computeIfAbsent(DrawerType.X_4, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, VoxelShapes.box(bounding.minX, bounding.minY, bounding.minZ, bounding.maxX, bounding.maxY, 7 / 16D));
                    CACHED_SHAPES.computeIfAbsent(DrawerType.X_4, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, VoxelShapes.box(bounding.minX, bounding.minY, 9 / 16D, bounding.maxX, bounding.maxY, bounding.maxZ));
                } else {
                    CACHED_SHAPES.computeIfAbsent(DrawerType.X_4, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, VoxelShapes.box(bounding.minX, bounding.minY, 9 / 16D, bounding.maxX, bounding.maxY, bounding.maxZ));
                    CACHED_SHAPES.computeIfAbsent(DrawerType.X_4, type1 -> MultimapBuilder.hashKeys().arrayListValues().build()).
                            put(direction, VoxelShapes.box(bounding.minX, bounding.minY, bounding.minZ, bounding.maxX, bounding.maxY, 7 / 16D));
                }
            }
        }
    }

    private final DrawerType type;
    private final IWoodType woodType;

    public DrawerBlock(IWoodType woodType, DrawerType type, AbstractBlock.Properties properties) {
        super(properties, DrawerTile.class);
        this.setRegistryName(woodType.getName() + "_" + type.getSlots());
        this.woodType = woodType;
        this.type = type;
        setItemGroup(FunctionalStorage.TAB);
        registerDefaultState(defaultBlockState().setValue(RotatableBlock.FACING_HORIZONTAL, Direction.NORTH).setValue(LOCKED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LOCKED);
    }

    @Nonnull
    @Override
    public RotationType getRotationType() {
        return RotationType.FOUR_WAY;
    }

    @Override
    public IFactory<DrawerTile> getTileEntityFactory() {
        return () -> new DrawerTile(this, type, woodType);
    }

    @Override
    public IFactory<BlockItem> getItemBlockFactory() {
        return () -> (BlockItem) new DrawerItem(this, new Item.Properties().tab(FunctionalStorage.TAB)).setRegistryName(Objects.requireNonNull(this.getRegistryName()));
    }

    @Override
    public List<VoxelShape> getBoundingBoxes(BlockState state, IBlockReader source, BlockPos pos) {
        return getShapes(state, source, pos, this.type);
    }

    private static List<VoxelShape> getShapes(BlockState state, IBlockReader source, BlockPos pos, DrawerType type) {
        List<VoxelShape> boxes = new ArrayList<>();
        CACHED_SHAPES.get(type).get(state.getValue(RotatableBlock.FACING_HORIZONTAL)).forEach(boxes::add);
        VoxelShape total = VoxelShapes.block();
        boxes.add(total);
        return boxes;
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
        return TileUtil.getTileEntity(worldIn, pos, DrawerTile.class).map(drawerTile -> drawerTile.onSlotActivated(player, hand, ray.getDirection(), ray.getLocation().x, ray.getLocation().y, ray.getLocation().z, getHit(state, worldIn, pos, player))).orElse(ActionResultType.PASS);
    }

    @Override
    public void attack(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
        if (worldIn.isClientSide()) return;
        TileUtil.getTileEntity(worldIn, pos, DrawerTile.class)
                    .ifPresent(drawerTile -> drawerTile.onClicked(player, getHit(state, worldIn, pos, player)));
    }

    public int getHit(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
        RayTraceResult result = RayTraceUtils.rayTraceSimple(worldIn, player, 32, 0);
        if (result instanceof BlockRayTraceResult) {
            VoxelShape hit = RayTraceUtils.rayTraceVoxelShape((BlockRayTraceResult) result, worldIn, player, 32, 0);
            if (hit != null) {
                if (hit.equals(VoxelShapes.block())) return -1;
                List<VoxelShape> shapes = new ArrayList<>();
                shapes.addAll(CACHED_SHAPES.get(type).get(state.getValue(RotatableBlock.FACING_HORIZONTAL)));
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
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        NonNullList<ItemStack> stacks = NonNullList.create();
        ItemStack stack = new ItemStack(this);
        TileEntity drawerTile = builder.getOptionalParameter(LootParameters.BLOCK_ENTITY);
        boolean locked = state.getValue(LOCKED);
        if (drawerTile instanceof DrawerTile) {
            DrawerTile tile = (DrawerTile) drawerTile;
            if (!tile.isEverythingEmpty() || locked) {
                stack.getOrCreateTag().put("Tile", tile.saveWithoutMetadata());
            }
            if (locked) {
                stack.getOrCreateTag().putBoolean("Locked", true);
            }
        }
        stacks.add(stack);
        return stacks;
    }

    @Override
    public void setPlacedBy(World level, BlockPos pos, BlockState state, @Nullable LivingEntity livingEntity, ItemStack stack) {
        super.setPlacedBy(level, pos, state, livingEntity, stack);
        TileEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof DrawerTile) {
            DrawerTile drawerTile = (DrawerTile) blockEntity;
            if (stack.hasTag()) {
                CompoundNBT tag = stack.getTag();
                if (tag.contains("Tile")) {
                    drawerTile.load(tag.getCompound("Tile"));
                    drawerTile.markForUpdate();
                }
                if (tag.contains("Locked")) {
                    drawerTile.setLocked(tag.getBoolean("Locked"));
                }
            }
        }
    }

    @Override
    public NonNullList<ItemStack> getDynamicDrops(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        return NonNullList.create();
    }

    @Override
    public void registerRecipe(Consumer<IFinishedRecipe> consumer) {
        if (type == DrawerType.X_1) {
            TitaniumShapedRecipeBuilder.shapedRecipe(this)
                    .pattern("PPP").pattern("PCP").pattern("PPP")
                    .define('P', woodType.getPlanks())
                    .define('C', Tags.Items.CHESTS_WOODEN)
                    .save(consumer);
        }
        if (type == DrawerType.X_2) {
            TitaniumShapedRecipeBuilder.shapedRecipe(this, 2)
                    .pattern("PCP").pattern("PPP").pattern("PCP")
                    .define('P', woodType.getPlanks())
                    .define('C', Tags.Items.CHESTS_WOODEN)
                    .save(consumer);
        }
        if (type == DrawerType.X_4) {
            TitaniumShapedRecipeBuilder.shapedRecipe(this, 4)
                    .pattern("CPC").pattern("PPP").pattern("CPC")
                    .define('P', woodType.getPlanks())
                    .define('C', Tags.Items.CHESTS_WOODEN)
                    .save(consumer);
        }
    }

    public DrawerType getType() {
        return type;
    }

    public IWoodType getWoodType() {
        return woodType;
    }

    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            TileUtil.getTileEntity(worldIn, pos, DrawerTile.class).ifPresent(tile -> {
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
    public void appendHoverText(ItemStack stack, @Nullable IBlockReader reader, List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.appendHoverText(stack, reader, tooltip, flag);
        if (stack.hasTag() && stack.getTag().contains("Tile")) {
            IFormattableTextComponent text = new TranslationTextComponent("drawer.block.contents");
            tooltip.add(text.withStyle(TextFormatting.GRAY));
            tooltip.add(new StringTextComponent(""));
            tooltip.add(new StringTextComponent(""));
        }
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
    public int getSignal(BlockState state, IBlockReader blockGetter, BlockPos blockPos, Direction direction) {
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

    public static class DrawerItem extends BlockItem {

        private final DrawerBlock drawerBlock;

        public DrawerItem(DrawerBlock drawerBlock, Properties properties) {
            super(drawerBlock, properties);
            this.drawerBlock = drawerBlock;
        }

        @Nullable
        @Override
        public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
            return new DrawerCapabilityProvider(stack, this.drawerBlock.getType());
        }
    }
}
