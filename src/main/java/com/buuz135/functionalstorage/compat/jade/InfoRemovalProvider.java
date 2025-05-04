package com.buuz135.functionalstorage.compat.jade;

import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.ControllerExtensionTile;
import com.buuz135.functionalstorage.block.tile.DrawerControllerTile;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

public class InfoRemovalProvider implements IBlockComponentProvider {

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        TileEntity blockEntity = blockAccessor.getBlockEntity();
        if (blockEntity instanceof ControllableDrawerTile && !(blockEntity instanceof DrawerControllerTile) && !(blockEntity instanceof ControllerExtensionTile)) {
            iTooltip.remove(Identifiers.UNIVERSAL_ITEM_STORAGE);
        }
    }

    @Override
    public ResourceLocation getUid() {
        return FunctionalStorageJadePlugin.DRAWER;
    }

    @Override
    public int getDefaultPriority() {
        return TooltipPosition.TAIL;
    }
}
