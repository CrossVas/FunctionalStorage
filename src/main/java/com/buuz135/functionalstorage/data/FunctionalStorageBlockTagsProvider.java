package com.buuz135.functionalstorage.data;

import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;

public class FunctionalStorageBlockTagsProvider extends BlockTagsProvider {

    public FunctionalStorageBlockTagsProvider(DataGenerator p_126530_, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(p_126530_, modId, existingFileHelper);
    }

    @Override
    protected void addTags() {
//        Builder<Block> tTagAppender = this.tag(BlockTags.MINEABLE_WITH_AXE);
//        for (DrawerType drawerType : FunctionalStorage.DRAWER_TYPES.keySet()) {
//            for (RegistryObject<Block> blockRegistryObject : FunctionalStorage.DRAWER_TYPES.get(drawerType).stream().map(Pair::getLeft).collect(Collectors.toList())) {
//                tTagAppender.add(blockRegistryObject.get());
//            }
//        }
//        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
//                .add(FunctionalStorage.COMPACTING_DRAWER.getLeft().get())
//                .add(FunctionalStorage.DRAWER_CONTROLLER.getLeft().get())
//                .add(FunctionalStorage.ARMORY_CABINET.getLeft().get())
//                .add(FunctionalStorage.ENDER_DRAWER.getLeft().get())
//                .add(FunctionalStorage.FRAMED_COMPACTING_DRAWER.getLeft().get())
//                .add(FunctionalStorage.FLUID_DRAWER_1.getLeft().get())
//                .add(FunctionalStorage.FLUID_DRAWER_2.getLeft().get())
//                .add(FunctionalStorage.FLUID_DRAWER_4.getLeft().get())
//                .add(FunctionalStorage.CONTROLLER_EXTENSION.getLeft().get())
//                .add(FunctionalStorage.SIMPLE_COMPACTING_DRAWER.getLeft().get())
//        ;
    }
}
