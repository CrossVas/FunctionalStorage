package com.buuz135.functionalstorage.data;

import com.buuz135.functionalstorage.init.FunctionalBlocks;
import com.buuz135.functionalstorage.util.StorageTags;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;
import net.minecraft.data.TagsProvider;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;

public class FunctionalStorageItemTagsProvider extends ItemTagsProvider {

    public FunctionalStorageItemTagsProvider(DataGenerator generator, BlockTagsProvider provider, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator, provider, modId, existingFileHelper);
    }

    @Override
    protected void addTags() {
        TagsProvider.Builder<Item> tTagAppender = this.tag(StorageTags.DRAWER);
        FunctionalBlocks.TYPED_DRAWER_BLOCKS.forEach(blockRegistryObject -> {
            tTagAppender.add(blockRegistryObject.asItem());
        });
        this.tag(StorageTags.IGNORE_CRAFTING_CHECK)
                .add(Items.CLAY, Items.CLAY_BALL)
                .add(Items.GLOWSTONE, Items.GLOWSTONE_DUST)
                .add(Items.MELON, Items.MELON_SLICE)
                .add(Items.QUARTZ, Items.QUARTZ_BLOCK)
                .add(Items.ICE, Items.BLUE_ICE, Items.PACKED_ICE)
        ;
    }
}
