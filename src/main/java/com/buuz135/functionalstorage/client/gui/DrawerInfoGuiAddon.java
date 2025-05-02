package com.buuz135.functionalstorage.client.gui;

import com.buuz135.functionalstorage.util.NumberUtils;
import com.hrznstudio.titanium.client.screen.addon.BasicScreenAddon;
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class DrawerInfoGuiAddon extends BasicScreenAddon {

    private final ResourceLocation gui;
    private final int slotAmount;
    private final Function<Integer, Pair<Integer, Integer>> slotPosition;
    private final Function<Integer, ItemStack> slotStack;
    private final Function<Integer, Integer> slotMaxAmount;

    public DrawerInfoGuiAddon(int posX, int posY, ResourceLocation gui, int slotAmount, Function<Integer, Pair<Integer, Integer>> slotPosition, Function<Integer, ItemStack> slotStack, Function<Integer, Integer> slotMaxAmount) {
        super(posX, posY);
        this.gui = gui;
        this.slotAmount = slotAmount;
        this.slotPosition = slotPosition;
        this.slotStack = slotStack;
        this.slotMaxAmount = slotMaxAmount;
    }

    @Override
    public int getXSize() {
        return 0;
    }

    @Override
    public int getYSize() {
        return 0;
    }

    @Override
    public void drawBackgroundLayer(MatrixStack stack, Screen screen, IAssetProvider provider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
        Minecraft.getInstance().textureManager.bind(gui);
        int size = 16 * 2 + 16;
        Screen.blit(stack, guiX + getPosX(), guiY + getPosY(), 0, 0, size, size, size, size);
        for (int i = 0; i < slotAmount; i++) {
            ItemStack itemStack = slotStack.apply(i);
            if (!itemStack.isEmpty()) {
                int x = guiX + slotPosition.apply(i).getLeft() + getPosX();
                int y = guiY + slotPosition.apply(i).getRight() + getPosY();
                Minecraft.getInstance().getItemRenderer().renderGuiItem(slotStack.apply(i), x, y);
                String amount = NumberUtils.getFormatedBigNumber(itemStack.getCount()) + "/" + NumberUtils.getFormatedBigNumber(slotMaxAmount.apply(i));
                float scale = 0.5f;
                stack.translate(0, 0, 200);
                stack.scale(scale, scale, scale);
                Minecraft.getInstance().font.drawShadow(stack, amount, (x + 17 - Minecraft.getInstance().font.width(amount) / 2) * (1 / scale), (y + 12) * (1 / scale), 0xFFFFFF);
                stack.scale(1 / scale, 1 / scale, 1 / scale);
                stack.translate(0, 0, -200);
            }
        }
    }

    @Override
    public void drawForegroundLayer(MatrixStack stack, Screen screen, IAssetProvider provider, int guiX, int guiY, int mouseX, int mouseY) {
        for (int i = 0; i < slotAmount; i++) {
            int x = slotPosition.apply(i).getLeft() + getPosX() + guiX;
            int y = slotPosition.apply(i).getRight() + getPosY() + guiY;
            if (mouseX > x && mouseX < x + 18 && mouseY > y && mouseY < y + 18) {
                x = slotPosition.apply(i).getLeft() + getPosX();
                y = slotPosition.apply(i).getRight() + getPosY();
                stack.translate(0, 0, 200);
                AbstractGui.fill(stack, x - 1, y - 1, x + 17, y + 17, -2130706433);
                stack.translate(0, 0, -200);
                List<ITextComponent> componentList = new ArrayList<>();
                ItemStack over = slotStack.apply(i);
                if (over.isEmpty()) {
                    componentList.add(new TranslationTextComponent("gui.functionalstorage.item").withStyle(TextFormatting.GOLD).append(new StringTextComponent("Empty").withStyle(TextFormatting.WHITE)));
                } else {
                    componentList.add(new TranslationTextComponent("gui.functionalstorage.item").withStyle(TextFormatting.GOLD).append(over.getHoverName().copy().withStyle(TextFormatting.WHITE)));
                    String amount = NumberUtils.getFormatedBigNumber(over.getCount()) + "/" + NumberUtils.getFormatedBigNumber(slotMaxAmount.apply(i));
                    componentList.add(new TranslationTextComponent("gui.functionalstorage.amount").withStyle(TextFormatting.GOLD).append(new StringTextComponent(amount).withStyle(TextFormatting.WHITE)));
                }
                componentList.add(new TranslationTextComponent("gui.functionalstorage.slot").withStyle(TextFormatting.GOLD).append(new StringTextComponent(i + "").withStyle(TextFormatting.WHITE)));
                screen.renderComponentTooltip(stack, componentList, mouseX - guiX, mouseY - guiY);
            }
        }
    }

}
