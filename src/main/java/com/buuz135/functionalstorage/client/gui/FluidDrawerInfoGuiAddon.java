package com.buuz135.functionalstorage.client.gui;

import com.buuz135.functionalstorage.fluid.BigFluidHandler;
import com.buuz135.functionalstorage.util.NumberUtils;
import com.buuz135.functionalstorage.util.internal.Rect2i;
import com.hrznstudio.titanium.client.screen.addon.BasicScreenAddon;
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class FluidDrawerInfoGuiAddon extends BasicScreenAddon {

    private final ResourceLocation gui;
    private final int slotAmount;
    private final Function<Integer, Pair<Integer, Integer>> slotPosition;
    private final Supplier<BigFluidHandler> fluidHandlerSupplier;
    private final Function<Integer, Integer> slotMaxAmount;

    public FluidDrawerInfoGuiAddon(int posX, int posY, ResourceLocation gui, int slotAmount, Function<Integer, Pair<Integer, Integer>> slotPosition, Supplier<BigFluidHandler> fluidHandlerSupplier, Function<Integer, Integer> slotMaxAmount) {
        super(posX, posY);
        this.gui = gui;
        this.slotAmount = slotAmount;
        this.slotPosition = slotPosition;
        this.fluidHandlerSupplier = fluidHandlerSupplier;
        this.slotMaxAmount = slotMaxAmount;
    }

    public static Rect2i getSizeForSlots(int currentSlot, int slotAmount) {
        if (slotAmount == 1) {
            return new Rect2i(9, 9, 30, 30);
        }
        if (slotAmount == 2) {
            if (currentSlot == 0) return new Rect2i(0, 30, 48, 13);
            if (currentSlot == 1) return new Rect2i(0, 6, 48, 13);
        }
        if (slotAmount == 4) {
            if (currentSlot == 0) return new Rect2i(30, 30, 16, 16);
            if (currentSlot == 1) return new Rect2i(2, 30, 16, 16);
            if (currentSlot == 2) return new Rect2i(30, 2, 16, 16);
            if (currentSlot == 3) return new Rect2i(2, 2, 16, 16);
        }
        return new Rect2i(0, 0, 0, 0);
    }

    public static Rect2i getSizeForHoverSlots(int currentSlot, int slotAmount) {
        if (slotAmount == 1) {
            return new Rect2i(9, 9, 30, 30);
        }
        if (slotAmount == 2) {
            if (currentSlot == 0) return new Rect2i(6, 30, 36, 12);
            if (currentSlot == 1) return new Rect2i(6, 6, 36, 12);
        }
        if (slotAmount == 4) {
            if (currentSlot == 0) return new Rect2i(30, 30, 12, 12);
            if (currentSlot == 1) return new Rect2i(6, 30, 12, 12);
            if (currentSlot == 2) return new Rect2i(30, 6, 12, 12);
            if (currentSlot == 3) return new Rect2i(6, 6, 12, 12);
        }
        return new Rect2i(0, 0, 0, 0);
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
        for (int i = 0; i < slotAmount; i++) {
            FluidStack fluidStack = fluidHandlerSupplier.get().getFluidInTank(i);
            if (fluidStack.isEmpty() && fluidHandlerSupplier.get().isDrawerLocked()) {
                fluidStack = fluidHandlerSupplier.get().getFilterStack()[i];
            }
            if (!fluidStack.isEmpty()) {
                renderFluid(stack, screen, guiX, guiY, fluidStack, i, slotAmount);
            }
        }
        Minecraft.getInstance().textureManager.bind(gui);
        int size = 16 * 2 + 16;
        Screen.blit(stack, guiX + getPosX(), guiY + getPosY(), 0, 0, size, size, size, size);
        for (int i = 0; i < slotAmount; i++) {
            FluidStack fluidStack = fluidHandlerSupplier.get().getFluidInTank(i);
            if (!fluidStack.isEmpty()) {
                int x = guiX + slotPosition.apply(i).getLeft() + getPosX();
                int y = guiY + slotPosition.apply(i).getRight() + getPosY();
                String amount = NumberUtils.getFormatedFluidBigNumber(fluidStack.getAmount()) + "/" + NumberUtils.getFormatedFluidBigNumber(slotMaxAmount.apply(i));
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
            Rect2i rect = getSizeForHoverSlots(i, slotAmount);
            int x = rect.getX() + getPosX() + guiX;
            int y = rect.getY() + getPosY() + guiY;
            if (mouseX > x && mouseX < x + rect.getWidth() && mouseY > y && mouseY < y + rect.getHeight()) {
                x = getPosX() + rect.getX();
                y = getPosY() + rect.getY();
                stack.translate(0, 0, 200);
                AbstractGui.fill(stack, x, y, x + rect.getWidth(), y + rect.getHeight(), -2130706433);
                stack.translate(0, 0, -200);
                List<ITextComponent> componentList = new ArrayList<>();
                FluidStack over = fluidHandlerSupplier.get().getFluidInTank(i);
                if (over.isEmpty()) {
                    componentList.add(new TranslationTextComponent("gui.functionalstorage.fluid").withStyle(TextFormatting.GOLD).append(new StringTextComponent("Empty").withStyle(TextFormatting.WHITE)));
                } else {
                    componentList.add(new TranslationTextComponent("gui.functionalstorage.fluid").withStyle(TextFormatting.GOLD).append(over.getDisplayName().copy().withStyle(TextFormatting.WHITE)));
                    String amount = NumberUtils.getFormatedFluidBigNumber(over.getAmount()) + "/" + NumberUtils.getFormatedFluidBigNumber(slotMaxAmount.apply(i));
                    componentList.add(new TranslationTextComponent("gui.functionalstorage.amount").withStyle(TextFormatting.GOLD).append(new StringTextComponent(amount).withStyle(TextFormatting.WHITE)));
                }
                componentList.add(new TranslationTextComponent("gui.functionalstorage.slot").withStyle(TextFormatting.GOLD).append(new StringTextComponent(i + "").withStyle(TextFormatting.WHITE)));
                screen.renderComponentTooltip(stack, componentList, mouseX - guiX, mouseY - guiY);
            }
        }
    }

    public void renderFluid(MatrixStack stack, Screen screen, int guiX, int guiY, FluidStack fluidStack, int slot, int slotAmount) {
        ResourceLocation flowing = fluidStack.getFluid().getAttributes().getStillTexture(fluidStack);
        if (flowing != null) {
            Texture texture = screen.getMinecraft().getTextureManager().getTexture(PlayerContainer.BLOCK_ATLAS); //getAtlasSprite
            if (texture instanceof AtlasTexture) {
                TextureAtlasSprite sprite = ((AtlasTexture) texture).getSprite(flowing);
                if (sprite != null) {
                    Minecraft.getInstance().textureManager.bind(PlayerContainer.BLOCK_ATLAS);
                    Color color = new Color(fluidStack.getFluid().getAttributes().getColor(fluidStack));
                    Rect2i rect = getSizeForSlots(slot, slotAmount);
                    RenderSystem.color4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
                    RenderSystem.enableBlend();
                    for (int x = 0; x < rect.getWidth(); x += 16) {
                        for (int y = 0; y < rect.getHeight(); y += 16) {
                            Screen.blit(stack, this.getPosX() + guiX + rect.getX() + x,
                                    this.getPosY() + guiY + rect.getY() + y,
                                    0,
                                    Math.min(16, rect.getWidth() - x),
                                    Math.min(16, rect.getHeight() - y),
                                    sprite);
                        }
                    }

                    RenderSystem.disableBlend();
                    RenderSystem.color4f(1, 1, 1, 1);
                }
            }
        }
    }


}
