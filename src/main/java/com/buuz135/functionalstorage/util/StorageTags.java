package com.buuz135.functionalstorage.util;

import com.buuz135.functionalstorage.FunctionalStorage;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class StorageTags {

    public static Map<ResourceLocation, ITag.INamedTag<Item>> tagCache = new HashMap<>();

    public static ITag.INamedTag<Item> DRAWER = getTag("drawer");
    public static ITag.INamedTag<Item> IGNORE_CRAFTING_CHECK = getTag("ignore_crafting_check");

    public static ITag.INamedTag<Item> getTag(String name) {
        return getTag(new ResourceLocation(FunctionalStorage.MOD_ID, name));
    }

    public static ITag.INamedTag<Item> getTag(ResourceLocation resourceLocation) {
        if (!tagCache.containsKey(resourceLocation)) {
            tagCache.put(resourceLocation, ItemTags.createOptional(resourceLocation));
        }
        return tagCache.get(resourceLocation);
    }
}
