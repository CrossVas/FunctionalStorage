package com.buuz135.functionalstorage.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class TooltipUtil {

    private static final Vector3f DIFFUSE_LIGHT_0 = Util.make(new Vector3f(0.2f, 1.0f, -0.7f), Vector3f::normalize);
    private static final Vector3f DIFFUSE_LIGHT_1 = Util.make(new Vector3f(-0.2f, 1.0f, 0.7f), Vector3f::normalize);

    public static void renderItems(MatrixStack stack, List<ItemStack> items, int x, int y) {
        for (int i = 0; i < items.size(); i++) {
            renderItemIntoGUI(stack, items.get(i), x + 18 * i, y, 512);
        }
    }

    public static void renderItemIntoGUI(MatrixStack matrixStack, ItemStack stack, int x, int y, int z) {
        renderItemModelIntoGUI(matrixStack, stack, x, y, z, Minecraft.getInstance().getItemRenderer().getModel(stack, (World) null, (LivingEntity) null));
    }

    public static void renderItemAdvanced(MatrixStack matrixStack, ItemStack stack, int x, int y, int z, String amount) {
        renderItemModelIntoGUI(matrixStack, stack, x, y, z, Minecraft.getInstance().getItemRenderer().getModel(stack, (World) null, (LivingEntity) null));
        renderItemStackOverlay(matrixStack, Minecraft.getInstance().font, stack, x, y, amount, amount.length() - 2);
    }

    public static void renderItemStackOverlay(MatrixStack matrixStack, FontRenderer fr, ItemStack stack, int xPosition, int yPosition, @Nullable String text, int scaled) {
        matrixStack.pushPose();
        matrixStack.translate(0, 0, 512 + 32);
        if (!stack.isEmpty()) {
            if (stack.getCount() != 1 || text != null) {
                String s = text == null ? String.valueOf(stack.getCount()) : text;
                if (text == null && stack.getCount() < 1) {
                    TextFormatting var10000 = TextFormatting.RED;
                    s = var10000 + String.valueOf(stack.getCount());
                }

                matrixStack.translate(0.0D, 0.0D, (double) (Minecraft.getInstance().getItemRenderer().blitOffset + 200.0F));
                RenderSystem.disableDepthTest();
                RenderSystem.disableBlend();
                if (scaled >= 2) {
                    matrixStack.pushPose();
                    matrixStack.scale(0.5F, 0.5F, 0.5F);
                    fr.drawShadow(matrixStack, s, (float) ((xPosition + 19 - 2) * 2 - 1 - fr.width(s)), (float) (yPosition * 2 + 24), 16777215);
                    matrixStack.popPose();
                } else if (scaled == 1) {
                    matrixStack.pushPose();
                    matrixStack.scale(0.75F, 0.75F, 0.75F);
                    fr.drawShadow(matrixStack, s, (float) (xPosition - 2) * 1.34F + 24.0F - (float) fr.width(s), (float) yPosition * 1.34F + 14.0F, 16777215);
                    matrixStack.popPose();
                } else {
                    fr.drawShadow(matrixStack, s, (float) (xPosition + 19 - 2 - fr.width(s)), (float) (yPosition + 6 + 3), 16777215);
                }

                RenderSystem.enableDepthTest();
                RenderSystem.enableBlend();
            }
        }
        matrixStack.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    public static void renderItemModelIntoGUI(MatrixStack posestack, ItemStack stack, int x, int y, int z, IBakedModel bakedmodel) {
        Minecraft.getInstance().getTextureManager().getTexture(PlayerContainer.BLOCK_ATLAS).setFilter(false, false);
        Minecraft.getInstance().textureManager.bind(PlayerContainer.BLOCK_ATLAS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        posestack.pushPose();
        posestack.translate((double) x, (double) y, (double) z);
        posestack.translate(8.0D, 8.0D, 0.0D);
        posestack.scale(1.0F, -1.0F, 1.0F);
        posestack.scale(16.0F, 16.0F, 16.0F);
        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        boolean flag = !bakedmodel.usesBlockLight();
        if (flag) {
            setupForFlatItems();
        }
        Minecraft.getInstance().getItemRenderer().render(stack, ItemCameraTransforms.TransformType.GUI, false, posestack, buffer, 15728880, OverlayTexture.NO_OVERLAY, bakedmodel);
        buffer.endBatch();
        RenderSystem.enableDepthTest();
        if (flag) {
            setupFor3DItems();
        }
        posestack.popPose();
    }

    public static void setupForFlatItems() {
        RenderSystem.setupGuiFlatDiffuseLighting(DIFFUSE_LIGHT_0, DIFFUSE_LIGHT_1);
    }

    public static void setupFor3DItems() {
        RenderSystem.setupGui3DDiffuseLighting(DIFFUSE_LIGHT_0, DIFFUSE_LIGHT_1);
    }
}
