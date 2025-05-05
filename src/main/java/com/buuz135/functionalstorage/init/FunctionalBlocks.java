package com.buuz135.functionalstorage.init;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.*;
import com.buuz135.functionalstorage.util.DrawerType;
import com.buuz135.functionalstorage.util.DrawerWoodType;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class FunctionalBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, FunctionalStorage.MOD_ID);
    public static final AbstractBlock.Properties STONE_PROPS = AbstractBlock.Properties.copy(Blocks.STONE_BRICKS);

    // oak
    public static final DrawerBlock OAK_1 = new DrawerBlock(DrawerWoodType.OAK, DrawerType.X_1, woodProps(DrawerWoodType.OAK::getPlanks));
    public static final DrawerBlock OAK_2 = new DrawerBlock(DrawerWoodType.OAK, DrawerType.X_2, woodProps(DrawerWoodType.OAK::getPlanks));
    public static final DrawerBlock OAK_4 = new DrawerBlock(DrawerWoodType.OAK, DrawerType.X_4, woodProps(DrawerWoodType.OAK::getPlanks));

    // spruce
    public static final DrawerBlock SPRUCE_1 = new DrawerBlock(DrawerWoodType.SPRUCE, DrawerType.X_1, woodProps(DrawerWoodType.SPRUCE::getPlanks));
    public static final DrawerBlock SPRUCE_2 = new DrawerBlock(DrawerWoodType.SPRUCE, DrawerType.X_2, woodProps(DrawerWoodType.SPRUCE::getPlanks));
    public static final DrawerBlock SPRUCE_4 = new DrawerBlock(DrawerWoodType.SPRUCE, DrawerType.X_4, woodProps(DrawerWoodType.SPRUCE::getPlanks));

    // birch
    public static final DrawerBlock BIRCH_1 = new DrawerBlock(DrawerWoodType.BIRCH, DrawerType.X_1, woodProps(DrawerWoodType.BIRCH::getPlanks));
    public static final DrawerBlock BIRCH_2 = new DrawerBlock(DrawerWoodType.BIRCH, DrawerType.X_2, woodProps(DrawerWoodType.BIRCH::getPlanks));
    public static final DrawerBlock BIRCH_4 = new DrawerBlock(DrawerWoodType.BIRCH, DrawerType.X_4, woodProps(DrawerWoodType.BIRCH::getPlanks));

    // jungle
    public static final DrawerBlock JUNGLE_1 = new DrawerBlock(DrawerWoodType.JUNGLE, DrawerType.X_1, woodProps(DrawerWoodType.JUNGLE::getPlanks));
    public static final DrawerBlock JUNGLE_2 = new DrawerBlock(DrawerWoodType.JUNGLE, DrawerType.X_2, woodProps(DrawerWoodType.JUNGLE::getPlanks));
    public static final DrawerBlock JUNGLE_4 = new DrawerBlock(DrawerWoodType.JUNGLE, DrawerType.X_4, woodProps(DrawerWoodType.JUNGLE::getPlanks));

    // acacia
    public static final DrawerBlock ACACIA_1 = new DrawerBlock(DrawerWoodType.ACACIA, DrawerType.X_1, woodProps(DrawerWoodType.ACACIA::getPlanks));
    public static final DrawerBlock ACACIA_2 = new DrawerBlock(DrawerWoodType.ACACIA, DrawerType.X_2, woodProps(DrawerWoodType.ACACIA::getPlanks));
    public static final DrawerBlock ACACIA_4 = new DrawerBlock(DrawerWoodType.ACACIA, DrawerType.X_4, woodProps(DrawerWoodType.ACACIA::getPlanks));

    // dark_oak
    public static final DrawerBlock DARK_OAK_1 = new DrawerBlock(DrawerWoodType.DARK_OAK, DrawerType.X_1, woodProps(DrawerWoodType.DARK_OAK::getPlanks));
    public static final DrawerBlock DARK_OAK_2 = new DrawerBlock(DrawerWoodType.DARK_OAK, DrawerType.X_2, woodProps(DrawerWoodType.DARK_OAK::getPlanks));
    public static final DrawerBlock DARK_OAK_4 = new DrawerBlock(DrawerWoodType.DARK_OAK, DrawerType.X_4, woodProps(DrawerWoodType.DARK_OAK::getPlanks));

    // crimson
    public static final DrawerBlock CRIMSON_1 = new DrawerBlock(DrawerWoodType.CRIMSON, DrawerType.X_1, woodProps(DrawerWoodType.CRIMSON::getPlanks));
    public static final DrawerBlock CRIMSON_2 = new DrawerBlock(DrawerWoodType.CRIMSON, DrawerType.X_2, woodProps(DrawerWoodType.CRIMSON::getPlanks));
    public static final DrawerBlock CRIMSON_4 = new DrawerBlock(DrawerWoodType.CRIMSON, DrawerType.X_4, woodProps(DrawerWoodType.CRIMSON::getPlanks));

    // warped
    public static final DrawerBlock WARPED_1= new DrawerBlock(DrawerWoodType.WARPED, DrawerType.X_1, woodProps(DrawerWoodType.WARPED::getPlanks));
    public static final DrawerBlock WARPED_2 = new DrawerBlock(DrawerWoodType.WARPED, DrawerType.X_2, woodProps(DrawerWoodType.WARPED::getPlanks));
    public static final DrawerBlock WARPED_4 = new DrawerBlock(DrawerWoodType.WARPED, DrawerType.X_4, woodProps(DrawerWoodType.WARPED::getPlanks));

    // framed
    public static final DrawerBlock FRAMED_1 = new FramedDrawerBlock(DrawerType.X_1);
    public static final DrawerBlock FRAMED_2 = new FramedDrawerBlock(DrawerType.X_2);
    public static final DrawerBlock FRAMED_4 = new FramedDrawerBlock(DrawerType.X_4);
    public static final CompactingFramedDrawerBlock FRAMED_COMPACTING = new CompactingFramedDrawerBlock("compacting_framed_drawer");

    // fluid
    public static final FluidDrawerBlock FLUID_1 = new FluidDrawerBlock(DrawerType.X_1, STONE_PROPS);
    public static final FluidDrawerBlock FLUID_2 = new FluidDrawerBlock(DrawerType.X_2, STONE_PROPS);
    public static final FluidDrawerBlock FLUID_4 = new FluidDrawerBlock(DrawerType.X_4, STONE_PROPS);

    // compacting
    public static final CompactingDrawerBlock COMPACTING = new CompactingDrawerBlock("compacting_drawer", STONE_PROPS);
    public static final SimpleCompactingDrawerBlock SIMPLE_COMPACTING = new SimpleCompactingDrawerBlock("simple_compacting_drawer", STONE_PROPS);

    // ender
    public static final EnderDrawerBlock ENDER = new EnderDrawerBlock();

    // armory cabinet
    public static final ArmoryCabinetBlock ARMORY_CABINET = new ArmoryCabinetBlock();

    // controller
    public static final DrawerControllerBlock CONTROLLER = new DrawerControllerBlock();
    public static final ControllerExtensionBlock CONTROLLER_EXTENSION = new ControllerExtensionBlock();

    public static void initBlocks(IEventBus e) {
        BLOCKS.register(e);
    }

    public static AbstractBlock.Properties woodProps(Supplier<Block> plankSupplier) {
        return AbstractBlock.Properties.copy(plankSupplier.get());
    }

    public static List<Block> DRAWERS = Arrays.asList(
            OAK_1, OAK_2, OAK_4,
            SPRUCE_1, SPRUCE_2, SPRUCE_4,
            BIRCH_1, BIRCH_2, BIRCH_4,
            JUNGLE_1, JUNGLE_2, JUNGLE_4,
            ACACIA_1, ACACIA_2, ACACIA_4,
            DARK_OAK_1, DARK_OAK_2, DARK_OAK_4,
            CRIMSON_1, CRIMSON_2, CRIMSON_4,
            WARPED_1, WARPED_2, WARPED_4,
            FRAMED_1, FRAMED_2, FRAMED_4
    );

    public static List<Block> FRAMED = Arrays.asList(FRAMED_1, FRAMED_2, FRAMED_4);
}
