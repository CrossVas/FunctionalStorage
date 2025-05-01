package com.buuz135.functionalstorage.world;

import com.buuz135.functionalstorage.inventory.EnderInventoryHandler;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

import java.util.HashMap;

public class EnderSavedData extends WorldSavedData {

    public static EnderSavedData CLIENT = new EnderSavedData();

    public static final String NAME = "FunctionalStorageEnder";

    private final HashMap<String, EnderInventoryHandler> itemHandlers = new HashMap<>();

    public EnderSavedData() {
        super(NAME);
    }

    public static EnderSavedData getInstance(World world) {
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) world;
            DimensionSavedDataManager storage = serverWorld.getDataStorage();

            return storage.computeIfAbsent(
                    EnderSavedData::new,
                    NAME
            );
        } else {
            return CLIENT;
        }
    }

    @Override
    public void load(CompoundNBT tag) {
        itemHandlers.clear();
        CompoundNBT enderTag = tag.getCompound("Ender");
        for (String key : enderTag.getAllKeys()) {
            EnderInventoryHandler handler = new EnderInventoryHandler(key, this);
            handler.deserializeNBT(enderTag.getCompound(key));
            itemHandlers.put(key, handler);
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        CompoundNBT enderTag = new CompoundNBT();
        itemHandlers.forEach((key, handler) -> enderTag.put(key, handler.serializeNBT()));
        tag.put("Ender", enderTag);
        return tag;
    }

    public void setFrequency(String frequency, EnderInventoryHandler handler) {
        itemHandlers.put(frequency, handler);
        setDirty(); // mark data as dirty so it gets saved
    }

    public EnderInventoryHandler getFrequency(String frequency) {
        return itemHandlers.computeIfAbsent(frequency, s -> new EnderInventoryHandler(s, this));
    }

    public HashMap<String, EnderInventoryHandler> getItemHandlers() {
        return itemHandlers;
    }
}
