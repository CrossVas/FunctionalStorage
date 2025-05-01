package com.buuz135.functionalstorage.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.buuz135.functionalstorage.FunctionalStorage.MOD_ID;

public class DrawerlessWoodIngredient extends Ingredient {

    public static WoodlessIngredientSerializer SERIALIZER = new WoodlessIngredientSerializer();
    public static ResourceLocation NAME = new ResourceLocation(MOD_ID, "woodless");

    private List<Item> woodless;

    public DrawerlessWoodIngredient() {
        super(Stream.empty());
    }

    @Override
    public ItemStack[] getItems() {
        return getWoods().stream().map(ItemStack::new).toArray(ItemStack[]::new);
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        return getWoods().contains(stack.getItem());
    }

    @Override
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return SERIALIZER;
    }

    private List<Item> getWoods(){
        if (woodless == null){
            woodless = ForgeRegistries.ITEMS.getEntries().stream().map(Map.Entry::getValue)
                    .filter(item -> item.is(ItemTags.PLANKS) && !ForgeRegistries.ITEMS.getKey(item).getNamespace().equalsIgnoreCase("minecraft")).collect(Collectors.toList());
            if (woodless.isEmpty()){
                woodless.add(Items.OAK_PLANKS);
            }
        }
        return woodless;
    }

    @Override
    public JsonElement toJson() {
        JsonObject element = new JsonObject();
        element.addProperty("type", NAME.toString());
        return element;
    }

    @Override
    protected void invalidate() {
        super.invalidate();
        this.woodless = null;
    }

    public static class WoodlessIngredientSerializer implements IIngredientSerializer<Ingredient>{

        @Override
        public Ingredient parse(PacketBuffer buffer) {
            return new DrawerlessWoodIngredient();
        }

        @Override
        public Ingredient parse(JsonObject json) {
            return new DrawerlessWoodIngredient();
        }

        @Override
        public void write(PacketBuffer buffer, Ingredient ingredient) {

        }
    }
}
