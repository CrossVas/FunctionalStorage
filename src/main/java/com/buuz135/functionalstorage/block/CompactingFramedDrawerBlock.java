package com.buuz135.functionalstorage.block;

import com.buuz135.functionalstorage.block.tile.CompactingDrawerTile;
import com.buuz135.functionalstorage.block.tile.CompactingFramedDrawerTile;
import com.buuz135.functionalstorage.block.tile.FramedDrawerTile;
import com.buuz135.functionalstorage.init.FunctionalBlocks;
import com.hrznstudio.titanium.api.IFactory;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import com.hrznstudio.titanium.util.TileUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class CompactingFramedDrawerBlock extends CompactingDrawerBlock {

    public CompactingFramedDrawerBlock(String name) {
        super(name, Properties.copy(Blocks.STONE).noOcclusion().isViewBlocking((p_61036_, p_61037_, p_61038_) -> false));
    }

    @Override
    public IFactory<CompactingDrawerTile> getTileEntityFactory() {
        return () -> new CompactingFramedDrawerTile(this);
    }

    @Override
    public void setPlacedBy(World level, BlockPos pos, BlockState state, @Nullable LivingEntity livingEntity, ItemStack stack) {
        super.setPlacedBy(level, pos, state, livingEntity, stack);
        TileUtil.getTileEntity(level, pos, CompactingFramedDrawerTile.class).ifPresent(framedDrawerTile -> {
            framedDrawerTile.setFramedDrawerModelData(FramedDrawerBlock.getDrawerModelData(stack));
        });
    }


    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        NonNullList<ItemStack> stacks = NonNullList.create();
        ItemStack stack = new ItemStack(this);
        TileEntity drawerTile = builder.getOptionalParameter(LootParameters.BLOCK_ENTITY);
        if (drawerTile instanceof CompactingFramedDrawerTile) {
            CompactingFramedDrawerTile framedDrawerTile = (CompactingFramedDrawerTile) drawerTile;
            if (!framedDrawerTile.isEverythingEmpty()) {
                stack.getOrCreateTag().put("Tile", framedDrawerTile.saveWithoutMetadata());
            }
            if (framedDrawerTile.getFramedDrawerModelData() != null) {
                stack.getOrCreateTag().put("Style", framedDrawerTile.getFramedDrawerModelData().serializeNBT());
            }
            if (framedDrawerTile.isLocked()) {
                stack.getOrCreateTag().putBoolean("Locked", framedDrawerTile.isLocked());
            }
        }
        stacks.add(stack);
        return stacks;
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader level, BlockPos pos, PlayerEntity player) {
        TileEntity entity = level.getBlockEntity(pos);
        if (entity instanceof FramedDrawerTile && ((FramedDrawerTile) entity).getFramedDrawerModelData() != null && !((FramedDrawerTile) entity).getFramedDrawerModelData().getDesign().isEmpty()) {
            ItemStack stack = new ItemStack(this);
            stack.getOrCreateTag().put("Style", ((FramedDrawerTile) entity).getFramedDrawerModelData().serializeNBT());
            return stack;
        }
        return super.getPickBlock(state, target, level, pos, player);
    }

    @Override
    public void registerRecipe(Consumer<IFinishedRecipe> consumer) {
        TitaniumShapedRecipeBuilder.shapedRecipe(this)
                .pattern("SSS").pattern("PDP").pattern("SIS")
                .define('S', Items.IRON_NUGGET)
                .define('P', Blocks.PISTON)
                .define('D', Ingredient.of(FunctionalBlocks.FRAMED.stream().map(ItemStack::new)))
                .define('I', Tags.Items.INGOTS_IRON)
                .save(consumer);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable IBlockReader reader, List<ITextComponent> components, ITooltipFlag flag) {
        components.add(new TranslationTextComponent("frameddrawer.use").withStyle(TextFormatting.GRAY));
        super.appendHoverText(stack, reader, components, flag);
    }
}
