package com.buuz135.functionalstorage.recipe;

import com.buuz135.functionalstorage.block.CompactingDrawerBlock;
import com.buuz135.functionalstorage.block.FramedDrawerBlock;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class FramedDrawerRecipe extends SpecialRecipe {

    public static IRecipeSerializer<FramedDrawerRecipe> SERIALIZER = new SpecialRecipeSerializer<>(FramedDrawerRecipe::new);

    public FramedDrawerRecipe(ResourceLocation idIn) {
        super(idIn);
    }


    public static boolean matches(ItemStack first, ItemStack second, ItemStack drawer) {
        return !first.isEmpty() && first.getItem() instanceof BlockItem && !second.isEmpty() && second.getItem() instanceof BlockItem && !drawer.isEmpty() && drawer.getItem() instanceof BlockItem && ((BlockItem) drawer.getItem()).getBlock() instanceof FramedDrawerBlock;
    }

    public static boolean matchesCompacting(ItemStack first, ItemStack second, ItemStack drawer) {
        return !first.isEmpty() && first.getItem() instanceof BlockItem && !second.isEmpty() && second.getItem() instanceof BlockItem && !drawer.isEmpty() && drawer.getItem() instanceof BlockItem && ((BlockItem) drawer.getItem()).getBlock() instanceof CompactingDrawerBlock;
    }

    @Override
    public boolean matches(CraftingInventory inv, World world) {
        return matches(inv.getItem(0), inv.getItem(1), inv.getItem(2)) || matchesCompacting(inv.getItem(0), inv.getItem(1), inv.getItem(2));
    }

    @Override
    public ItemStack assemble(CraftingInventory inv) {
        if (matches(inv.getItem(0), inv.getItem(1), inv.getItem(2)) || matchesCompacting(inv.getItem(0), inv.getItem(1), inv.getItem(2))){
            return FramedDrawerBlock.fill(inv.getItem(0), inv.getItem(1), inv.getItem(2).copy());
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}
