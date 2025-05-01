package com.buuz135.functionalstorage.client;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.CompactingFramedDrawerBlock;
import com.buuz135.functionalstorage.block.FramedDrawerBlock;
import com.buuz135.functionalstorage.block.tile.FramedDrawerTile;
import com.buuz135.functionalstorage.client.model.FramedDrawerModelData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.Map;

@Mod.EventBusSubscriber(modid = FunctionalStorage.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class FramedColors implements IBlockColor, IItemColor {

    @Override
    public int getColor(BlockState state, @Nullable IBlockDisplayReader level, @Nullable BlockPos pos, int tintIndex) {
        if (level != null && pos != null && tintIndex == 0) {
            TileEntity entity = level.getBlockEntity(pos);
            if (entity instanceof FramedDrawerTile) {
                FramedDrawerTile tile = (FramedDrawerTile) entity;
                FramedDrawerModelData framedDrawerModelData = tile.getFramedDrawerModelData();
                if (framedDrawerModelData != null) {
                    for (Map.Entry<String, Item> entry: framedDrawerModelData.getDesign().entrySet()) {
                        if (entry.getValue() instanceof BlockItem) {
                            BlockItem blockItem = (BlockItem) entry.getValue();
                            BlockState state1 = blockItem.getBlock().defaultBlockState();
                            int color = Minecraft.getInstance().getBlockColors().getColor(state1, level, pos, tintIndex);
                            if (color != -1)
                                return color;
                        }
                    }
                }
            }
        }
        return 0xFFFFFF;
    }

    @Override
    public int getColor(ItemStack itemStack, int tintIndex) {
        if (tintIndex == 0) {
            Item item = itemStack.getItem();
            if (item instanceof BlockItem && (((BlockItem) item).getBlock() instanceof FramedDrawerBlock || ((BlockItem) item).getBlock() instanceof CompactingFramedDrawerBlock)) {
                FramedDrawerModelData framedDrawerModelData = FramedDrawerBlock.getDrawerModelData(itemStack);
                if (framedDrawerModelData != null) {
                    for (Map.Entry<String, Item> entry: framedDrawerModelData.getDesign().entrySet()) {
                        if (entry.getValue() instanceof BlockItem) {
                            int color = Minecraft.getInstance().getItemColors().getColor(itemStack, tintIndex);
                            if (color != -1)
                                return color;
                        }
                    }
                }
            }
        }
        return 0xFFFFFF;
    }

    @SubscribeEvent
    public static void blockColors(ColorHandlerEvent.Block event) {
        Block block1 = Registry.BLOCK.get(new ResourceLocation(FunctionalStorage.MOD_ID, "framed_1"));
        event.getBlockColors().register(new FramedColors(), block1);
        Block block2 = Registry.BLOCK.get(new ResourceLocation(FunctionalStorage.MOD_ID, "framed_2"));
        event.getBlockColors().register(new FramedColors(), block2);
        Block block4 = Registry.BLOCK.get(new ResourceLocation(FunctionalStorage.MOD_ID, "framed_4"));
        event.getBlockColors().register(new FramedColors(), block4);

        Block block5 = Registry.BLOCK.get(new ResourceLocation(FunctionalStorage.MOD_ID, "compacting_framed_drawer"));
        event.getBlockColors().register(new FramedColors(), block5);
    }
}
