package com.buuz135.functionalstorage.block.tile;

import com.buuz135.functionalstorage.client.model.FramedDrawerModelData;
import com.buuz135.functionalstorage.util.DrawerType;
import com.buuz135.functionalstorage.util.DrawerWoodType;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.block.BasicTileBlock;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;

import javax.annotation.Nonnull;
import java.util.HashMap;

public class FramedDrawerTile extends DrawerTile {
    @Save
    private FramedDrawerModelData framedDrawerModelData;

    public FramedDrawerTile(BasicTileBlock<DrawerTile> base, DrawerType type) {
        super(base, type, DrawerWoodType.FRAMED);
        this.framedDrawerModelData = new FramedDrawerModelData(new HashMap<>());
    }

    public FramedDrawerModelData getFramedDrawerModelData() {
        return framedDrawerModelData;
    }

    public void setFramedDrawerModelData(FramedDrawerModelData framedDrawerModelData) {
        this.framedDrawerModelData = framedDrawerModelData;
        markForUpdate();
        if (level.isClientSide) requestModelDataUpdate();
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        return new ModelDataMap.Builder().withInitial(FramedDrawerModelData.FRAMED_PROPERTY, framedDrawerModelData).build();
    }
}
