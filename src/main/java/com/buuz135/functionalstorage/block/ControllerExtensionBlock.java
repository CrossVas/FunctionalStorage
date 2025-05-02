package com.buuz135.functionalstorage.block;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.ControllerExtensionTile;
import com.buuz135.functionalstorage.block.tile.DrawerControllerTile;
import com.buuz135.functionalstorage.item.LinkingToolItem;
import com.buuz135.functionalstorage.util.StorageTags;
import com.hrznstudio.titanium.api.IFactory;
import com.hrznstudio.titanium.block.RotatableBlock;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import com.hrznstudio.titanium.util.TileUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class ControllerExtensionBlock extends RotatableBlock<ControllerExtensionTile> {

    public ControllerExtensionBlock() {
        super(Properties.copy(Blocks.IRON_BLOCK), ControllerExtensionTile.class);
        // name: "controller_extension"
        setItemGroup(FunctionalStorage.TAB);
        registerDefaultState(defaultBlockState().setValue(RotatableBlock.FACING_HORIZONTAL, Direction.NORTH).setValue(DrawerBlock.LOCKED, false));
    }

    @Override
    public IFactory<ControllerExtensionTile> getTileEntityFactory() {
        return () -> new ControllerExtensionTile(this, (TileEntityType<ControllerExtensionTile>) FunctionalStorage.CONTROLLER_EXTENSION.getRight().get(), p_155268_, p_155269_);
    }

    @Nonnull
    @Override
    public RotationType getRotationType() {
        return RotationType.FOUR_WAY;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(DrawerBlock.LOCKED);
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult ray) {
        return TileUtil.getTileEntity(worldIn, pos, ControllerExtensionTile.class).map(drawerTile -> drawerTile.onSlotActivated(player, hand, ray.getDirection(), ray.getLocation().x, ray.getLocation().y, ray.getLocation().z)).orElse(InteractionResult.PASS);
    }

    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            TileUtil.getTileEntity(worldIn, pos, ControllerExtensionTile.class).ifPresent(tile -> {
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
    public void registerRecipe(Consumer<IFinishedRecipe> consumer) {
        TitaniumShapedRecipeBuilder.shapedRecipe(FunctionalStorage.CONTROLLER_EXTENSION.getLeft().get())
                .pattern("IBI").pattern("CDC").pattern("IBI")
                .define('I', Tags.Items.STONE)
                .define('B', Tags.Items.STORAGE_BLOCKS_QUARTZ)
                .define('C', StorageTags.DRAWER)
                .define('D', Items.REPEATER)
                .save(consumer);
    }
}
