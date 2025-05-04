package com.buuz135.functionalstorage.block;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.DrawerControllerTile;
import com.buuz135.functionalstorage.block.tile.EnderDrawerTile;
import com.buuz135.functionalstorage.block.tile.ItemControllableDrawerTile;
import com.buuz135.functionalstorage.init.FunctionalItems;
import com.buuz135.functionalstorage.item.LinkingToolItem;
import com.buuz135.functionalstorage.util.DrawerType;
import com.hrznstudio.titanium.api.IFactory;
import com.hrznstudio.titanium.block.RotatableBlock;
import com.hrznstudio.titanium.datagenerator.loot.block.BasicBlockLootTables;
import com.hrznstudio.titanium.util.RayTraceUtils;
import com.hrznstudio.titanium.util.TileUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.buuz135.functionalstorage.block.DrawerBlock.LOCKED;

public class EnderDrawerBlock extends RotatableBlock<EnderDrawerTile> {

    public EnderDrawerBlock() {
        super(Properties.copy(Blocks.ENDER_CHEST), EnderDrawerTile.class);
        this.setRegistryName("ender_drawer");
        setItemGroup(FunctionalStorage.TAB);
        registerDefaultState(defaultBlockState().setValue(RotatableBlock.FACING_HORIZONTAL, Direction.NORTH).setValue(LOCKED, false));
    }

    public static HashMap<String, List<ItemStack>> FREQUENCY_LOOK = new HashMap<>();

    public static List<ItemStack> getFrequencyDisplay(String string) {
        return FREQUENCY_LOOK.computeIfAbsent(string, s -> {
            List<Item> minecraftItems = ForgeRegistries.ITEMS.getValues().stream().filter(item -> item != Items.AIR && ForgeRegistries.ITEMS.getKey(item).getNamespace().equals("minecraft") && !(item instanceof BlockItem)).collect(Collectors.toList());
            return Arrays.stream(string.split("-")).map(s1 -> new ItemStack(minecraftItems.get(Math.abs(s1.hashCode()) % minecraftItems.size()))).collect(Collectors.toList());
        });
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
    public IFactory<EnderDrawerTile> getTileEntityFactory() {
        return () -> new EnderDrawerTile(this);
    }

    @Override
    public List<VoxelShape> getBoundingBoxes(BlockState state, IBlockReader source, BlockPos pos) {
        return getShapes(state, source, pos);
    }

    private static List<VoxelShape> getShapes(BlockState state, IBlockReader source, BlockPos pos) {
        List<VoxelShape> boxes = new ArrayList<>();
        DrawerBlock.CACHED_SHAPES.get(DrawerType.X_1).get(state.getValue(RotatableBlock.FACING_HORIZONTAL)).forEach(boxes::add);
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
        return TileUtil.getTileEntity(worldIn, pos, EnderDrawerTile.class).map(drawerTile -> drawerTile.onSlotActivated(player, hand, ray.getDirection(), ray.getLocation().x, ray.getLocation().y, ray.getLocation().z, getHit(state, worldIn, pos, player))).orElse(ActionResultType.PASS);
    }

    @Override
    public void attack(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
        TileUtil.getTileEntity(worldIn, pos, EnderDrawerTile.class).ifPresent(drawerTile -> drawerTile.onClicked(player, getHit(state, worldIn, pos, player)));
    }

    public int getHit(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
        RayTraceResult result = RayTraceUtils.rayTraceSimple(worldIn, player, 32, 0);
        if (result instanceof BlockRayTraceResult) {
            VoxelShape hit = RayTraceUtils.rayTraceVoxelShape((BlockRayTraceResult) result, worldIn, player, 32, 0);
            if (hit != null) {
                if (hit.equals(VoxelShapes.block())) return -1;
                List<VoxelShape> shapes = new ArrayList<>(DrawerBlock.CACHED_SHAPES.get(DrawerType.X_1).get(state.getValue(RotatableBlock.FACING_HORIZONTAL)));
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
        //nbtBuilder.copy("frequency",  "BlockEntityTag.frequency");
        return blockLootTables.droppingNothing();
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        NonNullList<ItemStack> stacks = NonNullList.create();
        ItemStack stack = new ItemStack(this);
        TileEntity drawerTile = builder.getOptionalParameter(LootParameters.BLOCK_ENTITY);
        if (drawerTile instanceof EnderDrawerTile) {
            EnderDrawerTile tile = (EnderDrawerTile) drawerTile;
            if (!tile.isEverythingEmpty()) {
                stack.getOrCreateTag().put("Tile", tile.saveWithoutMetadata());
            }
        }
        stacks.add(stack);
        return stacks;
    }

    @Override
    public void setPlacedBy(World level, BlockPos pos, BlockState state, @Nullable LivingEntity livingEntity, ItemStack stack) {
        super.setPlacedBy(level, pos, state, livingEntity, stack);
        if (stack.hasTag()) {
            if (stack.getTag().contains("Tile")) {
                TileEntity entity = level.getBlockEntity(pos);
                if (entity instanceof ControllableDrawerTile) {
                    ControllableDrawerTile<?> tile = (ControllableDrawerTile<?>) entity;
                    tile.load(stack.getTag().getCompound("Tile"));
                    tile.markForUpdate();
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

    }

    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            TileUtil.getTileEntity(worldIn, pos, EnderDrawerTile.class).ifPresent(tile -> {
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
        if (stack.hasTag()) {
            tooltip.add(new TranslationTextComponent("linkingtool.ender.frequency").withStyle(TextFormatting.GRAY));
            tooltip.add(new StringTextComponent(""));
            tooltip.add(new StringTextComponent(""));
        }

    }

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader level, BlockPos pos, @Nullable Direction direction) {
        return true;
    }

    @Override
    public boolean isSignalSource(BlockState p_60571_) {
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
