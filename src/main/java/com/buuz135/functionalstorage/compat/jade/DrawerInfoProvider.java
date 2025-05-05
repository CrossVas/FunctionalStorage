package com.buuz135.functionalstorage.compat.jade;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.tile.*;
import com.buuz135.functionalstorage.init.FunctionalItems;
import com.buuz135.functionalstorage.inventory.BigInventoryHandler;
import com.buuz135.functionalstorage.inventory.CompactingInventoryHandler;
import com.buuz135.functionalstorage.inventory.EnderInventoryHandler;
import com.buuz135.functionalstorage.item.UpgradeItem;
import com.buuz135.functionalstorage.util.NumberUtils;
import com.buuz135.functionalstorage.world.EnderSavedData;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.text.WordUtils;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;

import java.util.Locale;

public class DrawerInfoProvider implements IBlockComponentProvider {

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig config) {
        TileEntity blockEntity = blockAccessor.getBlockEntity();
        IElementHelper helper = iTooltip.getElementHelper();
        iTooltip.add(helper.spacer(0, 0));
        if (blockEntity instanceof ControllableDrawerTile && !(blockEntity instanceof DrawerControllerTile) && !(blockEntity instanceof ControllerExtensionTile)) {
            if (blockEntity instanceof DrawerTile) {
                BigInventoryHandler handler = ((DrawerTile) blockEntity).getHandler();
                for (int i = 0; i < handler.getStoredStacks().size(); i++) {
                    BigInventoryHandler.BigStack storedStack = handler.getStoredStacks().get(i);
                    if (storedStack.getAmount() > 0 || (handler.isLocked() && !storedStack.getStack().isEmpty())) {
                        iTooltip.append(helper.item(storedStack.getStack(), 1, NumberUtils.getFormatedBigNumber(handler.getStackInSlot(i).getCount()) + "/" + NumberUtils.getFormatedBigNumber(handler.getSlotLimit(i))));
                    }
                }
            }
            if (blockEntity instanceof EnderDrawerTile) {
                EnderInventoryHandler savedData = EnderSavedData.getInstance(blockAccessor.getLevel()).getFrequency(((EnderDrawerTile) blockEntity).getFrequency());
                iTooltip.add(helper.spacer(0, 0));
                for (int i = 0; i < savedData.getStoredStacks().size(); i++) {
                    BigInventoryHandler.BigStack storedStack = savedData.getStoredStacks().get(i);
                    if (storedStack.getAmount() > 0 || (savedData.isLocked() && !storedStack.getStack().isEmpty())) {
                        iTooltip.append(helper.item(storedStack.getStack(), 1, NumberUtils.getFormatedBigNumber(storedStack.getAmount()) + "/" + NumberUtils.getFormatedBigNumber(savedData.getSlotLimit(i))));
                    }
                }
            }
            if (blockEntity instanceof CompactingDrawerTile) {
                CompactingInventoryHandler inventoryHandler = ((CompactingDrawerTile) blockEntity).getHandler();
                int amount = inventoryHandler.getAmount();
                iTooltip.append(helper.item(inventoryHandler.getResultList().get(2).getResult(), 1, NumberUtils.getFormatedBigNumber(inventoryHandler.getStackInSlot(2).getCount()) + "/" + NumberUtils.getFormatedBigNumber(inventoryHandler.getSlotLimit(2))));
                amount -= inventoryHandler.getResultList().get(2).getNeeded() * inventoryHandler.getStackInSlot(2).getCount();
                iTooltip.append(helper.item(inventoryHandler.getResultList().get(1).getResult(), 1, NumberUtils.getFormatedBigNumber((int) Math.floor(amount / inventoryHandler.getResultList().get(1).getNeeded()))));
                amount -= inventoryHandler.getResultList().get(1).getNeeded() * Math.floor(amount / inventoryHandler.getResultList().get(1).getNeeded());
                iTooltip.append(helper.item(inventoryHandler.getResultList().get(0).getResult(), 1, NumberUtils.getFormatedBigNumber((int) Math.floor(amount / inventoryHandler.getResultList().get(0).getNeeded()))));
            }
            if (blockEntity instanceof SimpleCompactingDrawerTile) {
                CompactingInventoryHandler inventoryHandler = ((SimpleCompactingDrawerTile) blockEntity).getHandler();
                int amount = inventoryHandler.getAmount();
                iTooltip.append(helper.item(inventoryHandler.getResultList().get(1).getResult(), 1, NumberUtils.getFormatedBigNumber((int) Math.floor(amount / inventoryHandler.getResultList().get(1).getNeeded()))));
                amount -= inventoryHandler.getResultList().get(1).getNeeded() * Math.floor(amount / inventoryHandler.getResultList().get(1).getNeeded());
                iTooltip.append(helper.item(inventoryHandler.getResultList().get(0).getResult(), 1, NumberUtils.getFormatedBigNumber((int) Math.floor(amount / inventoryHandler.getResultList().get(0).getNeeded()))));
            }
            iTooltip.add(helper.spacer(0, 0));
            for (int i = 0; i < ((ControllableDrawerTile) blockEntity).getUtilityUpgrades().getSlots(); i++) {
                ItemStack stack = ((ControllableDrawerTile) blockEntity).getUtilityUpgrades().getStackInSlot(i);
                if (!stack.isEmpty()) {
                    String extra = "";
                    if (stack.sameItem(FunctionalItems.PUSHING_UPGRADE.get().getDefaultInstance()) ||
                            stack.sameItem(FunctionalItems.PULLING_UPGRADE.get().getDefaultInstance()) ||
                            stack.sameItem(FunctionalItems.COLLECTOR_UPGRADE.get().getDefaultInstance())) {
                        extra = WordUtils.capitalize(UpgradeItem.getDirection(stack).name().toLowerCase(Locale.ROOT));
                        if (extra.equals("Up")) {
                            extra = "   " + extra;
                        }
                    }
                    iTooltip.append(helper.item(stack, 1f, extra));
                }
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return FunctionalStorageJadePlugin.DRAWER;
    }

    @Override
    public int getDefaultPriority() {
        return TooltipPosition.HEAD;
    }
}
