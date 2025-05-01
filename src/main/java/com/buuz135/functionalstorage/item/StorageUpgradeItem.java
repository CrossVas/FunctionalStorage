package com.buuz135.functionalstorage.item;

import com.hrznstudio.titanium.item.BasicItem;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class StorageUpgradeItem extends UpgradeItem{

    private final StorageTier storageTier;

    public StorageUpgradeItem(StorageTier tier) {
        super(new Properties(), Type.STORAGE);
        this.storageTier = tier;
    }

    public int getStorageMultiplier() {
        return storageTier.storageMultiplier;
    }

    public StorageTier getStorageTier() {
        return storageTier;
    }

    @Override
    public void addTooltipDetails(@Nullable BasicItem.Key key, ItemStack stack, List<ITextComponent> tooltip, boolean advanced) {
        super.addTooltipDetails(key, stack, tooltip, advanced);
        if (storageTier == StorageTier.IRON){
            tooltip.add(new TranslationTextComponent("item.utility.downgrade").withStyle(TextFormatting.GRAY));
        } else {
            tooltip.add(new TranslationTextComponent("storageupgrade.desc.item").withStyle(TextFormatting.GRAY).append(this.storageTier.getStorageMultiplier() + ""));
            tooltip.add(new TranslationTextComponent("storageupgrade.desc.fluid").withStyle(TextFormatting.GRAY).append(this.storageTier.getStorageMultiplier() / 2 + ""));
        }
    }

    @Override
    public boolean hasTooltipDetails(@Nullable BasicItem.Key key) {
        return key == null;
    }


    @Override
    @OnlyIn(Dist.CLIENT)
    public ITextComponent getName(ItemStack p_41458_) {
        ITextComponent component = super.getName(p_41458_);
        if (component instanceof TranslationTextComponent) {
            ((TranslationTextComponent) component).setStyle(Style.EMPTY.withColor(Color.fromRgb(storageTier == StorageTier.NETHERITE && Minecraft.getInstance().level != null ? MathHelper.hsvToRgb((Minecraft.getInstance().level.getGameTime() % 360) / 360f, 1, 1) : storageTier.getColor())));
        }
        return component;
    }

    public static enum StorageTier {
        COPPER(8, MathHelper.color(204, 109, 81)),
        GOLD(16, MathHelper.color(233, 177, 21)),
        DIAMOND(24, MathHelper.color(32, 197, 181)),
        NETHERITE(32, MathHelper.color(49, 41, 42)),
        IRON(1, MathHelper.color(130, 130, 130));

        private final int storageMultiplier;
        private final int color;

        StorageTier(int storageMultiplier, int color) {
            this.storageMultiplier = storageMultiplier;
            this.color = color;
        }

        public int getStorageMultiplier() {
            return storageMultiplier;
        }

        public int getColor() {
            return color;
        }
    }
}
