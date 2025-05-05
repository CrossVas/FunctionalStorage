package com.buuz135.functionalstorage.block;

import com.buuz135.functionalstorage.block.tile.DrawerTile;
import com.buuz135.functionalstorage.block.tile.FramedDrawerTile;
import com.buuz135.functionalstorage.client.model.FramedDrawerModelData;
import com.buuz135.functionalstorage.util.DrawerType;
import com.buuz135.functionalstorage.util.DrawerWoodType;
import com.hrznstudio.titanium.api.IFactory;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import com.hrznstudio.titanium.util.TileUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class FramedDrawerBlock extends DrawerBlock {

    public FramedDrawerBlock(DrawerType type) {
        super(DrawerWoodType.FRAMED, type, Properties.copy(Blocks.OAK_PLANKS).noOcclusion().isViewBlocking((p_61036_, p_61037_, p_61038_) -> false));
    }

    @Override
    public IFactory<DrawerTile> getTileEntityFactory() {
        return () -> new FramedDrawerTile(this, this.getType());
    }

    @Override
    public void setPlacedBy(World level, BlockPos pos, BlockState state, @Nullable LivingEntity livingEntity, ItemStack stack) {
        super.setPlacedBy(level, pos, state, livingEntity, stack);
        TileUtil.getTileEntity(level, pos, FramedDrawerTile.class).ifPresent(framedDrawerTile -> {
            framedDrawerTile.setFramedDrawerModelData(getDrawerModelData(stack));
        });
    }

    public static FramedDrawerModelData getDrawerModelData(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("Style")) {
            CompoundNBT tag = stack.getTag().getCompound("Style");
            if (tag.isEmpty()) return null;
            HashMap<String, Item> data = new HashMap<>();
            data.put("particle", ForgeRegistries.ITEMS.getValue(new ResourceLocation(tag.getString("particle"))));
            data.put("front", ForgeRegistries.ITEMS.getValue(new ResourceLocation(tag.getString("front"))));
            data.put("side", ForgeRegistries.ITEMS.getValue(new ResourceLocation(tag.getString("side"))));
            data.put("front_divider", ForgeRegistries.ITEMS.getValue(new ResourceLocation(tag.getString("front_divider"))));
            return new FramedDrawerModelData(data);
        }
        return null;
    }

    public static ItemStack fill(ItemStack first, ItemStack second, ItemStack drawer) {
        drawer = ItemHandlerHelper.copyStackWithSize(drawer, 1);
        CompoundNBT style = drawer.getOrCreateTagElement("Style");
        style.putString("particle", ForgeRegistries.ITEMS.getKey(first.getItem()).toString());
        style.putString("side", ForgeRegistries.ITEMS.getKey(first.getItem()).toString());
        style.putString("front", ForgeRegistries.ITEMS.getKey(second.getItem()).toString());
        style.putString("front_divider", ForgeRegistries.ITEMS.getKey(first.getItem()).toString());
        drawer.getOrCreateTag().put("Style", style);
        return drawer;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        NonNullList<ItemStack> stacks = NonNullList.create();
        ItemStack stack = new ItemStack(this);
        TileEntity drawerTile = builder.getOptionalParameter(LootParameters.BLOCK_ENTITY);
        boolean locked = state.getValue(LOCKED);
        if (drawerTile instanceof FramedDrawerTile) {
            FramedDrawerTile framedDrawerTile = (FramedDrawerTile) drawerTile;
            if (!framedDrawerTile.isEverythingEmpty() || locked) {
                stack.getOrCreateTag().put("Tile", drawerTile.save(new CompoundNBT()));
            }
            if (framedDrawerTile.getFramedDrawerModelData() != null) {
                stack.getOrCreateTag().put("Style", framedDrawerTile.getFramedDrawerModelData().serializeNBT());
            }
            if (locked) {
                stack.getOrCreateTag().putBoolean("Locked", true);
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
        if (this.getType() == DrawerType.X_1) {
            TitaniumShapedRecipeBuilder.shapedRecipe(this)
                    .pattern("PPP").pattern("PCP").pattern("PPP")
                    .define('P', Items.IRON_NUGGET)
                    .define('C', Tags.Items.CHESTS_WOODEN)
                    .save(consumer);
        }
        if (this.getType() == DrawerType.X_2) {
            TitaniumShapedRecipeBuilder.shapedRecipe(this, 2)
                    .pattern("PCP").pattern("PPP").pattern("PCP")
                    .define('P', Items.IRON_NUGGET)
                    .define('C', Tags.Items.CHESTS_WOODEN)
                    .save(consumer);

        }
        if (this.getType() == DrawerType.X_4) {
            TitaniumShapedRecipeBuilder.shapedRecipe(this, 4)
                    .pattern("CPC").pattern("PPP").pattern("CPC")
                    .define('P', Items.IRON_NUGGET)
                    .define('C', Tags.Items.CHESTS_WOODEN)
                    .save(consumer);

        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable IBlockReader reader, List<ITextComponent> components, ITooltipFlag flag) {
        components.add(new TranslationTextComponent("frameddrawer.use").withStyle(TextFormatting.GRAY));
        super.appendHoverText(stack, reader, components, flag);
    }
}
