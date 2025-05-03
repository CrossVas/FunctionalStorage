package com.buuz135.functionalstorage.util;

import com.buuz135.functionalstorage.FunctionalStorage;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;

public class StorageTags {

    public static ITag.INamedTag<Item> DRAWER = ItemTags.createOptional(new ResourceLocation(FunctionalStorage.MOD_ID, "drawer"));
    public static ITag.INamedTag<Item> IGNORE_CRAFTING_CHECK = ItemTags.createOptional(new ResourceLocation(FunctionalStorage.MOD_ID, "ignore_crafting_check"));

}
