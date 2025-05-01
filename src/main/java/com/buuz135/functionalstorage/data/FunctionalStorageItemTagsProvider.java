package com.buuz135.functionalstorage.data;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.util.StorageTags;
import net.minecraft.block.Block;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;
import net.minecraft.data.TagsProvider;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.RegistryObject;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.stream.Collectors;

public class FunctionalStorageItemTagsProvider extends ItemTagsProvider {

    public FunctionalStorageItemTagsProvider(DataGenerator generator, BlockTagsProvider provider, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator, provider, modId, existingFileHelper);
    }

    @Override
    protected void addTags() {
        TagsProvider.Builder<Item> tTagAppender = this.tag(StorageTags.DRAWER);
        for (FunctionalStorage.DrawerType drawerType : FunctionalStorage.DRAWER_TYPES.keySet()) {
            for (RegistryObject<Block> blockRegistryObject : FunctionalStorage.DRAWER_TYPES.get(drawerType).stream().map(Pair::getLeft).collect(Collectors.toList())) {
                tTagAppender.add(blockRegistryObject.get().asItem());
            }
        }
        this.tag(StorageTags.IGNORE_CRAFTING_CHECK)
                .add(Items.CLAY, Items.CLAY_BALL)
                .add(Items.GLOWSTONE, Items.GLOWSTONE_DUST)
                .add(Items.MELON, Items.MELON_SLICE)
                .add(Items.QUARTZ, Items.QUARTZ_BLOCK)
                .add(Items.ICE, Items.BLUE_ICE, Items.PACKED_ICE)
        ;
    }
}
