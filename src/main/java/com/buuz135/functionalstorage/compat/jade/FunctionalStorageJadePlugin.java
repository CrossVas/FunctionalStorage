package com.buuz135.functionalstorage.compat.jade;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.hrznstudio.titanium.block.BasicTileBlock;
import net.minecraft.util.ResourceLocation;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin(FunctionalStorage.MOD_ID)
public class FunctionalStorageJadePlugin implements IWailaPlugin {

    public static final ResourceLocation DRAWER = new ResourceLocation(FunctionalStorage.MOD_ID, "drawer");

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new DrawerInfoProvider(), BasicTileBlock.class);
        registration.registerBlockComponent(new InfoRemovalProvider(), BasicTileBlock.class);
    }
}
