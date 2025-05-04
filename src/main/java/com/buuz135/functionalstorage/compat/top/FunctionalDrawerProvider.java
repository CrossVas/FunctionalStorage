package com.buuz135.functionalstorage.compat.top;

import com.buuz135.functionalstorage.block.EnderDrawerBlock;
import com.buuz135.functionalstorage.block.tile.*;
import com.buuz135.functionalstorage.init.FunctionalItems;
import com.buuz135.functionalstorage.inventory.BigInventoryHandler;
import com.buuz135.functionalstorage.inventory.CompactingInventoryHandler;
import com.buuz135.functionalstorage.inventory.EnderInventoryHandler;
import com.buuz135.functionalstorage.item.UpgradeItem;
import com.buuz135.functionalstorage.util.NumberUtils;
import com.buuz135.functionalstorage.world.EnderSavedData;
import mcjty.theoneprobe.api.*;
import mcjty.theoneprobe.apiimpl.elements.ElementHorizontal;
import mcjty.theoneprobe.apiimpl.elements.ElementTank;
import mcjty.theoneprobe.apiimpl.elements.ElementVertical;
import mcjty.theoneprobe.apiimpl.styles.ProgressStyle;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import org.apache.commons.lang3.text.WordUtils;

import java.awt.Color;
import java.util.Locale;
import java.util.function.Function;

public class FunctionalDrawerProvider implements IProbeInfoProvider {

    public static Function<ITheOneProbe, Void> REGISTER = iTheOneProbe -> {
        iTheOneProbe.registerProvider(new FunctionalDrawerProvider());
        iTheOneProbe.registerElementFactory(CustomElementItemStack::new);
        return null;
    };


    @Override
    public String getID() {
        return "drawer";
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo iProbeInfo, PlayerEntity player, World level, BlockState blockState, IProbeHitData iProbeHitData) {
        TileEntity blockEntity = level.getBlockEntity(iProbeHitData.getPos());
        if (blockEntity instanceof ControllableDrawerTile && !(blockEntity instanceof DrawerControllerTile) && !(blockEntity instanceof ControllerExtensionTile)) {
            iProbeInfo.getElements().removeIf(iElement -> iElement instanceof ElementVertical);
            ElementVertical vertical = new ElementVertical();
            if (blockEntity instanceof DrawerTile) {
                BigInventoryHandler handler = ((DrawerTile) blockEntity).getHandler();
                if (handler.getSlots() == 1 || player.isShiftKeyDown() || probeMode == ProbeMode.EXTENDED) {
                    ElementVertical elementVertical = new ElementVertical(iProbeInfo.defaultLayoutStyle().spacing(2).leftPadding(7).rightPadding(7));
                    elementVertical.getStyle().borderColor(Color.CYAN.darker().getRGB());
                    for (int i = 0; i < handler.getStoredStacks().size(); i++) {
                        BigInventoryHandler.BigStack storedStack = handler.getStoredStacks().get(i);
                        if (storedStack.getAmount() > 0 || (handler.isLocked() && !storedStack.getStack().isEmpty())) {
                            elementVertical.element(new CustomElementItemStack(storedStack.getStack(), NumberUtils.getFormatedBigNumber(handler.getStackInSlot(i).getCount()) + "/" + NumberUtils.getFormatedBigNumber(handler.getSlotLimit(i)), iProbeInfo.defaultItemStyle(), true));
                        }
                    }
                    if (elementVertical.getElements().size() > 0) vertical.element(elementVertical);
                    vertical.element(new ElementVertical(iProbeInfo.defaultLayoutStyle().topPadding(4)));
                } else {
                    ElementHorizontal abstractElementPanel = new ElementHorizontal(iProbeInfo.defaultLayoutStyle().spacing(8).leftPadding(7).rightPadding(7));
                    abstractElementPanel.getStyle().borderColor(Color.CYAN.darker().getRGB());
                    for (int i = 0; i < handler.getStoredStacks().size(); i++) {
                        BigInventoryHandler.BigStack storedStack = handler.getStoredStacks().get(i);
                        if (storedStack.getAmount() > 0 || (handler.isLocked() && !storedStack.getStack().isEmpty())) {
                            abstractElementPanel.element(new CustomElementItemStack(storedStack.getStack(), NumberUtils.getFormatedBigNumber(handler.getStackInSlot(i).getCount()) + "/" + NumberUtils.getFormatedBigNumber(handler.getSlotLimit(i)), iProbeInfo.defaultItemStyle()));
                        }
                    }
                    if (abstractElementPanel.getElements().size() > 0) vertical.element(abstractElementPanel);
                    vertical.element(new ElementVertical(iProbeInfo.defaultLayoutStyle().topPadding(4)));
                }
            }
            if (blockEntity instanceof EnderDrawerTile) {
                ElementHorizontal abstractElementPanel = new ElementHorizontal(iProbeInfo.defaultLayoutStyle().spacing(8).leftPadding(7).rightPadding(7));
                abstractElementPanel.getStyle().borderColor(Color.CYAN.darker().getRGB());
                EnderInventoryHandler savedData = EnderSavedData.getInstance(level).getFrequency(((EnderDrawerTile) blockEntity).getFrequency());
                for (int i = 0; i < savedData.getStoredStacks().size(); i++) {
                    BigInventoryHandler.BigStack storedStack = savedData.getStoredStacks().get(i);
                    if (storedStack.getAmount() > 0 || (savedData.isLocked() && !storedStack.getStack().isEmpty())) {
                        abstractElementPanel.element(new CustomElementItemStack(storedStack.getStack(), NumberUtils.getFormatedBigNumber(storedStack.getAmount()) + "/" + NumberUtils.getFormatedBigNumber(savedData.getSlotLimit(i)), iProbeInfo.defaultItemStyle(), player.isShiftKeyDown() || probeMode == ProbeMode.EXTENDED));
                    }
                }
                if (abstractElementPanel.getElements().size() > 0) vertical.element(abstractElementPanel);
                ElementVertical elementVertical = new ElementVertical(iProbeInfo.defaultLayoutStyle());
                elementVertical.getStyle().borderColor(Color.CYAN.darker().getRGB());
                elementVertical.text(new TranslationTextComponent("linkingtool.ender.frequency"));
                vertical.element(new ElementVertical(iProbeInfo.defaultLayoutStyle().topPadding(4)));
                abstractElementPanel = new ElementHorizontal(iProbeInfo.defaultLayoutStyle().leftPadding(4).topPadding(2).rightPadding(4));
                for (ItemStack stack : EnderDrawerBlock.getFrequencyDisplay(((EnderDrawerTile) blockEntity).getFrequency())) {
                    abstractElementPanel.element(new CustomElementItemStack(stack, "", iProbeInfo.defaultItemStyle()));
                }
                elementVertical.element(abstractElementPanel);
                vertical.element(elementVertical);
                vertical.element(new ElementVertical(iProbeInfo.defaultLayoutStyle().topPadding(4)));
            }
            if (blockEntity instanceof CompactingDrawerTile) {
                CompactingInventoryHandler inventoryHandler = ((CompactingDrawerTile) blockEntity).getHandler();
                if (player.isShiftKeyDown() || probeMode == ProbeMode.EXTENDED || inventoryHandler.isCreative()) {
                    ElementVertical abstractElementPanel = new ElementVertical(iProbeInfo.defaultLayoutStyle().spacing(2).leftPadding(7).rightPadding(7));
                    abstractElementPanel.getStyle().borderColor(Color.CYAN.darker().getRGB());
                    abstractElementPanel.element(new CustomElementItemStack(inventoryHandler.getResultList().get(2).getResult(), NumberUtils.getFormatedBigNumber(inventoryHandler.getStackInSlot(2).getCount()) + "/" + NumberUtils.getFormatedBigNumber(inventoryHandler.getSlotLimit(2)), iProbeInfo.defaultItemStyle(), true));
                    abstractElementPanel.element(new CustomElementItemStack(inventoryHandler.getResultList().get(1).getResult(), NumberUtils.getFormatedBigNumber(inventoryHandler.getStackInSlot(1).getCount()) + "/" + NumberUtils.getFormatedBigNumber(inventoryHandler.getSlotLimit(1)), iProbeInfo.defaultItemStyle(), true));
                    abstractElementPanel.element(new CustomElementItemStack(inventoryHandler.getResultList().get(0).getResult(), NumberUtils.getFormatedBigNumber(inventoryHandler.getStackInSlot(0).getCount()) + "/" + NumberUtils.getFormatedBigNumber(inventoryHandler.getSlotLimit(0)), iProbeInfo.defaultItemStyle(), true));
                    if (abstractElementPanel.getElements().size() > 0) vertical.element(abstractElementPanel);
                } else {
                    ElementHorizontal abstractElementPanel = new ElementHorizontal(iProbeInfo.defaultLayoutStyle().spacing(8).leftPadding(7).rightPadding(7));
                    abstractElementPanel.getStyle().borderColor(Color.CYAN.darker().getRGB());
                    int amount = inventoryHandler.getAmount();
                    abstractElementPanel.element(new CustomElementItemStack(inventoryHandler.getResultList().get(2).getResult(), NumberUtils.getFormatedBigNumber(inventoryHandler.getStackInSlot(2).getCount()) + "/" + NumberUtils.getFormatedBigNumber(inventoryHandler.getSlotLimit(2)), iProbeInfo.defaultItemStyle()));
                    amount -= inventoryHandler.getResultList().get(2).getNeeded() * inventoryHandler.getStackInSlot(2).getCount();
                    abstractElementPanel.element(new CustomElementItemStack(inventoryHandler.getResultList().get(1).getResult(), NumberUtils.getFormatedBigNumber((int) Math.floor(amount / inventoryHandler.getResultList().get(1).getNeeded())), iProbeInfo.defaultItemStyle()));
                    amount -= inventoryHandler.getResultList().get(1).getNeeded() * Math.floor(amount / inventoryHandler.getResultList().get(1).getNeeded());
                    abstractElementPanel.element(new CustomElementItemStack(inventoryHandler.getResultList().get(0).getResult(), NumberUtils.getFormatedBigNumber((int) Math.floor(amount / inventoryHandler.getResultList().get(0).getNeeded())), iProbeInfo.defaultItemStyle()));
                    if (abstractElementPanel.getElements().size() > 0) vertical.element(abstractElementPanel);
                }
                vertical.element(new ElementVertical(iProbeInfo.defaultLayoutStyle().topPadding(4)));
            }
            if (blockEntity instanceof SimpleCompactingDrawerTile) {
                CompactingInventoryHandler inventoryHandler = ((SimpleCompactingDrawerTile) blockEntity).getHandler();
                if (player.isShiftKeyDown() || probeMode == ProbeMode.EXTENDED || inventoryHandler.isCreative()) {
                    ElementVertical abstractElementPanel = new ElementVertical(iProbeInfo.defaultLayoutStyle().spacing(2).leftPadding(7).rightPadding(7));
                    abstractElementPanel.getStyle().borderColor(Color.CYAN.darker().getRGB());
                    abstractElementPanel.element(new CustomElementItemStack(inventoryHandler.getResultList().get(1).getResult(), NumberUtils.getFormatedBigNumber(inventoryHandler.getStackInSlot(1).getCount()) + "/" + NumberUtils.getFormatedBigNumber(inventoryHandler.getSlotLimit(1)), iProbeInfo.defaultItemStyle(), true));
                    abstractElementPanel.element(new CustomElementItemStack(inventoryHandler.getResultList().get(0).getResult(), NumberUtils.getFormatedBigNumber(inventoryHandler.getStackInSlot(0).getCount()) + "/" + NumberUtils.getFormatedBigNumber(inventoryHandler.getSlotLimit(0)), iProbeInfo.defaultItemStyle(), true));
                    if (abstractElementPanel.getElements().size() > 0) vertical.element(abstractElementPanel);
                } else {
                    ElementHorizontal abstractElementPanel = new ElementHorizontal(iProbeInfo.defaultLayoutStyle().spacing(8).leftPadding(7).rightPadding(7));
                    abstractElementPanel.getStyle().borderColor(Color.CYAN.darker().getRGB());
                    int amount = inventoryHandler.getAmount();
                    abstractElementPanel.element(new CustomElementItemStack(inventoryHandler.getResultList().get(1).getResult(), NumberUtils.getFormatedBigNumber((int) Math.floor(amount / inventoryHandler.getResultList().get(1).getNeeded())), iProbeInfo.defaultItemStyle()));
                    amount -= inventoryHandler.getResultList().get(1).getNeeded() * Math.floor(amount / inventoryHandler.getResultList().get(1).getNeeded());
                    abstractElementPanel.element(new CustomElementItemStack(inventoryHandler.getResultList().get(0).getResult(), NumberUtils.getFormatedBigNumber((int) Math.floor(amount / inventoryHandler.getResultList().get(0).getNeeded())), iProbeInfo.defaultItemStyle()));
                    if (abstractElementPanel.getElements().size() > 0) vertical.element(abstractElementPanel);
                }
                vertical.element(new ElementVertical(iProbeInfo.defaultLayoutStyle().topPadding(4)));
            }
            if (blockEntity instanceof FluidDrawerTile && (player.isShiftKeyDown() || probeMode == ProbeMode.EXTENDED)) {
                ElementVertical tanksVertical = new ElementVertical(iProbeInfo.defaultLayoutStyle().spacing(2));
                for (int i = 0; i < ((FluidDrawerTile) blockEntity).getFluidHandler().getTanks(); i++) {
                    TankReference tankReference = TankReference.createTank(((FluidDrawerTile) blockEntity).getFluidHandler().getTankList()[i]);
                    tanksVertical.element(new ElementTank(tankReference, new ProgressStyle().numberFormat(NumberFormat.COMPACT)));
                }
                iProbeInfo.element(tanksVertical);
            }
            if (player.isShiftKeyDown() || probeMode == ProbeMode.EXTENDED) {
                ElementHorizontal abstractElementPanel = new ElementHorizontal(iProbeInfo.defaultLayoutStyle().topPadding(0).spacing(8).leftPadding(7).rightPadding(7));
                abstractElementPanel.getStyle().borderColor(Color.CYAN.darker().getRGB());
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
                        abstractElementPanel.element(new CustomElementItemStack(stack, extra, iProbeInfo.defaultItemStyle()));
                    }
                }
                if (abstractElementPanel.getElements().size() > 0) vertical.element(abstractElementPanel);
            }
            iProbeInfo.element(vertical);
        }

    }
}